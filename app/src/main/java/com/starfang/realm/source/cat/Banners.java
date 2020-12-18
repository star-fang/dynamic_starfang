package com.starfang.realm.source.cat;

import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Banners extends RealmObject implements Source, SearchNameWithoutBlank {

    public static final String FIELD_NAME = "name";

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

    @Override
    public void setNameWithoutBlank() {
        if( name != null ) {
            this.nameWithoutBlank = name.replaceAll("\\s+", "");
        }
    }
}
