package com.starfang.realm.source;

import com.starfang.realm.Source;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Tiles extends RealmObject implements Source {

    @PrimaryKey
    private int id;
    @Index
    private String name;
    @Index
    private String name2;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getString(String field) {
        switch ( field ) {
            case FIELD_ID:
                return String.valueOf(id);
            case FIELD_NAME:
                return name;
            case FIELD_NAME2:
                return name2;
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
