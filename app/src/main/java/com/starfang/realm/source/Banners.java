package com.starfang.realm.source;

import com.starfang.realm.Source;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Banners extends RealmObject implements Source {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_NAME_WITHOUT_BLANK = "nameWithoutBlank";

    @PrimaryKey
    private int id;
    @Index
    private String name;

    /*
    Runtime Fields
     */
    @Index
    private String nameWithoutBlank;

    /*
    methods
     */
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
            case FIELD_NAME_WITHOUT_BLANK:
                return nameWithoutBlank;
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {
        if( field.equals(FIELD_ID) ) {
            return id;
        } else {
            return -1;
        }
    }

    public void setNameWithoutBlank(String nameWithoutBlank) {
        this.nameWithoutBlank = nameWithoutBlank;
    }
}
