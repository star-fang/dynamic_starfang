package com.starfang.realm.source.cat;

import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Tactics extends RealmObject implements Source, SearchNameWithoutBlank {

    public static final String FIELD_MP = "mp";
    public static final String FIELD_EP = "ep";
    public static final String FIELD_TYPE = "skillType";
    public static final String FIELD_POWER = "skillPower";
    public static final String FIELD_ACCURACY = "maxAccu";
    public static final String FIELD_EFFECT_AREA_ID = "effectArea";
    public static final String FIELD_TARGET_AREA_ID = "targetArea";
    public static final String FIELD_DAMAGE_TYPE = "damageType";
    public static final String FIELD_HEAL_TYPE = "healType";
    public static final String FIELD_ACCU_TYPE = "accuType";
    public static final String FIELD_CAN_STREAK = "canStreakCase";
    public static final String FIELD_IS_OBSTRUCTIVE = "obstructiveSkill";
    public static final String FIELD_ICON = "icon";

    // primitive fields
    @PrimaryKey
    private int id;

    private int mp;
    private int ep;
    private int skillType;
    private int skillPower;
    private int maxAccu;
    private int effectArea;
    private int targetArea;
    private String damageType;
    private String healType;
    private String accuType;
    private int canStreakCast;
    private int obstructiveSkill;
    private String icon;
    private String name;
    private String desc;

    // runtime fields
    private String nameWithoutBlank;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getString(String field) {
        switch( field ) {
            case FIELD_ID:
                return String.valueOf(id);
            case FIELD_NAME:
                return name;
            case FIELD_DESCRIPTION:
                return desc;
            case FIELD_NAME_WITHOUT_BLANK:
                return nameWithoutBlank;
            case FIELD_MP:
                return String.valueOf(mp);
            case FIELD_EP:
                return String.valueOf(ep);
            case FIELD_TYPE:
                return String.valueOf(skillType);
            case FIELD_POWER:
                return String.valueOf(skillPower);
            case FIELD_ACCURACY:
                return String.valueOf(maxAccu);
            case FIELD_EFFECT_AREA_ID:
                return String.valueOf(effectArea);
            case FIELD_TARGET_AREA_ID:
                return String.valueOf(targetArea);
            case FIELD_DAMAGE_TYPE:
                return damageType;
            case FIELD_HEAL_TYPE:
                return healType;
            case FIELD_ACCU_TYPE:
                return accuType;
            case FIELD_CAN_STREAK:
                return String.valueOf(canStreakCast != 0);
            case FIELD_IS_OBSTRUCTIVE:
                return String.valueOf(obstructiveSkill != 0);
            case FIELD_ICON:
                return String.valueOf(icon);
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {
        switch ( field ) {
            case FIELD_ID:
                return id;
            case FIELD_MP:
                return mp;
            case FIELD_EP:
                return ep;
            case FIELD_TYPE:
                return skillType;
            case FIELD_POWER:
                return skillPower;
            case FIELD_ACCURACY:
                return maxAccu;
            case FIELD_EFFECT_AREA_ID:
                return effectArea;
            case FIELD_TARGET_AREA_ID:
                return targetArea;
            case FIELD_CAN_STREAK:
                return canStreakCast;
            case FIELD_IS_OBSTRUCTIVE:
                return obstructiveSkill;
            default:
                return -1;
        }
    }

    @Override
    public void setNameWithoutBlank() {
        if( name != null ) {
            this.nameWithoutBlank = name.replaceAll("\\s+", "");
        }
    }
}
