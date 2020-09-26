package com.starfang.realm.source.rok;

import com.starfang.realm.source.Source;

import java.text.MessageFormat;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Civilizations extends RealmObject implements Source {

    public static final String FIELD_COMMENT = "comment";
    public static final String FIELD_COMMANDER_ID = "initCommanderId";
    public static final String FIELD_BONUS1 = "bonus1";
    public static final String FIELD_BONUS1_VAL = "bonus1Val";
    public static final String FIELD_BONUS2 = "bonus2";
    public static final String FIELD_BONUS2_VAL = "bonus2Val";
    public static final String FIELD_BONUS3 = "bonus3";
    public static final String FIELD_BONUS3_VAL = "bonus3Val";
    public static final String FIELD_SPECIAL_UNIT = "specialUnit";

    @PrimaryKey
    private int id;
    private String name;
    private String comment;
    private int initCommanderId;
    private String bonus1;
    private int bonus1Val;
    private String bonus2;
    private int bonus2Val;
    private String bonus3;
    private int bonus3Val;
    private String specialUnit;


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
            case FIELD_COMMENT:
                return comment;
            case FIELD_SPECIAL_UNIT:
                return specialUnit;
            case FIELD_BONUS1:
                return MessageFormat.format(bonus1,bonus1Val);
            case FIELD_BONUS2:
                return MessageFormat.format(bonus2,bonus2Val);
            case FIELD_BONUS3:
                return MessageFormat.format(bonus3,bonus3Val);
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {
        switch ( field ) {
            case FIELD_ID:
                return id;
            case FIELD_COMMANDER_ID:
                return initCommanderId;
            default:
                return 0;
        }
    }
}
