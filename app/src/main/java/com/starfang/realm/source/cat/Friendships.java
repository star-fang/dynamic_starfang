package com.starfang.realm.source.cat;

import android.text.TextUtils;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

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

    private interface PASSIVE_TYPE_CODES {
        int PARTICIPATE = 0;
        int RETREAT = 1;
        int ADJACENT = 2;
    }

    private static final String[] STAT_TYPE = {"공격력", "정신력", "방어력", "순발력", "사기"};

    @PrimaryKey
    private int id;
    @Index
    private String name;
    private RealmList<RealmInteger> unitIds;
    private RealmList<RealmInteger> passiveIds; // passiveList ids
    private RealmList<RealmInteger> passiveUnitCount;
    private RealmList<RealmInteger> passiveType; // 0 : 0명이상 출진, 1:퇴각시 2: 인접시
    private int needString;
    private RealmList<RealmInteger> statType; // 0:공격력, 1:정신력, 2:방어력, 3:순발력, 4:사기
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

    public void setPassiveLists(RealmList<PassiveList> passiveLists) {
        this.passiveLists = passiveLists;
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

    public boolean activated(int unitCount) {
        if (unitList != null) {
            int minimumCount = unitList.size();
            for (int i = 0; i < passiveType.size(); i++) {
                if (i < passiveUnitCount.size()) {
                    RealmInteger type = passiveType.get(i);
                    if (type != null && type.getValue() == PASSIVE_TYPE_CODES.PARTICIPATE) {
                        RealmInteger count = passiveUnitCount.get(i);
                        if (count != null && count.getValue() != 0 && count.getValue() < minimumCount) {
                            minimumCount = count.getValue();
                        }
                    }
                }
            }
            return unitCount >= minimumCount;
        }
        return false;
    }

    public String getInfo(int unitCount) {
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append(name);

        if (unitCount <= 0 && statType != null && statVal != null) {
            infoBuilder
                    .append("\r\n인연의 끈: ").append(needString)
                    .append("\r\n지속 효과: ");
            for (int i = 0; i < statType.size(); i++) {
                if (i < statVal.size()) {
                    RealmInteger type = statType.get(i);
                    RealmInteger value = statVal.get(i);
                    if (type != null && value != null) {
                        infoBuilder.append(i > 0 ? ", " : "")
                                .append(STAT_TYPE[type.getValue()]).append("+").append(value.getValue());
                    }

                }
            }
        } // if detail

        if (unitList != null && passiveType != null && passiveLists != null && passiveUnitCount != null) {
            infoBuilder.append("\r\n");
            for( int i = 0; i < unitList.size(); i++ ) {
                Units unit = unitList.get(i);
                infoBuilder.append(i > 0 ? ", ": "").append(unit.getString(Source.FIELD_NAME));
                if( unit.getInt(Units.FIELD_COUNT_NAMESAKE) > 1 ) {
                    UnitTypes type = unit.getType();
                    if( type != null ) {
                        infoBuilder.append("(").append(type.getString(Source.FIELD_NAME)).append(")");
                    }
                }
            }
            for (int i = 0; i < passiveType.size(); i++) {
                if (i < passiveUnitCount.size() && i < passiveLists.size()) {
                    RealmInteger type = passiveType.get(i);
                    PassiveList passiveList = passiveLists.get(i);
                    if (type != null && passiveList != null) {
                        switch (type.getValue()) {
                            case PASSIVE_TYPE_CODES.PARTICIPATE:
                                RealmInteger countObj = passiveUnitCount.get(i);
                                if (countObj != null) {
                                    int count = countObj.getValue();
                                    String criteria = count > 0 ? count + "명 이상 출전: " : "모두 출전: ";
                                    count = count == 0 ? unitList.size() : count;
                                    infoBuilder.append("\r\n").append(criteria)
                                            .append(passiveList.getPassiveAndVal()).append(unitCount > 0 ? (unitCount < count ? " [x]" : " [o]") : "");
                                }
                                break;
                            case PASSIVE_TYPE_CODES.ADJACENT:
                                infoBuilder.append("\r\n").append("인접: ").append(passiveList.getPassiveAndVal());
                                break;
                            case PASSIVE_TYPE_CODES.RETREAT:
                                infoBuilder.append("\r\n").append("퇴각: ").append(passiveList.getPassiveAndVal());
                                break;
                        }

                    }
                }
            }
        }

        return infoBuilder.toString();

    }
}
