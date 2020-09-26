package com.starfang.realm.transaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.starfang.R;
import com.starfang.StarfangConstants;
import com.starfang.realm.ProgressRequest;
import com.starfang.realm.TableList;
import com.starfang.realm.Transaction;
import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;
import com.starfang.ui.progress.ProgressFragment;
import com.starfang.ui.progress.ProgressViewModel;
import com.starfang.utilities.ArithmeticUtils;
import com.starfang.utilities.CipherUtils;
import com.starfang.utilities.RealmSyncUtils;

import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

/*
MySQL <1> PHP <2> App <3> User
 <1> : Mysql tables interpreted by php and converted to Json form
 <2> : App download json and convert it to realm objects
 <3> : User can watch view by realm-adaptors
 */

public class DownloadJsonTask extends Transaction<String, Bundle, Bundle> {
    private static final String TAG = "FANG_DOWNLOAD";

    protected interface ProgressCode {
        int START = 0;
        int DONE = 1;
        int PROGRESS = 2;
        int ERROR = 3;
    }

    protected interface ProgressArgs {
        String PROGRESS_CODE = "progressCode";
        String TASK_COUNT = "count";
        String TASK_INDEX = "index";
        String TITLE = "title";
        String BYTE_DONE = "byte_done";
        String BYTE_TOTAL = "byte_total";
    }

    protected interface ResultArgs {
        String RESULT_SUMMARY = "summary";
        String RESULT_MODELS = "models";
    }


    private WeakReference<Context> contextWeakReference;

    private WeakReference<ProgressViewModel> viewModelWeakRef;

    private Map<Integer, Long> byteSizeMap;

    DownloadJsonTask(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        final Context context = contextWeakReference.get();
        final ProgressFragment progressFragment = ProgressFragment.newInstance(null);
        if (context instanceof AppCompatActivity) {

            final FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(android.R.id.content, progressFragment).commitNow();

            viewModelWeakRef = new WeakReference<>(new ViewModelProvider(progressFragment).get(ProgressViewModel.class));
            ProgressViewModel progressViewModel = viewModelWeakRef.get();

            progressViewModel.setIndeterminate(false);
            progressViewModel.setQuitVisibility(View.GONE);
            progressViewModel.setTopStartText("다운로드");
            this.byteSizeMap = new HashMap<>();

            return;
        }
        this.cancel(true);


    }

