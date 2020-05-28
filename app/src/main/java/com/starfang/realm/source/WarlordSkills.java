package com.starfang.realm.source;

import com.starfang.realm.primitive.RealmInteger;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class WarlordSkills extends RealmObject {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_NAME_WITHOUT_BLANK = "nameWithoutBlank";

    /*
    Primitive fields
     */
    @PrimaryKey
    private int id;
    @Index
    private String name;
    private String desc;
    private RealmList<RealmInteger> vals;

    /*
    Runtime fields
     */
    @Index
    private String nameWithoutBlank;

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public RealmList<RealmInteger> getVals() {
        return vals;
    }

    public String getNameWithoutBlank() {
        return nameWithoutBlank;
    }

    public void setNameWithoutBlank(String nameWithoutBlank) {
        this.nameWithoutBlank = nameWithoutBlank;
    }
}
