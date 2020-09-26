package com.starfang.realm.source.rok;

import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Skills extends RealmObject implements Source, SearchNameWithoutBlank {

    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_PROPERTY = "property";

    @PrimaryKey
    private int id;

    private String name;

    private String property;

    private String description;

    //runtime filed
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
        switch( field ) {
            case FIELD_ID:
                return String.valueOf(id);
            case FIELD_NAME:
                return name;
            case FIELD_NAME_WITHOUT_BLANK:
                return nameWithoutBlank;
            case FIELD_PROPERTY:
                return property;
            case FIELD_DESCRIPTION:
                return description;
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {
        return 0;
    }
}
