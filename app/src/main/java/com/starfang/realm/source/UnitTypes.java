package com.starfang.realm.source;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.primitive.RealmMap;
import com.starfang.realm.primitive.RealmPair;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class UnitTypes extends RealmObject {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_NAME2 = "name2";

    /*
    Primitive fields
     */
    @PrimaryKey
    private int id;
    @Index
    private String name;
    private int aktLv;
    private int wisLv;
    private int defLv;
    private int agiLv;
    private int mrlLv;
    private RealmList<RealmInteger> unitPassiveListIds;
    private RealmList<RealmInteger> typePassiveIds;
    private RealmList<RealmInteger> typePassiveGrades;
    private RealmList<RealmInteger> gradeIds;
    @Index
    private String name2;
    private RealmList<RealmPair> tactics; // tacticId & lv
    private RealmList<RealmMap> tiles; // tileId & [adv,move]
    private RealmList<RealmPair> counters; // typeId & value
    private int hpPlus;
    private int mpPlus;
    private int canPincers;
    private RealmList<RealmInteger> itemType;

    /*
    Runtime fields
     */
    private RealmList<PassiveList> unitPassiveLists;
    private RealmList<Passives> typePassiveList;
    private RealmList<UnitGrades> gradeList;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAktLv() {
        return aktLv;
    }

    public int getWisLv() {
        return wisLv;
    }

    public int getDefLv() {
        return defLv;
    }

    public int getAgiLv() {
        return agiLv;
    }

    public int getMrlLv() {
        return mrlLv;
    }

    public RealmList<RealmInteger> getUnitPassiveListIds() {
        return unitPassiveListIds;
    }

    public RealmList<RealmInteger> getTypePassiveIds() {
        return typePassiveIds;
    }

    public RealmList<RealmInteger> getTypePassiveGrades() {
        return typePassiveGrades;
    }

    public RealmList<RealmInteger> getGradeIds() {
        return gradeIds;
    }

    public String getName2() {
        return name2;
    }

    public RealmList<RealmPair> getTactics() {
        return tactics;
    }

    public RealmList<RealmMap> getTiles() {
        return tiles;
    }

    public RealmList<RealmPair> getCounters() {
        return counters;
    }

    public int getHpPlus() {
        return hpPlus;
    }

    public int getMpPlus() {
        return mpPlus;
    }

    public int getCanPincers() {
        return canPincers;
    }

    public RealmList<RealmInteger> getItemType() {
        return itemType;
    }

    /*
    Runtime Field's Methods
     */
    public RealmList<PassiveList> getUnitPassiveLists() {
        return unitPassiveLists;
    }

    public void setUnitPassiveLists(RealmList<PassiveList> unitPassiveLists) {
        this.unitPassiveLists = unitPassiveLists;
    }

    public RealmList<Passives> getTypePassiveList() {
        return typePassiveList;
    }

    public void setTypePassiveList(RealmList<Passives> typePassiveList) {
        this.typePassiveList = typePassiveList;
    }

    public RealmList<UnitGrades> getGradeList() {
        return gradeList;
    }

    public void setGradeList(RealmList<UnitGrades> gradeList) {
        this.gradeList = gradeList;
    }
}
