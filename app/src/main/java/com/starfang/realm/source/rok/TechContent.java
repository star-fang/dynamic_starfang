package com.starfang.realm.source.rok;

import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class TechContent extends RealmObject implements Source, SearchNameWithoutBlank {

    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_CATEGORY_KOR = "categoryKor";
    public static final String FIELD_NAME_ENG = "nameEng";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TIER = "tier";
    public static final String FIELD_FACTS = "facts";
    public static final String FIELD_DESCRIPTION = "description";

    @PrimaryKey
    private int id;

    private String category; // economic or military
    private String nameEng;

    @Index
    private String name;
    private int tier;
    private RealmList<RealmString> facts;
    private String description;

    // runtime fields
    private String nameWithoutBlank;
    private String categoryKor; // 경제, 군사

    public RealmList<RealmString> getFacts() {
        return facts;
    }

    @Override
    public void setNameWithoutBlank() {
        this.nameWithoutBlank = name.replaceAll("\\s+","").trim();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getString(String field) {
        switch (field) {
            case FIELD_NAME:
                return name;
            case FIELD_NAME_WITHOUT_BLANK:
                return nameWithoutBlank;
            case FIELD_NAME_ENG:
                return nameEng;
            case FIELD_CATEGORY:
                return category;
            case FIELD_CATEGORY_KOR:
                return categoryKor;
            case FIELD_DESCRIPTION:
                return description;
            default:
                return null;
        }

    }

    @Override
    public int getInt(String field) {
        switch (field) {
            case FIELD_ID:
                return id;
            case FIELD_TIER:
                return tier;
            default:
                return 0;
        }
    }
}
