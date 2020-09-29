package com.starfang.realm.transaction.rok;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.starfang.StarfangConstants;
import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.utilities.RealmSyncUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class ReadJsonFileTask extends AsyncTask<String, Bundle, Bundle> {

    private static final String TAG = "FANG_READ";

    private WeakReference<Context> contextWeakReference;

    public ReadJsonFileTask( Context context ) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected void onPostExecute(Bundle bundle) {
        super.onPostExecute(bundle);
        Log.d(TAG, "ROK Read json complete");
        LinkingTask linkingTask = new LinkingTask(contextWeakReference.get());
        linkingTask.execute();
    }

    @Override
    protected Bundle doInBackground(String... strings) {

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


        String jsonString;
        try {
            InputStream jsonInputStream = contextWeakReference.get().getAssets().open(strings[0]);
            int size = jsonInputStream.available();

            byte[] buffer = new byte[size];
            int a = jsonInputStream.read(buffer);
            Log.d(TAG, a + " / " + size );
            jsonInputStream.close();

            jsonString = new String(buffer, StandardCharsets.UTF_8);


            JSONArray jsonArray = new JSONArray(jsonString);
            for( int i = 0; i < jsonArray.length(); i++ ) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if( jsonObject.getString("type").equals("table")) {
                    String modelName = StringUtils.capitalize(jsonObject.getString("name"));

                    Class<? extends RealmObject> realmObjectClass = Class.forName(StarfangConstants.REALM_MODEL_SOURCE_ROK + modelName)
                            .asSubclass(RealmObject.class);

                    JSONArray tuples = jsonObject.getJSONArray("data");

                    try(Realm realm = Realm.getDefaultInstance()) {
                        for (int j = 0; j < tuples.length(); j++) {
                            JSONObject tuple = tuples.getJSONObject(j);
                            realm.beginTransaction();
                            String tupleStr = tuple.toString();
                                    //.replace("\"[","[").replace("]\"","]");
                            //Log.d(TAG,tupleStr);
                            try {
                                RealmObject realmObject = gson.fromJson(tupleStr, realmObjectClass);
                                if (realmObject != null) {
                                    if( realmObject instanceof SearchNameWithoutBlank ) {
                                        ((SearchNameWithoutBlank) realmObject).setNameWithoutBlank();
                                    }

                                    realm.copyToRealmOrUpdate(realmObject);
                                }
                            } catch( IllegalStateException e ) {
                                Log.e(TAG,Log.getStackTraceString(e));
                            }
                            realm.commitTransaction();
                        }
                    }

                }
            }
        } catch (IOException | JSONException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch( RuntimeException e ) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }
}
