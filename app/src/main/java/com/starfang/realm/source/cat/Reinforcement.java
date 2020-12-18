package com.starfang.realm.source.cat;

import com.starfang.realm.primitive.RealmInteger;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Reinforcement extends RealmObject {

    public static final String FIELD_GRADE = "grade";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_VALS = "vals";

    private int grade;
    private String type;
    private RealmList<RealmInteger> vals;

    public int getGrade() {
        return grade;
    }

    public String getType() {
        return type;
    }

    public RealmInteger getVal( int level ) {
        if( vals != null && level > 0 && level <= vals.size() ) {
            return vals.get( level - 1 );
        }
        return null;
    }
}
