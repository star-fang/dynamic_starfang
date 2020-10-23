package com.starfang.utilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.starfang.realm.primitive.RealmDouble;
import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.primitive.RealmString;

import org.apache.commons.lang3.math.NumberUtils;

import java.lang.reflect.Type;

import io.realm.RealmList;

public class RealmSyncUtils {

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
            if(  jsonElement.isJsonNull() ) {
                return "";
            }

            try {
                return jsonElement.getAsString();
            } catch (IllegalStateException e ) {
                return jsonElement.toString();
            }

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

    public static class RealmDoubleDeserializer implements
            JsonDeserializer<RealmList<RealmDouble>> {

        @Override
        public RealmList<RealmDouble> deserialize(JsonElement json, Type typeOfT,
                                                   JsonDeserializationContext context) throws JsonParseException {

            RealmList<RealmDouble> realmDoubles = new RealmList<>();
            JsonArray stringList = json.getAsJsonArray();

            for (JsonElement doubleElement : stringList) {
                realmDoubles.add(new RealmDouble(getNullAsZeroDouble(doubleElement)));
            }

            return realmDoubles;
        }

        private double getNullAsZeroDouble(JsonElement jsonElement) {
            String valueStr = jsonElement.isJsonNull() ? "" : jsonElement.getAsString();
            return NumberUtils.toDouble(valueStr, 0d);
        }
    }

}
