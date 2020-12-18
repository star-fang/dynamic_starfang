package com.starfang.realm.source.cat;

import android.text.TextUtils;

import com.starfang.realm.primitive.RealmIntegerPair;
import com.starfang.realm.source.Source;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class EffectArea extends RealmObject implements Source {

    public static final String FIELD_VECTOR = "vector";
    public static final String FIELD_POINTS = "points";
    public static final String FIELD_VALUE = "value";

    @PrimaryKey
    private int id;

    @Index
    private String name;
    private RealmList<RealmIntegerPair> points;
    private String value;
    private int vector; // 0 or 1

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getString(String field) {
        switch( field ) {
            case FIELD_ID:
                return String.valueOf(id);
            case FIELD_NAME:
                return name;
            case FIELD_POINTS:
                return points == null ? null : TextUtils.join(", ", points);
            case FIELD_VALUE:
                return value;
            case FIELD_VECTOR:
                return String.valueOf(vector != 0);
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {
        switch( field ) {
            case FIELD_ID:
                return id;
            case FIELD_VECTOR:
                return vector;
            default:
                return -1;
        }
    }
}
