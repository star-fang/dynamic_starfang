package com.starfang.realm.source;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Formula extends RealmObject {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_NAME2 = "name2";
    public static final String FIELD_NAME_WITHOUT_BLANK = "nameWithoutBlank";

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
    public String getName() {
        return name;
    }

    public String getName2() {
        return name2;
    }

    public String getDesc() {
        return desc;
    }

    public String getNameWithoutBlank() {
        return nameWithoutBlank;
    }

    public void setNameWithoutBlank(String nameWithoutBlank) {
        this.nameWithoutBlank = nameWithoutBlank;
    }
}