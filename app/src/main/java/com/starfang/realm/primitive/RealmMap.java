package com.starfang.realm.primitive;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmMap extends RealmObject {

    public static final String FIELD_KEY = "key";
    public static final String FIELD_VALUES = "values";

    @PrimaryKey
    private int key;
    private RealmList<RealmInteger> values;

    public int getKey() {
        return key;
    }

    public RealmList<RealmInteger> getValues() {
        return values;
    }
}
