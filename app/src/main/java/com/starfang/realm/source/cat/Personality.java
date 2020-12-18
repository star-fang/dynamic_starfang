package com.starfang.realm.source.cat;

import com.starfang.realm.source.Source;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Personality extends RealmObject implements Source {

    @PrimaryKey
    private int id;
    @Index
    private String name;


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
