package com.starfang.realm.source;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Personality extends RealmObject {

    public static final String FIELD_NAME = "name";

    @PrimaryKey
    private int id;
    @Index
    private String name;

    public String getName() {
        return name;
    }
}
