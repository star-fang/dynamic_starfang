package com.starfang.realm.source.cat;

import android.text.TextUtils;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class WarlordSkills extends RealmObject implements Source, SearchNameWithoutBlank {

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

    @Override
    public void setNameWithoutBlank() {
        if( name != null ) {
            this.nameWithoutBlank = name.replaceAll("\\s+", "");
        }
    }
}
