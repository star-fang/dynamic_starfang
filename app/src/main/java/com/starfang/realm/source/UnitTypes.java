package com.starfang.realm.source;

import android.text.TextUtils;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.primitive.RealmPairSet;
import com.starfang.realm.primitive.RealmSet;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class UnitTypes extends RealmObject implements Source {

    public static final String FIELD_ATK_LV = "atkLv";
    public static final String FIELD_WIS_LV = "wisLv";
    public static final String FIELD_DEF_LV = "defLv";
    public static final String FIELD_AGI_LV = "agiLv";
    public static final String FIELD_MRL_LV = "mrlLv";
    public static final String FIELD_UNIT_PASV_LIST_IDS = "unitPassiveListIds";
    public static final String FIELD_TYPE_PASV_IDS = "typePassiveIds";
    public static final String FIELD_TYPE_PASV_GRADES = "typePassiveGrades";
    public static final String FIELD_GRADE_IDS = "gradeIds";
    public static final String FIELD_TACTICS = "tactics";
    public static final String FIELD_TILES = "tiles";
    public static final String FIELD_COUNTERS = "counters";
    public static final String FIELD_HP_PLUS = "hpPlus";
    public static final String FIELD_MP_PLUS = "mpPlus";
    public static final String FIELD_CAN_PINCERS = "canPincers";
    public static final String FIELD_ITEM_TYPE = "itemType";
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
    private RealmList<RealmSet> tactics; // tacticId & lv
    private RealmList<RealmPairSet> tiles; // tileId & [adv,move]
    private RealmList<RealmSet> counters; // typeId & value
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


    public RealmList<RealmInteger> getTypePassiveIds() {
        return typePassiveIds;
    }

    public RealmList<RealmInteger> getGradeIds() {
        return gradeIds;
    }

    public RealmList<RealmInteger> getUnitPassiveListIds() {
        return unitPassiveListIds;
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
            case FIELD_ATK_LV:
                return String.valueOf(aktLv);
            case FIELD_WIS_LV:
                return String.valueOf(wisLv);
            case FIELD_DEF_LV:
                return String.valueOf(defLv);
            case FIELD_AGI_LV:
                return String.valueOf(agiLv);
            case FIELD_MRL_LV:
                return String.valueOf(mrlLv);
            case FIELD_UNIT_PASV_LIST_IDS:
                return unitPassiveListIds == null ? null : TextUtils.join(", ", unitPassiveListIds);
            case FIELD_TYPE_PASV_IDS:
                return typePassiveIds == null ? null : TextUtils.join(", ", typePassiveIds);
            case FIELD_TYPE_PASV_GRADES:
                return typePassiveGrades == null ? null : TextUtils.join(", ", typePassiveGrades);
            case FIELD_GRADE_IDS:
                return gradeIds == null ? null : TextUtils.join(", ", gradeIds);
            case FIELD_TACTICS:
                return tactics == null ? null : TextUtils.join(", ", tactics);
            case FIELD_TILES:
                return tiles == null ? null : TextUtils.join(", ", tiles);
            case FIELD_COUNTERS:
                return counters == null ? null : TextUtils.join(", ", counters);
            case FIELD_HP_PLUS:
                return String.valueOf(hpPlus);
            case FIELD_MP_PLUS:
                return String.valueOf(mpPlus);
            case FIELD_CAN_PINCERS:
                return String.valueOf(canPincers != 0);
            case FIELD_ITEM_TYPE:
                return itemType == null ? null : TextUtils.join(", ", itemType);
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {
        switch (field) {
            case FIELD_ID:
                return id;
            case FIELD_ATK_LV:
                return aktLv;
            case FIELD_WIS_LV:
                return wisLv;
            case FIELD_DEF_LV:
                return defLv;
            case FIELD_AGI_LV:
                return agiLv;
            case FIELD_MRL_LV:
                return mrlLv;
            case FIELD_HP_PLUS:
                return hpPlus;
            case FIELD_MP_PLUS:
                return mpPlus;
            case FIELD_CAN_PINCERS:
                return canPincers;
            default:
                return -1;
        }
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

    public void setUnitPassiveLists(List<PassiveList> unitPassivesList) {
        this.unitPassiveLists = new RealmList<>();
        this.unitPassiveLists.addAll(unitPassivesList);
    }

    public RealmList<Passives> getTypePassiveList() {
        return typePassiveList;
    }

    public void setTypePassiveList(RealmList<Passives> typePassiveList) {
        this.typePassiveList = typePassiveList;
    }

    public void setTypePassiveList(List<Passives> typePassivesList) {
        this.typePassiveList = new RealmList<>();
        this.typePassiveList.addAll(typePassivesList);
    }

    public UnitGrades getGrade( int grade ) {
        if( gradeList != null ) {
            return gradeList.where().equalTo(UnitGrades.FIELD_GRADE, grade).findFirst();
        }
        return null;
    }

    public RealmList<UnitGrades> getGradeList() {
        return gradeList;
    }

    public void setGradeList(RealmList<UnitGrades> gradeList) {
        this.gradeList = gradeList;
    }

    public void setGradeList(List<UnitGrades> gradesList) {
        this.gradeList = new RealmList<>();
        this.gradeList.addAll(gradesList);
    }
}
