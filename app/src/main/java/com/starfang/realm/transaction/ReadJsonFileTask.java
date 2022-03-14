package com.starfang.realm.transaction;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.starfang.R;
import com.starfang.StarfangConstants;
import com.starfang.activities.viewmodel.CMDActivityViewModel;
import com.starfang.fragments.progress.row.ProgressRowFragment;
import com.starfang.fragments.progress.row.ProgressRowViewModel;
import com.starfang.nlp.SystemMessage;
import com.starfang.realm.primitive.RealmDouble;
import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;
import com.starfang.realm.transaction.linking.LinkingCat;
import com.starfang.realm.transaction.linking.LinkingRok;
import com.starfang.utilities.RealmSyncUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class ReadJsonFileTask extends AsyncTask<String, String, Void> {

    private static final String TAG = "FANG_READ";

    private final WeakReference<Context> mContextRef;
    private final String source;
    private final String title;
    private final int marginTop;
    private final WeakReference<ViewModel> mActivityViewModelRef;

    private WeakReference<ViewModel> mViewModelRef;

    public ReadJsonFileTask(Context context, String source, String title, int marginTop,
                            WeakReference<ViewModel> activityVmRef ) {
        this.mContextRef = new WeakReference<>(context);
        this.source = source;
        this.title = title;
        this.marginTop = marginTop;
        this.mActivityViewModelRef = activityVmRef;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ViewModel activityVm = mActivityViewModelRef.get();
        if( activityVm instanceof CMDActivityViewModel ) {
            ((CMDActivityViewModel) activityVm).countUpProcess();
        }
        final Context context = mContextRef.get();
        final ProgressRowFragment progressFragment = ProgressRowFragment.newInstance(title, marginTop);
        if (context instanceof AppCompatActivity) {

            final FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.layout_progress, progressFragment).commitNow();

            mViewModelRef = new WeakReference<>(new ViewModelProvider(progressFragment).get(ProgressRowViewModel.class));
            ViewModel viewModel = mViewModelRef.get();
            if( viewModel instanceof ProgressRowViewModel) {
                ((ProgressRowViewModel) viewModel).setStepText("Sourcing");
                ((ProgressRowViewModel) viewModel).setProgress(0);
                return;
            }
        }
        this.cancel(true);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d(TAG, source + " Read json complete");

        Context context = mContextRef.get();
        ViewModel viewModel = mViewModelRef.get();

        switch( this.source ) {
            case StarfangConstants.REALM_MODEL_SOURCE_ROK:
                new LinkingRok( title, context, viewModel, mActivityViewModelRef ).execute();
                break;
            case StarfangConstants.REALM_MODEL_SOURCE_CAT:
                new LinkingCat( title, context, viewModel, mActivityViewModelRef ).execute();
                break;
            default:
                SystemMessage.insertMessage(title + " 연결 완료","com.starfang" , context);
                if( viewModel instanceof ProgressRowViewModel ) {
                    ((ProgressRowViewModel) viewModel).setRemoveFragment(true);
                }
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        // todo : case1: cancelled by error, case2: cancelled by user request
        ViewModel activityVm = mActivityViewModelRef.get();
        if( activityVm instanceof CMDActivityViewModel ) {
            ((CMDActivityViewModel) activityVm).countDownProcess();
        }


        SystemMessage.insertMessage(title + " 데이터 읽기 취소" , "com.starfang", mContextRef.get() );
        ViewModel viewModel = mViewModelRef.get();
        if( viewModel instanceof ProgressRowViewModel ) {
            ((ProgressRowViewModel) viewModel).setRemoveFragment(true);
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        // 0 : detail, 1 : progress
        super.onProgressUpdate(values);
        ViewModel model = mViewModelRef.get();
        if (model instanceof ProgressRowViewModel) {
            if (values.length > 1) {
                ((ProgressRowViewModel) model).setProgress(
                        NumberUtils.toInt(values[1], 0));
            }
            if( values.length > 0 ) {
                ((ProgressRowViewModel) model).setDetailText(values[0]);
            }
        }
    }

    @Override
    protected Void doInBackground(String... fileNames) {

        final GsonBuilder gsonBuilder = new GsonBuilder()
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                });

        gsonBuilder.registerTypeAdapter(new TypeToken<RealmList<RealmString>>() {
        }.getType(), new RealmSyncUtils.RealmStringDeserializer());

        gsonBuilder.registerTypeAdapter(new TypeToken<RealmList<RealmInteger>>() {
        }.getType(), new RealmSyncUtils.RealmIntegerDeserializer());

        gsonBuilder.registerTypeAdapter(new TypeToken<RealmList<RealmDouble>>() {
        }.getType(), new RealmSyncUtils.RealmDoubleDeserializer());

        final Gson gson = gsonBuilder.create();

        int fileNameCount = fileNames.length;
        for (int count = 0; count < fileNameCount; count++) {

            String fileName = fileNames[count];
            publishProgress(
                    "Reading file " + fileName // values[0] : detail
                    , String.valueOf((int)((float)count / fileNameCount * 100))); // values[1] : progress

            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String jsonString;
            try {
                InputStream jsonInputStream = mContextRef.get().getAssets().open(fileName);
                int size = jsonInputStream.available();

                byte[] buffer = new byte[size];
                int a = jsonInputStream.read(buffer);
                Log.d(TAG, a + " / " + size);
                jsonInputStream.close();

                jsonString = new String(buffer, StandardCharsets.UTF_8);


                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("type").equals("table")) {
                        String tableName = jsonObject.getString("name");
                        String[] tableNameSplit = tableName.split("_");

                        String modelName;
                        if (tableNameSplit.length > 1) {
                            StringBuilder modelNameBuilder = new StringBuilder();
                            for (String split : tableNameSplit) {
                                modelNameBuilder.append(StringUtils.capitalize(split));
                            }
                            modelName = modelNameBuilder.toString();
                        } else {
                            modelName = StringUtils.capitalize(tableName);
                        }

                        Class<? extends RealmObject> realmObjectClass = Class.forName(source + modelName)
                                .asSubclass(RealmObject.class);

                        publishProgress("Parsing " + fileName + "/" + modelName );

                        JSONArray tuples = jsonObject.getJSONArray("data");
                        Integer serverNo;
                        if( jsonObject.has( "server" ) ) {
                            serverNo = jsonObject.getInt("server");
                        } else {
                            serverNo = null;
                        }

                        try (Realm realm = Realm.getDefaultInstance()) {
                            realm.beginTransaction();
                            realm.delete(realmObjectClass);
                            for (int j = 0; j < tuples.length(); j++) {
                                JSONObject tuple = tuples.getJSONObject(j);
                                for (Iterator<String> it = tuple.keys(); it.hasNext(); ) {
                                    String key = it.next();
                                    if (!key.equals("desc") && !key.equals("description")) {
                                        Object obj = tuple.get(key);
                                        if (obj instanceof String) {
                                            String str = ((String) obj).trim();
                                            if (str.startsWith("[") && str.endsWith("]")) {
                                                try {
                                                    tuple.put(key, new JSONArray(str));
                                                } catch (JSONException ignore) {
                                                }
                                            } else if (str.startsWith("{") && str.endsWith("}")) {
                                                try {
                                                    JSONObject curObj = new JSONObject(str);
                                                    JSONArray newArray = new JSONArray();
                                                    Iterator<String> keyIterator = curObj.keys();
                                                    while (keyIterator.hasNext()) {
                                                        JSONObject eachObj = new JSONObject();
                                                        String eachKey = keyIterator.next();
                                                        eachObj.put(eachKey, curObj.get(eachKey));
                                                        newArray.put(eachObj);
                                                    }
                                                    tuple.put(key, newArray);
                                                } catch (JSONException ignore) {
                                                }
                                            }
                                        }
                                    }
                                }
                                if( serverNo != null && !tuple.has("server") ) {
                                    tuple.put("server", serverNo.intValue() );
                                }
                                String tupleStr = tuple.toString();
                                try {
                                    RealmObject realmObject = gson.fromJson(tupleStr, realmObjectClass);
                                    if (realmObject != null) {
                                        if (realmObject instanceof SearchNameWithoutBlank) {
                                            ((SearchNameWithoutBlank) realmObject).setNameWithoutBlank();
                                        }

                                        if (realmObject instanceof Source) {
                                            realm.copyToRealmOrUpdate(realmObject);
                                        } else {
                                            realm.copyToRealm(realmObject);
                                        }

                                    }
                                } catch (IllegalStateException e) {
                                    Log.d(TAG, tupleStr + " [exception]");
                                }
                            }
                            realm.commitTransaction();
                        } catch (RuntimeException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }

                    }
                }
            } catch (IOException | JSONException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
