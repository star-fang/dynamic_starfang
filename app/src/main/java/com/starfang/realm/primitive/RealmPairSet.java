package com.starfang.realm.primitive;

import java.text.MessageFormat;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmPairSet extends RealmObject {

    public static final String FIELD_KEY = "key";
    public static final String FIELD_VALUES = "values";

    @PrimaryKey
    private String key;
    private RealmIntegerPair values;

    public String getKey() {
        return key;
    }

    public RealmIntegerPair getValues() {
        return values;
    }

    @Override
    public String toString() {
        return MessageFormat.format("[{0}: {1}]", key, values.toString());
    }
}
