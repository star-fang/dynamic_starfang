package com.starfang.realm.source.cat;

import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Formula extends RealmObject implements Source, SearchNameWithoutBlank {

    @PrimaryKey
    private int id;

    @Index
    private String name;
    private String desc;

    @Index
    private String name2;

    /*
    runtime Fields
     */
    @Index
    private String nameWithoutBlank;

    /*
    methods
     */

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
            case FIELD_NAME2:
                return name2;
            case FIELD_NAME_WITHOUT_BLANK:
                return nameWithoutBlank;
            case FIELD_DESCRIPTION:
                return desc;
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
