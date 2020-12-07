package com.starfang.realm.source.caocao;

import android.text.TextUtils;

import com.starfang.realm.primitive.RealmIntegerPair;
import com.starfang.realm.source.Source;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class TargetArea extends RealmObject implements Source {

    public static final String FIELD_POINTS = "points";
    public static final String FIELD_VALUE = "value";

    @PrimaryKey
    private int id;

    @Index
    private String name;
    private RealmList<RealmIntegerPair> points;
    private String value;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getString(String field) {
        switch (field) {
            case FIELD_NAME:
                return name;
            case FIELD_POINTS:
                return points == null ? null : TextUtils.join(", ", points);
            case FIELD_VALUE:
                return value;
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {
        if (FIELD_ID.equals(field)) {
            return id;
        }
        return -1;
    }
}
