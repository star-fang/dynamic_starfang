package com.starfang.realm.source.rok;

import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Specifications extends RealmObject implements Source, SearchNameWithoutBlank {

    public static final String FIELD_POSITION = "position";

    @PrimaryKey
    private int id;
    private String name;
    private int position;

    private String nameWithoutBlank;

    @Override
    public void setNameWithoutBlank() {
        this.nameWithoutBlank = name.replaceAll("\\s+","").trim();
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
            case FIELD_NAME_WITHOUT_BLANK:
                return nameWithoutBlank;
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {

        switch ( field ) {
            case FIELD_ID:
                return id;
            case FIELD_POSITION:
                return position;
            default:
                return 0;
        }
    }
}
