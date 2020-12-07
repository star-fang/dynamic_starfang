package com.starfang.realm.simulator;

import com.starfang.realm.source.caocao.PassiveList;
import com.starfang.realm.source.caocao.UnitGrades;
import com.starfang.realm.source.caocao.Units;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;

public class UnitSim extends RealmObject implements Simulator {

    public static final int[] MAX_LEVEL_BY_GRADE = {20, 40, 60, 80, 99};
    public static final int[] MIN_LEVEL_BY_REINFORCE = {1, 7, 14, 21, 28, 35, 42, 49, 56, 63, 70, 77};
    public static final int[] MAX_LEVEL_BY_REINFORCE = {20, 20, 40, 40, 40, 60, 60, 60, 80, 80, 80, 99};

    public enum STAT_CODE {
        STR, INTEL, CMD, DEX, LCK
    }

    @PrimaryKey
    private int id;

    @Index
    private String alias;
    private Units unit;
    private int level; // 1 ~ 99
    private UnitGrades grade; // 1 ~ 5
    private int reinforcement; // 1 ~ 12
    private int strPlus;
    private int intelPlus;
    private int cmdPlus;
    private int dexPlus;
    private int lckPlus;
    private RealmList<PassiveList> checkedPassiveLists;
    private int atk;
    private int wis;
    private int def;
    private int agi;
    private int mrl;
    private int power;
    private int cost;

    private int maxPlusStatSum;
    private int maxPlusStat;

    private int plusStatSum;

    public UnitSim() {

    }

    public UnitSim(Units unit, String alias) throws RealmPrimaryKeyConstraintException {
        this.id = (int) UUID.randomUUID().getMostSignificantBits();
        this.alias = alias;
        this.unit = unit;
        this.level = 1;
        this.reinforcement = 1;
        setGrade(1);
    }

    public UnitSim(Units unit) throws RealmPrimaryKeyConstraintException {
        this.id = (int) UUID.randomUUID().getMostSignificantBits();
        this.alias = unit.getString(Units.FIELD_NAME) + "#" + id;
        this.unit = unit;
        this.level = 1;
        this.reinforcement = 1;
        setGrade(1);
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public Units getUnit() {
        return unit;
    }

    public int getId() {
        return id;
    }

    private void setGrade(int grade) {
        if (grade > 0 && grade <= MAX_LEVEL_BY_GRADE.length) {
            this.grade = unit.getType().getGrade(grade);
            this.cost = unit.getIntCostByGrade(grade);
            this.maxPlusStatSum = grade * 100;
            this.maxPlusStat = calcMaxPlusStat(grade);
            for (STAT_CODE code : STAT_CODE.values()) {
                if (getPlusStat(code) > this.maxPlusStat) {
                    setPlusStat(code, this.maxPlusStat);
                }
            }
        }
    }

    public int calcMaxPlusStat(int grade) {
        return grade < 5 ? MAX_LEVEL_BY_GRADE[grade - 1] :
                (unit.getIntCostByGrade(grade) + 6) * 5;
    }

    public int getMaxPlusStat() {
        return maxPlusStat;
    }

    public int getMaxPlusStatSum() {
        return maxPlusStatSum;
    }

    public int getPlusStat(STAT_CODE statCode) {
        switch (statCode) {
            case STR:
                return strPlus;
            case INTEL:
                return intelPlus;
            case CMD:
                return cmdPlus;
            case DEX:
                return dexPlus;
            case LCK:
                return lckPlus;
            default:
                return 0;
        }
    }

    private void setPlusStat(STAT_CODE statCode, int value) {
        switch (statCode) {
            case STR:
                this.strPlus = value;
                break;
            case INTEL:
                this.intelPlus = value;
                break;
            case CMD:
                this.cmdPlus = value;
                break;
            case DEX:
                this.dexPlus = value;
                break;
            case LCK:
                this.lckPlus = value;
                break;
        }
    }

    public int getCost() {
        return cost;
    }

    public int getLevel() {
        return level;
    }

    public int getReinforcement() {
        return reinforcement;
    }

    public UnitGrades getGrade() {
        return grade;
    }
}
