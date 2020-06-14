package com.starfang.realm.source;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class PassiveList extends RealmObject implements Source {

    public static final String FIELD_PASV_ID = "passiveId";
    public static final String FIELD_PASSIVE = "passive";
    public static final String FIELD_VAL = "val";
    public static final String FIELD_UNIT_LEVEL = "unitLevel";


    /*
    Primitive fields
     */
    @PrimaryKey
    private int id;
    @Index
    private int passiveId;
    @Index
    private int val;

    /*
    Runtime fields
     */
    private Passives passive;
    private int unitLevel; // 30,50,70,90


    /*
    methods
     */
    public void setPassive(Passives passive) {
        this.passive = passive;
    }

    public void setUnitLevel(int level) {
        this.unitLevel = level;
    }

    public Passives getPassive() {
        return passive;
    }

    public int getPassiveId() {
        return passiveId;
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
            case FIELD_PASV_ID:
                return String.valueOf(passiveId);
            case FIELD_VAL:
                return String.valueOf(val);
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {
        switch (field) {
            case FIELD_ID:
                return id;
            case FIELD_PASV_ID:
                return passiveId;
            case FIELD_VAL:
                return val;
            case FIELD_UNIT_LEVEL:
                return unitLevel;
            default:
                return -1;
        }
    }
}
