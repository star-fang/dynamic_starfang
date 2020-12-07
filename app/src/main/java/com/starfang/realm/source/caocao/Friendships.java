package com.starfang.realm.source.caocao;

import android.text.TextUtils;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Friendships extends RealmObject implements Source, SearchNameWithoutBlank {

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
    private RealmList<RealmInteger> passiveIds; // passiveList ids
    private RealmList<RealmInteger> passiveUnitCount;
    private RealmList<RealmInteger> passiveType; // 0 : 0명이상 출진, 1:퇴각시 2: 인접시
    private int needString;
    private RealmList<RealmInteger> statType;
    private RealmList<RealmInteger> statVal;

    /*
    Runtime fields
     */
    @Index
    private String nameWithoutBlank;
    private RealmList<Units> unitList;
    private RealmList<PassiveList> passiveLists;

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

    public RealmList<PassiveList> getPassiveLists() {
        return passiveLists;
    }

    public void setUnitList(RealmList<Units> unitList) {
        this.unitList = unitList;
    }

    public void setUnitList(List<Units> unitArrayList) {
        if (unitArrayList != null) {
            this.unitList = new RealmList<>();
            this.unitList.addAll(unitArrayList);
        }
    }


    public void setPassiveList(List<PassiveList> passiveArrayList) {
        if (passiveArrayList != null) {
            this.passiveLists = new RealmList<>();
            this.passiveLists.addAll(passiveArrayList);
        }
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

    @Override
    public void setNameWithoutBlank() {
        if (name != null) {
            this.nameWithoutBlank = name.replaceAll("\\s+", "");
        }
    }

    public List<PassiveList> checkActive(List<Integer> idList) {
        List<PassiveList> list = new ArrayList<>();
        for (int i = 0; i < passiveLists.size(); i++) {
            PassiveList passive = passiveLists.get(i);
            RealmInteger unitCountRealmInt = passiveUnitCount.get(i);
            RealmInteger passiveTypeRealmInt = passiveType.get(i);
            if (unitCountRealmInt != null && passiveTypeRealmInt != null) {
                if (passiveTypeRealmInt.getValue() == 0) {
                    int matchCount = 0;
                    for (RealmInteger idRealmInt : unitIds) {
                        if (idRealmInt != null && idList.contains(idRealmInt.getValue())) {
                            matchCount++;
                        }
                    }
                    int unitCount = unitCountRealmInt.getValue();
                    if ((unitCount == 0 && matchCount == unitIds.size())
                            || (unitCount > 0 && unitCount <= matchCount)) {
                        list.add(passive);
                    }
                }
            }
        }
        return list;
    }
}
