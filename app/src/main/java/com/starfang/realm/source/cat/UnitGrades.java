package com.starfang.realm.source.cat;

import android.text.TextUtils;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.Source;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class UnitGrades extends RealmObject implements Source {

    public static final String FIELD_GRADE = "grade";
    public static final String FIELD_TYPE_PASV_VALS = "typePassiveVals";
    public static final String FIELD_MOVING = "moving";
    public static final String FIELD_EFFECT_AREA_ID = "effectArea";
    public static final String FIELD_TARGET_AREA_ID = "targetArea";

    @PrimaryKey
    private int id;
    @Index
    private String name;
    private int grade; // 1~5
    private RealmList<RealmInteger> typePassiveVals; //[0,0,0]
    private int moving;
    private int effectArea;
    private int targetArea;

    public RealmList<RealmInteger> getTypePassiveVals() {
        return typePassiveVals;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getString(String field) {
        switch (field) {
            case FIELD_ID:
                return String.valueOf(id);
            case FIELD_NAME:
                return name;
            case FIELD_GRADE:
                return String.valueOf(grade);
            case FIELD_TYPE_PASV_VALS:
                return typePassiveVals == null ? null : TextUtils.join(", ", typePassiveVals);
            case FIELD_MOVING:
                return String.valueOf(moving);
            case FIELD_EFFECT_AREA_ID:
                return String.valueOf(effectArea);
            case FIELD_TARGET_AREA_ID:
                return String.valueOf(targetArea);
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {
        switch (field) {
            case FIELD_ID:
                return id;
            case FIELD_GRADE:
                return grade;
            case FIELD_MOVING:
                return moving;
            case FIELD_EFFECT_AREA_ID:
                return effectArea;
            case FIELD_TARGET_AREA_ID:
                return targetArea;
            default:
                return -1;
        }
    }

    public int getGrade() {
        return grade;
    }
}
