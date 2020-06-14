package com.starfang.realm.primitive;

import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmSet extends RealmObject {

    public static final String FIELD_KEY = "key";
    public static final String FIELD_VALUE = "value";

    @PrimaryKey
    private String key;
    private int value;

    public String getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }

    @NotNull
    @Override
    public String toString() {
        return MessageFormat.format("({0}: {1})", key, value);
    }

}
