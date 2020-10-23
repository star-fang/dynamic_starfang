package com.starfang.realm.primitive;

import androidx.annotation.NonNull;

import io.realm.RealmObject;

public class RealmDouble extends RealmObject {
    public static final String VALUE = "value";
    private double value;

    public RealmDouble(){}

    public RealmDouble(double value){
        this.value =  value;
    }

    public double getValue() { return value; }

    public void setValue(double value) {
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
