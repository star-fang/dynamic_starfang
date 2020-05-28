package com.starfang.realm;

import android.os.AsyncTask;
import android.util.Log;

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
import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.primitive.RealmString;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class LinkingRealmTask extends AsyncTask<JSONArray, String, String> {

    private static final String TAG = "FANG_LINKING";
    private static final String KEY_ID = "id";
    private static final String KEY_FLAG = "flag";
    private static final String KEY_DATA = "data";
    private static final int FLAG_UPDATE = 0;
    private static final int FLAG_DELETE = 1;
    private static final String DB_PACKAGE = "com.starfang.realm.source.";

    private String tableName; // unit_types
    private String modelName; // UnitTypes
    private long lastModified;
    private Gson gson;

    LinkingRealmTask(String tableName, long lastModified) {
        StringBuilder sb = new StringBuilder();
        for (String s : tableName.replaceAll("\\s+", "_").split("_")) {
            sb.append(StringUtils.capitalize(s));
        }
        this.modelName = sb.toString(); // unit_types -> UnitTypes
        this.tableName = tableName;
        this.lastModified = lastModified;


        GsonBuilder gsonBuilder = new GsonBuilder()
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
        }.getType(), new RealmStringDeserializer());

        gsonBuilder.registerTypeAdapter(new TypeToken<RealmList<RealmInteger>>() {
        }.getType(), new RealmIntegerDeserializer());

        gson = gsonBuilder.create();
    }

    @Override
    protected String doInBackground(JSONArray... arrays) {

        Class<? extends RealmObject> realmObjectClass;
        try {
            realmObjectClass = Class.forName(DB_PACKAGE + modelName)
                    .asSubclass(RealmObject.class);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return null;
        }

        Realm realm = Realm.getDefaultInstance();
        TableList tableList = realm.where(TableList.class).equalTo(TableList.FIELD_TABLE, tableName, Case.INSENSITIVE).findFirst();
        realm.beginTransaction();
        if (tableList == null) {
            tableList = realm.createObject(TableList.class);
            tableList.setTable(tableName);
        }
        tableList.setLastModified(lastModified);

        final JSONArray capsules = arrays[0];
        for (int i = 0; i < capsules.length(); i++) {
            try {
                JSONObject capsule = capsules.getJSONObject(i);
                switch (capsule.getInt(KEY_FLAG)) {
                    case FLAG_DELETE:
                        RealmObject deleteObj = realm.where(realmObjectClass).equalTo(KEY_ID, capsule.getInt(KEY_ID)).findFirst();
                        if (deleteObj != null) {
                            deleteObj.deleteFromRealm();
                        }
                        break;
                    case FLAG_UPDATE:
                        RealmObject updateObj = gson.fromJson(capsule.getJSONObject(KEY_DATA).toString(), realmObjectClass);
                        realm.copyToRealmOrUpdate(updateObj);
                        break;
                    default:
                }
            } catch (JSONException | NullPointerException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

        }


        realm.commitTransaction();
        realm.close();


        return null;
    }

    public static class RealmStringDeserializer implements
            JsonDeserializer<RealmList<RealmString>> {

        @Override
        public RealmList<RealmString> deserialize(JsonElement json, Type typeOfT,
                                                  JsonDeserializationContext context) throws JsonParseException {

            RealmList<RealmString> realmStrings = new RealmList<>();
            JsonArray stringList = json.getAsJsonArray();

            for (JsonElement stringElement : stringList) {
                realmStrings.add(new RealmString(getNullAsEmptyString(stringElement)));
            }

            return realmStrings;
        }

        private String getNullAsEmptyString(JsonElement jsonElement) {
            return jsonElement.isJsonNull() ? "" : jsonElement.getAsString();
        }
    }


    public static class RealmIntegerDeserializer implements
            JsonDeserializer<RealmList<RealmInteger>> {

        @Override
        public RealmList<RealmInteger> deserialize(JsonElement json, Type typeOfT,
                                                   JsonDeserializationContext context) throws JsonParseException {

            RealmList<RealmInteger> realmIntegers = new RealmList<>();
            JsonArray stringList = json.getAsJsonArray();

            for (JsonElement integerElement : stringList) {
                realmIntegers.add(new RealmInteger(getNullAsZeroInt(integerElement)));
            }

            return realmIntegers;
        }

        private int getNullAsZeroInt(JsonElement jsonElement) {
            String valueStr = jsonElement.isJsonNull() ? "" : jsonElement.getAsString();
            return NumberUtils.toInt(valueStr, 0);
        }
    }
}
