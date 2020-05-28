package com.starfang.realm.source;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Banners extends RealmObject {

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
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNameWithoutBlank() {
        return nameWithoutBlank;
    }

    public void setNameWithoutBlank(String nameWithoutBlank) {
        this.nameWithoutBlank = nameWithoutBlank;
    }
}
