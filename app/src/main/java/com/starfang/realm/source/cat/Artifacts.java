package com.starfang.realm.source.cat;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Artifacts extends RealmObject implements Source, SearchNameWithoutBlank {

    public static final String FIELD_GRADE = "grade";
    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_PASSIVES = "passives";
    private static final String[] STAT_NAMES = {"공격력", "정신력", "방어력", "순발력", "사기", "이동력"};

    @PrimaryKey
    private int id;
    @Index
    private String name;
    private String grade;
    private int categoryId;
    private int atk;
    private int wis;
    private int def;
    private int agi;
    private int mrl;
    private int mov;
    private RealmList<RealmInteger> passiveIds;
    private RealmList<RealmString> passiveVals;
    private String description;
    private RealmList<RealmInteger> unitId;
    private RealmList<RealmInteger> unitTypeIds;

    @Index
    private String nameWithoutBlank;
    private ArtifactsCate category;
    private RealmList<Passives> passives;
    private Units unit;
    private RealmList<UnitTypes> unitTypes;

    public void setCategory(ArtifactsCate category) {
        this.category = category;
    }

    public void setPassives(RealmList<Passives> passives) {
        this.passives = passives;
    }

    public void setUnit(Units unit) {
        this.unit = unit;
    }

    public void setUnitTypes(RealmList<UnitTypes> unitTypes) {
        this.unitTypes = unitTypes;
    }

    public RealmList<RealmInteger> getPassiveIds() {
        return passiveIds;
    }

    public RealmList<RealmInteger> getUnitId() {
        return unitId;
    }

    public RealmList<RealmInteger> getUnitTypeIds() {
        return unitTypeIds;
    }

    @Override
    public void setNameWithoutBlank() {
        if( name != null ) {
            this.nameWithoutBlank = name.replaceAll("\\s+", "");
        }
    }

    public String getSubjectInfo( int level ) {
        StringBuilder subjectBuilder = new StringBuilder();
        subjectBuilder.append("[").append(category.getString(ArtifactsCate.FIELD_CATE_SUB)).append("] ");
        if( level > 0 ) {
            subjectBuilder.append("+").append(level).append(" ");
        }
        subjectBuilder.append(name).append(" (").append( NumberUtils.isDigits(grade)? "★" + grade : grade).append(")");
        return subjectBuilder.toString();
    }

    public String getStatInfo( int level , Realm realm ) {
        StringBuilder statBuilder = new StringBuilder();
        int[] statArr = getStatArr();
        String[] statTypeArr = ( level > 0 && realm != null ) ? category.getStatTypeArr() : null;
        boolean statExisting = false;
        for( int i = 0; i < statArr.length; i++ ) {
            int statVal = statArr[i];
            int plusStat = 0;
            if( statTypeArr != null && i < statTypeArr.length ) {
                String statType = statTypeArr[i];
                if( statType != null ) {
                    Reinforcement reinforcement = realm.where(Reinforcement.class).equalTo(Reinforcement.FIELD_TYPE,statType, Case.INSENSITIVE)
                            .equalTo(Reinforcement.FIELD_GRADE, getGradeValue() ).findFirst();
                    if( reinforcement != null ) {
                        RealmInteger plusStatObj = reinforcement.getVal( level );
                        if( plusStatObj != null ) {
                            plusStat = plusStatObj.getValue();
                            statVal += plusStat;
                        }
                    }
                }
            } // if reinforcement
            if( statVal > 0 ) {
                statExisting = true;
                statBuilder.append("\r\n  ").append(STAT_NAMES[i]).append(": ")
                        .append(statVal);
                if( plusStat > 0 ) {
                    statBuilder.append(" (+").append(plusStat).append(")");
                }
            }
        } // for stats
        if( statExisting ) {
            return "\r\n*능력치"+statBuilder;
        }
        return "";
    }

    public String getPassivesInfo() {
        if (passives != null) {
            StringBuilder passiveBuilder = new StringBuilder();
            passiveBuilder.append("\r\n*보물 효과");
            if (unit != null) {
                passiveBuilder.append("(")
                        .append(unit.getString(Units.FIELD_TYPE)).append(" ").append(unit.getString(Source.FIELD_NAME)).append(")");
            }
            for (int i = 0; i < passives.size(); i++) {
                String passiveName = passives.get(i).getString(Source.FIELD_NAME);
                RealmString passiveValObj = i < passiveVals.size() ? passiveVals.get(i) : null;
                String passiveVal = passiveValObj != null ? passiveValObj.toString() : null;
                if (passiveName != null && passiveVal != null) {
                    if (passiveName.endsWith("%")) {
                        passiveName = passiveName.substring(0, passiveName.length() - 1) + " " + passiveVal + "%";
                    } else {
                        passiveName = passiveName + " " + passiveVal;
                    }
                }
                passiveBuilder.append("\r\n  ").append(passiveName);
            }
            return passiveBuilder.toString();
        }
        return "";
    }

    public String getWearRestrictionInfo() {

        RealmList<UnitTypes> restrictionList = unitTypes != null ? unitTypes : category.getUnitTypes();
        if( restrictionList != null ) {
            StringBuilder wearBuilder = new StringBuilder();
            wearBuilder.append("\r\n*착용 가능");
            for( int i = 0; i < restrictionList.size(); i++ ) {
                wearBuilder.append(i % 2 == 0 ? "\r\n  " : "   ")
                        .append(StringUtils.rightPad(restrictionList.get(i).getString(Source.FIELD_NAME),4,'　'));
            }
            return wearBuilder.toString();
        }
        return "";
    }

    private int getGradeValue() {
        switch( grade ) {
            case "연의":
                return 0;
            case "전용":
            case "제작":
                return 7;
            default:
                return NumberUtils.toInt(grade,7);
        }
    }

    private int[] getStatArr() {
        return new int[]{atk, wis, def, agi, mrl, mov};
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getString(String field) {
        return null;
    }

    @Override
    public int getInt(String field) {
        switch ( field ) {
            case FIELD_CATEGORY:
                return categoryId;
            case FIELD_GRADE:
                return getGradeValue();
            default:
                return -1;
        }
    }
}
