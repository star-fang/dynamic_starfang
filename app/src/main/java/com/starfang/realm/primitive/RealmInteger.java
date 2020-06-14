package com.starfang.realm.primitive;

import androidx.annotation.NonNull;

import io.realm.RealmObject;

public class RealmInteger extends RealmObject {
    public static final String VALUE = "value";
    private int value;

    public RealmInteger(){}

    public RealmInteger(int value){
        this.value =  value;
    }

    public int getValue() { return value; }

    public void setValue(int value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
