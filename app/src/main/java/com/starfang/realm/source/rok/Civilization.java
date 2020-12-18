package com.starfang.realm.source.rok;

import com.starfang.realm.primitive.RealmDouble;
import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.Source;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Civilization extends RealmObject implements Source {


    //{"id":"108","name":"한국","nameEng":"Korea"
    // ,"comment":"음양의 조화","commentEng":"Balance of Yin and Yang"
    // ,"initCommanderId":"17"
    // ,"attrIds":[1006, 1030, 1031]
    // ,"attrVals":[5, 15, 3]
    // ,"specialUnit":"화랑"},

    public static final String FIELD_COMMENT = "comment";
    public static final String FIELD_COMMENT_ENG = "commentEng";
    public static final String FIELD_COMMANDER_ID = "initCommanderId";
    public static final String FIELD_SPECIAL_UNIT = "specialUnit";
    public static final String FIELD_ATTR_IDS = "attrIds";
    public static final String FIELD_ATTR_VALS = "attrVals";
    public static final String FIELD_ATTRS = "attrs";

    @PrimaryKey
    private int id;
    private String name;
    private String nameEng;
    private String comment;
    private String commentEng;
    private int initCommanderId;
    private RealmList<RealmInteger> attrIds;
    private RealmList<RealmDouble> attrVals;
    private String specialUnit;

    private RealmList<Attribute> attrs;
    private Commander initCommander;

    public void setInitCommander(Commander initCommander) {
        this.initCommander = initCommander;
    }

    public void setAttrs(RealmList<Attribute> attrs) {
        this.attrs = attrs;
    }

    public RealmList<RealmInteger> getAttrIds() {
        return attrIds;
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
            case FIELD_NAME_ENG:
                return nameEng;
            case FIELD_COMMENT:
                return comment;
            case FIELD_COMMENT_ENG:
                return commentEng;
            case FIELD_SPECIAL_UNIT:
                return specialUnit;
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

    public String getInfo() {
        StringBuilder civil_info = new StringBuilder();

        civil_info
                .append(name).append("\r\n")
                .append("\"").append(comment).append("\"").append("\r\n\r\n");

        if (initCommander != null) {
            civil_info.append("초기 사령관: ").append(initCommander.getString(Source.FIELD_NAME)).append("\r\n");
        }

        if( attrs != null ) {
            for (int i = 0, number = 1; i < attrs.size(); i++) {
                Attribute attr = attrs.get(i);
                if (attr != null) {
                    RealmDouble valObj = attrVals == null ? null : attrVals.get(i);
                    civil_info.append("보너스").append(number).append(": ")
                            .append(attr.getFormWithValue(valObj == null ? null : valObj.getValue())).append("\r\n");
                    number++;
                }
            }
        }

        civil_info.append("특수 유닛: ").append(specialUnit);
        return civil_info.toString();
    }
}
