package com.starfang.realm.source;

import android.text.TextUtils;

import com.starfang.realm.Source;
import com.starfang.realm.primitive.RealmInteger;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Friendships extends RealmObject implements Source {

    public static final String FIELD_UNIT_IDS = "unitIds";
    public static final String FIELD_PASV_IDS = "passiveIds";
    public static final String FIELD_PASV_UNIT_COUNT = "passiveUnitCount";
    public static final String FIELD_PASV_TYPE = "passiveType";
    public static final String FIELD_NEED_STRING = "needString";
    public static final String FIELD_STAT_TYPE = "statType";
    public static final String FIELD_STAT_VAL = "statVal";

    public static final String FIELD_UNIT_LIST = "unitList";
    public static final String FIELD_PASSIVE_LIST = "passiveList";

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

    public RealmList<RealmInteger> getStatType() {
        return statType;
    }

    public RealmList<RealmInteger> getStatVal() {
        return statVal;
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
            case FIELD_UNIT_IDS:
                return unitIds == null ? null : TextUtils.join(", ", unitIds);
            case FIELD_PASV_IDS:
                return passiveIds == null ? null : TextUtils.join(", ", passiveIds);
            case FIELD_PASV_UNIT_COUNT:
                return passiveUnitCount == null ? null : TextUtils.join(", ", passiveUnitCount);
            case FIELD_PASV_TYPE:
                return passiveType == null ? null : TextUtils.join(", ", passiveType);
            case FIELD_NEED_STRING:
                return String.valueOf(needString);
            case FIELD_STAT_TYPE:
                return statType == null ? null : TextUtils.join(", ", statType);
            case FIELD_STAT_VAL:
                return statVal == null ? null : TextUtils.join(", ", statVal);
            case FIELD_NAME_WITHOUT_BLANK:
                return nameWithoutBlank;
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {
        switch (field) {
            case FIELD_ID:
                return id;
            case FIELD_NEED_STRING:
                return needString;
            default:
                return -1;
        }
    }
}
