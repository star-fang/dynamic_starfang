package com.starfang.realm.source;

import com.starfang.realm.primitive.RealmInteger;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Friendships extends RealmObject {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_NAME_WITHOUT_BLANK = "nameWithoutBlank";
    public static final String FIELD_UNIT_LIST = "unitList";
    public static final String FIELD_PASSIVE_LIST  = "passiveList";

    @PrimaryKey
    private int id;
    @Index
    private String name;
    private RealmList<RealmInteger> unitIds;
    private RealmList<RealmInteger> passiveIds;
    private RealmList<RealmInteger> passiveUnitCount;
    private RealmList<RealmInteger> passiveType; // 0 : 0명이상 동반 출진, 1:? 2:?
    private int needString;
    private RealmList<RealmInteger> statType;
    private RealmList<RealmInteger> statVal;

    /*
    Runtime fields
     */
    @Index
    private String nameWithoutBlank;
    private RealmList<Units> unitList;
    private RealmList<Passives> passiveList;

    public String getName() {
        return name;
    }

    public RealmList<RealmInteger> getUnitIds() {
        return unitIds;
    }

    public RealmList<RealmInteger> getPassiveIds() {
        return passiveIds;
    }

    public RealmList<RealmInteger> getPassiveUnitCount() {
        return passiveUnitCount;
    }

    public RealmList<RealmInteger> getPassiveType() {
        return passiveType;
    }

    public int getNeedString() {
        return needString;
    }

    public RealmList<RealmInteger> getStatType() {
        return statType;
    }

    public RealmList<RealmInteger> getStatVal() {
        return statVal;
    }

    public String getNameWithoutBlank() {
        return nameWithoutBlank;
    }

    public RealmList<Units> getUnitList() {
        return unitList;
    }

    public RealmList<Passives> getPassiveList() {
        return passiveList;
    }

    public void setNameWithoutBlank(String nameWithoutBlank) {
        this.nameWithoutBlank = nameWithoutBlank;
    }

    public void setUnitList(RealmList<Units> unitList) {
        this.unitList = unitList;
    }

    public void setPassiveList(RealmList<Passives> passiveList) {
        this.passiveList = passiveList;
    }
}
