package com.starfang.realm.source.cat;

import android.text.TextUtils;

import com.starfang.realm.primitive.RealmSet;
import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Passives extends RealmObject implements Source, SearchNameWithoutBlank {

    public static final String FIELD_ACCUMULATE = "accumulate";
    public static final String FIELD_TRIGGER_TILE_ID = "triggerTileValue";
    public static final String FIELD_TRIGGER_TYPE = "triggerType";
    public static final String FIELD_PARENT_ID = "parentId";
    public static final String FIELD_ICON = "icon";
    public static final String FIELD_TILE_ADVS = "tileAdvs";

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
    private RealmList<RealmSet> tileAdvs;

    /*
    Runtime fields
     */
    @Index
    private String nameWithoutBlank;
    private Tiles triggerTile;

    /*
    methods
     */

    public RealmList<RealmSet> getTileAdvs() {
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

    public int getTriggerTileValue() {
        return triggerTileValue;
    }

    public void setTriggerTile(Tiles triggerTile) {
        this.triggerTile = triggerTile;
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
            case FIELD_NAME2:
                return name2;
            case FIELD_DESCRIPTION:
                return desc;
            case FIELD_REMARK:
                return remark;
            case FIELD_NAME_WITHOUT_BLANK:
                return nameWithoutBlank;
            case FIELD_ACCUMULATE:
                return String.valueOf(accumulate != 0);
            case FIELD_TRIGGER_TILE_ID:
                return String.valueOf(triggerTileValue);
            case FIELD_TRIGGER_TYPE:
                return String.valueOf(triggerType);
            case FIELD_PARENT_ID:
                return String.valueOf(parentId);
            case FIELD_ICON:
                return icon;
            case FIELD_TILE_ADVS:
                return tileAdvs == null ? null : TextUtils.join(", ", tileAdvs);
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {
        switch (field) {
            case FIELD_ID:
                return id;
            case FIELD_ACCUMULATE:
                return accumulate;
            case FIELD_TRIGGER_TILE_ID:
                return triggerTileValue;
            case FIELD_TRIGGER_TYPE:
                return triggerType;
            case FIELD_PARENT_ID:
                return parentId;
            default:
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
