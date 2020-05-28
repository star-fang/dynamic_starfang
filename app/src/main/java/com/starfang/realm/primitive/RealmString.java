package com.starfang.realm.primitive;

import androidx.annotation.NonNull;

import io.realm.RealmObject;

public class RealmString extends RealmObject {

    public static final String VALUE = "value";
    private String value;

    public RealmString(){}

    public RealmString(String value){
        this.value =  value;
    }

    @NonNull
    @Override
    public String toString() {
        return value;
    }

}
