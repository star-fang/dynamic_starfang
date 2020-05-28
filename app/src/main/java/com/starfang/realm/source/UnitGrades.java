package com.starfang.realm.source;

import com.starfang.realm.primitive.RealmInteger;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class UnitGrades extends RealmObject {

    public static final String FIELD_NAME = "name";

    @PrimaryKey
    private int id;
    @Index
    private String name;
    private int grade; // 1~5
    private RealmList<RealmInteger> typePassiveVals; //[0,0,0]
    private int moving;
    private int effectArea;
    private int targetArea;

    public String getName() {
        return name;
    }

    public int getGrade() {
        return grade;
    }

    public RealmList<RealmInteger> getTypePassiveVals() {
        return typePassiveVals;
    }

    public int getMoving() {
        return moving;
    }

    public int getEffectArea() {
        return effectArea;
    }

    public int getTargetArea() {
        return targetArea;
    }
}
