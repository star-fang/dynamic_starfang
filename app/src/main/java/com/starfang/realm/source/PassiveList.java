package com.starfang.realm.source;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class PassiveList extends RealmObject {

    public static final String FIELD_PASSIVE_ID = "passiveId";
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
    public void setPassive( Passives passive ) {
        this.passive = passive;
    }

    public void setUnitLevel( int level) {
        this.unitLevel = level;
    }

    public int getPassiveId() {return passiveId;}

    public Passives getPassive() {
        return passive;
    }

    public int getUnitLevel() {
        return unitLevel;
    }

    public int getVal() {
        return val;
    }
}