    @Override
    protected void onPostExecute(Bundle result) {
        //25 actionable tasks: 5 executed, 20 up-to-date
        ProgressViewModel progressViewModel = viewModelWeakRef.get();
        if (result != null) {
            Iterator<String> iterator = result.keySet().iterator();
            Bundle[] bundles = new Bundle[result.size() - 1];
            for (int i = 0; iterator.hasNext(); ) {
                String key = iterator.next();
                if (key.equals(ResultArgs.RESULT_SUMMARY)) {
                    String summary = result.getString(key);
                    progressViewModel.setTitleText(summary);
                } else {
                    ArrayList<Integer> ids = result.getIntegerArrayList(key);
                    bundles[i] = new Bundle();
                    bundles[i].putString(Linking.modelName, key);
                    bundles[i].putIntegerArrayList(Linking.idList, ids);
                    i++;
                }
            }

            AsyncTask<Bundle, String, String> linking = new LinkingTask(progressViewModel);
            linking.execute(bundles);

        } else {
            progressViewModel.setQuitVisibility(View.VISIBLE);
        }

        long totalByteSize = 0L;
        for (Long bytes : byteSizeMap.values()) {
            try {
                totalByteSize = ArithmeticUtils.longSum(totalByteSize, bytes);
            } catch (ArithmeticException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        progressViewModel.setBelowText(MessageFormat.format("total size: {0}byte", totalByteSize));

    }

    @Override
    protected final void onProgressUpdate(Bundle... argsArray) {
        Bundle args = argsArray[0];
        if (args != null) {
            final ProgressViewModel progressViewModel = viewModelWeakRef.get();
            int progressCode = args.getInt(ProgressArgs.PROGRESS_CODE, -1);
            switch (progressCode) {
                case ProgressCode.PROGRESS:
                    long downloadedByte = args.getLong(ProgressArgs.BYTE_DONE, 0);
                    long totalByte = args.getLong(ProgressArgs.BYTE_TOTAL, 1);

                    progressViewModel.setMaxByte(totalByte);
                    byteSizeMap.put(args.getInt(ProgressArgs.TASK_INDEX, 0), totalByte);

                    float percentage = (downloadedByte / ((float) totalByte)) * 100;
                    progressViewModel.setProgress((int) percentage);
                    //progressViewModel.setAboveTextText(MessageFormat.format("{0}%", percentage));
                    //progressViewModel.setBelowText(MessageFormat.format("{0} / {1}", downloadedByte, totalByte));

                    //Log.d(TAG, downloaded + " / " + total + " (" + percentage + "%)");
                    break;
                case ProgressCode.START:
                    progressViewModel.setProgress(0);
                    progressViewModel.setTitleText(args.getString(ProgressArgs.TITLE, null));
                    progressViewModel.setTopEndText(MessageFormat.format("{0} / {1}"
                            , (args.getInt(ProgressArgs.TASK_INDEX, 0) + 1)
                            , args.getInt(ProgressArgs.TASK_COUNT, 0)));
                    //progressBar.setProgress(0);
                    //titleTextWeakRef.get().setText(args.getString(ProgressArgs.TITLE, null));
                    //topEndTextWeakRef.get().setText(MessageFormat.format("{0} / {1}"
                    //       , (args.getInt(ProgressArgs.TASK_INDEX, 0) + 1)
                    //       , args.getInt(ProgressArgs.TASK_COUNT, 0)));
                    break;
                case ProgressCode.DONE:
                    progressViewModel.setProgress(101);
                    //progressBar.setProgress(100);
                    break;
            }
        }
    }

    @Override
    protected Bundle doInBackground(@NotNull String... tableNames) {

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

        final Gson gson = gsonBuilder.create();


        StringBuilder stringBuilder = new StringBuilder();
        final Context context = contextWeakReference.get();

        RequestQueue requestQueue = Volley.newRequestQueue(contextWeakReference.get());

        final SharedPreferences sharedPreferences = context.getSharedPreferences(StarfangConstants.SHARED_PREF_STORE, Context.MODE_PRIVATE);
        final String uid = FirebaseAuth.getInstance().getUid();
        final String id = sharedPreferences.getString(StarfangConstants.PREF_ID_KEY, null);
        String stringKey;
        try {
            stringKey = CipherUtils.getSecureSharedPreferences(context)
                    .getString(StarfangConstants.SECURE_PREF_SECRET_KEY, null);
        } catch (GeneralSecurityException | IOException e) {
            stringKey = null;
        }

        if (uid == null || id == null || stringKey == null) {
            return null;
        }

        int count = tableNames.length;
        stringBuilder.append(count).append("actionable tasks: ");
        int executedCount = 0;
        Bundle resultArgs = new Bundle();
        for (int i = 0; i < count; i++) {
            if (isCancelled()) {
                break;
            }

            Log.d(TAG, i + "--------------------------------------");
            String tableName = tableNames[i];
            Bundle args = new Bundle();
            args.putInt(ProgressArgs.PROGRESS_CODE, ProgressCode.START);
            args.putInt(ProgressArgs.TASK_INDEX, i);
            args.putString(ProgressArgs.TITLE, tableName);
            args.putInt(ProgressArgs.TASK_COUNT, count);
            publishProgress(args);
            final String stringIv = sharedPreferences.getString(StarfangConstants.PREF_IV_KEY, null);
            boolean success = false;
            if (stringIv != null) {
                try {
                    JSONObject echo = postTable(
                            i, id, stringKey, stringIv, uid,
                            requestQueue,
                            context,
                            tableName
                            , context.getString(R.string.url_sync_default) + context.getString(R.string.php_transaction));

                    if (echo != null) {

                        //Log.d(TAG, "echo: " + echo);
                        //Log.d(TAG, "str length: " + echo.toString().length() );
                        try {
                            String message = echo.getString(Echo.MESSAGE);
                            Log.d(TAG, "message: " + message);
                        } catch (JSONException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }


                        try {

                            String status = echo.getString(Echo.STATUS);
                            switch (status) {
                                case Status.SUCCESS:
                                    String newIv = echo.getString(Echo.NEW_IV);
                                    if (sharedPreferences.edit().putString(StarfangConstants.PREF_IV_KEY, newIv).commit()) {

                                        String modelName = tableName.substring(0, 1).toUpperCase() + tableName.substring(1);
                                        Log.d(TAG, "model: " + modelName);
                                        try {
                                            Class<? extends RealmObject> realmObjectClass = Class.forName(StarfangConstants.REALM_MODEL_SOURCE + modelName)
                                                    .asSubclass(RealmObject.class);
                                            JSONArray tuples = echo.getJSONArray(Echo.TUPLES);
                                            long lastModified = echo.getInt(Echo.LAST_MODIFIED) * 1000L;
                                            Log.d(TAG, "timestamp in millis: " + lastModified);

                                            try (Realm realm = Realm.getDefaultInstance()) {
                                                realm.beginTransaction();

                                                // todo: make progress ui for each tuple
                                                ArrayList<Integer> ids = new ArrayList<>();
                                                for (int j = 0; j < tuples.length(); j++) {
                                                    JSONObject tuple = tuples.getJSONObject(j);
                                                    RealmObject updateObj = gson.fromJson(tuple.toString(), realmObjectClass);
                                                    realm.copyToRealmOrUpdate(updateObj);

                                                    if (updateObj instanceof Source) {
                                                        ids.add(((Source) updateObj).getId());
                                                    }

                                                    //if (updateObj instanceof SearchNameWithoutBlank) {
                                                    //    Log.d(TAG, "nwb complete");
                                                   //     ((SearchNameWithoutBlank) updateObj).setNameWithoutBlank();
                                                    //}
                                                }

                                                TableList tableInfo = realm.where(TableList.class).equalTo(TableList.FIELD_TABLE, tableName).findFirst();
                                                if (tableInfo == null) {
                                                    tableInfo = realm.createObject(TableList.class, tableName);
                                                }
                                                tableInfo.setLastModified(lastModified);
                                                Log.d(TAG, tuples.length() + "record(s) updated");
                                                Calendar calendar = Calendar.getInstance();
                                                calendar.setTimeInMillis(lastModified);
                                                Log.d(TAG, "record(s) updated" + calendar.getTime());
                                                realm.commitTransaction();


                                                Bundle doneArgs = new Bundle();
                                                doneArgs.putInt(ProgressArgs.PROGRESS_CODE, ProgressCode.DONE);
                                                publishProgress(doneArgs);
                                                success = true;
                                                executedCount++;
                                                resultArgs.putIntegerArrayList(modelName, ids);
                                            } catch (RuntimeException e) {
                                                Log.e(TAG, Log.getStackTraceString(e));
                                            }
                                        } catch (ClassNotFoundException e) {
                                            Log.e(TAG, Log.getStackTraceString(e));
                                        }
                                    }
                                    break;
                                case Status.FAIL:

                            }

                        } catch (JSONException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }

                } catch (GeneralSecurityException | InterruptedException | ExecutionException | TimeoutException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, Log.getStackTraceString(e));
                Thread.currentThread().interrupt();
            }

            if (!success) {
                Bundle errorArgs = new Bundle();
                errorArgs.putInt(ProgressArgs.PROGRESS_CODE, ProgressCode.ERROR);
                publishProgress(errorArgs);
            }

        }

        stringBuilder.append(executedCount).append(" executed, ")
                .append(count - executedCount).append(" error occurred");


        resultArgs.putString(ResultArgs.RESULT_SUMMARY, stringBuilder.toString());
        return resultArgs;
    }


    private JSONObject postTable(int index, String id, String stringKey, String stringIv, String uid, RequestQueue requestQueue, Context context, final String tableName, String url) throws GeneralSecurityException, InterruptedException, ExecutionException, TimeoutException {

        final String eeUid = Base64.encodeToString(
                CipherUtils.encrypt(
                        context.getString(R.string.cipher_algorithm)
                        , context.getString(R.string.cipher_instance)
                        , stringKey
                        , uid.getBytes(StandardCharsets.UTF_8)
                        , Base64.decode(stringIv, Base64.DEFAULT)
                ), Base64.DEFAULT);

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();

        //Log.d(TAG, "url:" + url);

        Map<String, String> params = new HashMap<>();
        params.put(Params.ID, id);
        params.put(Params.EE_UID, eeUid);
        params.put(Params.TABLE_NAME, tableName);
        params.put(Params.LAST_MODIFIED, getTableLastModified(tableName));
        params.put(Params.TRANSACTION, String.valueOf(StarfangConstants.TRANSACTION_DOWNLOAD_JSON));

        ProgressRequest request = new ProgressRequest(
                Request.Method.POST, url, new JSONObject(params)
                , requestFuture
                , requestFuture, (transferredBytes, totalSize) -> {
            Bundle progArgs = new Bundle();
            progArgs.putInt(ProgressArgs.TASK_INDEX, index);
            progArgs.putInt(ProgressArgs.PROGRESS_CODE, ProgressCode.PROGRESS);
            progArgs.putLong(ProgressArgs.BYTE_DONE, transferredBytes);
            progArgs.putLong(ProgressArgs.BYTE_TOTAL, totalSize);
            publishProgress(progArgs);

        });

        //Log.d(TAG, "request:" + new String(request.getBody()));

        requestQueue.add(request);
        return requestFuture.get(20, TimeUnit.SECONDS);
    }

    private String getTableLastModified(String tableName) {

        long lastModified = 0L;

        try (Realm realm = Realm.getDefaultInstance()) {
            TableList tableList = realm.where(TableList.class)
                    .equalTo(TableList.FIELD_TABLE, tableName, Case.INSENSITIVE).findFirst();
            if (tableList != null) {
                lastModified = tableList.getLastModified();
            }
        } catch (RuntimeException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return String.valueOf(lastModified);
    }




}


/*
 DynamicRealm dynamicRealm = DynamicRealm.getInstance(realm.getConfiguration());
                                                dynamicRealm.beginTransaction();
                                                RealmSchema schema = dynamicRealm.getSchema();
                                                if (!schema.contains(modelName)) {
                                                    schema.createWithPrimaryKeyField(modelName, "id", int.class);
                                                }

                                                for (int j = 0; j < tuples.length(); j++) {
                                                    JSONObject tuple = tuples.getJSONObject(j);
                                                    Iterator<String> keyIt = tuple.keys();
                                                    int tupleId = tuple.getInt("id");
                                                    DynamicRealmObject dynamicObj = dynamicRealm.where(modelName).equalTo("id", tupleId).findFirst();
                                                    if (dynamicObj == null) {
                                                        dynamicObj = dynamicRealm.createObject(modelName, tupleId);
                                                    }
                                                    while (keyIt.hasNext()) {
                                                        try {
                                                            String key = keyIt.next();
                                                            if (!key.equals("id")) {
                                                                Object value = tuple.get(key);
                                                                if( value instanceof Integer ) {
                                                                    dynamicObj.setInt(key, (Integer) value);

                                                                } else if( value instanceof JSONArray ) {
                                                                    RealmList<RealmInteger> list = new RealmList<>();
                                                                    for( int k = 0 ; k < ((JSONArray) value).length(); k++ ) {
                                                                        Object element = ((JSONArray) value).get(k);
                                                                        if( element instanceof Integer ) {
                                                                            list.add(new RealmInteger((Integer)element));
                                                                        } else {
                                                                            if (j == 0)
                                                                                Log.d(TAG, "unknown array key: " + key + ", value: " + value );
                                                                        }
                                                                    }
                                                                    if( list.size() > 0 ) {
                                                                        dynamicObj.setList(key, list);
                                                                    }

                                                                } else if (value instanceof String) {
                                                                    dynamicObj.setString(key, String.valueOf(value));
                                                                } else {
                                                                    if (j == 0)
                                                                        Log.d(TAG, "? key: " + key + ", value: " + value );
                                                                }



                                                            }
                                                        } catch (JSONException ignore) {

                                                        }
                                                    }

                                                }

                                                dynamicRealm.commitTransaction();

 */