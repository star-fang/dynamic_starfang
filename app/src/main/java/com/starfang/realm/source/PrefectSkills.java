package com.starfang.realm.source;

import android.text.TextUtils;

import com.starfang.realm.Source;
import com.starfang.realm.primitive.RealmInteger;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class PrefectSkills extends RealmObject implements Source {

    public static final String FIELD_VALS = "vals";

    /*
    Primitive fields
     */
    @PrimaryKey
    private int id;
    @Index
    private String name;
    private String desc;
    private RealmList<RealmInteger> vals;

    /*
    Runtime fields
     */
    @Index
    private String nameWithoutBlank;

    public RealmList<RealmInteger> getVals() {
        return vals;
    }

    public void setNameWithoutBlank(String nameWithoutBlank) {
        this.nameWithoutBlank = nameWithoutBlank;
    }

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
            case FIELD_DESCRIPTION:
                return desc;
            case FIELD_NAME_WITHOUT_BLANK:
                return nameWithoutBlank;
            case FIELD_VALS:
                return vals == null ? null : TextUtils.join(", ", vals);
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {
        if( field.equals( FIELD_ID) ) {
            return id;
        }
        return -1;
    }
}
