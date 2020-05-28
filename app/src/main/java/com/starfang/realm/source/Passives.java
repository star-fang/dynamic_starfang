package com.starfang.realm.source;

import com.starfang.realm.primitive.RealmPair;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Passives extends RealmObject {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_NAME2 = "name2";
    public static final String FIELD_NAME_WITHOUT_BLANK = "nameWithoutBlank";

    /*
    Primitive fields
     */
    @PrimaryKey
    private int id;
    @Index
    private String name;
    private String desc;
    private String remark;
    @Index
    private String name2;
    private int accumulate;
    private int triggerTileValue;
    private int triggerType;
    private int parentId;
    private String icon;
    private RealmList<RealmPair> tileAdvs;

    /*
    Runtime fields
     */
    @Index
    private String nameWithoutBlank;
    private Tiles triggerTile;

    /*
    methods
     */
    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getRemark() {
        return remark;
    }

    public String getName2() {
        return name2;
    }

    public int getAccumulate() {
        return accumulate;
    }

    public int getTriggerTileValue() {
        return triggerTileValue;
    }

    public int getTriggerType() {
        return triggerType;
    }

    public int getParentId() {
        return parentId;
    }

    public String getIcon() {
        return icon;
    }

    public RealmList<RealmPair> getTileAdvs() {
        return tileAdvs;
    }

    public String getNameWithoutBlank() {
        return nameWithoutBlank;
    }

    public Tiles getTriggerTile() {
        return triggerTile;
    }

    public void setNameWithoutBlank(String nameWithoutBlank) {
        this.nameWithoutBlank = nameWithoutBlank;
    }

    public void setTriggerTile(Tiles triggerTile) {
        this.triggerTile = triggerTile;
    }

    public int getId() {
        return id;
    }
}
