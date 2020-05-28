package com.starfang.realm.source;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Tiles extends RealmObject {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_NAME2 = "name2";

    @PrimaryKey
    private int id;
    @Index
    private String name;
    @Index
    private String name2;

    public String getName() {
        return name;
    }

    public String getName2() {
        return name2;
    }
}
