package com.starfang.realm.primitive;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmPair extends RealmObject {

    public static final String FIELD_KEY = "key";
    public static final String FIELD_VALUE = "value";

    @PrimaryKey
    private int key;
    private int value;

    public int getKey() {
        return key;
    }

    public int getValue() {
        return value;
    }

}
