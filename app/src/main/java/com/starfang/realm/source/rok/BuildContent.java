package com.starfang.realm.source.rok;

import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BuildContent extends RealmObject implements Source, SearchNameWithoutBlank {

    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_CATEGORY_KOR = "categoryKor";
    public static final String FIELD_NAME_ENG = "nameEng";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_FACTS = "facts";

    @PrimaryKey
    private int id;

    private String category; // economic or military or other
    private String nameEng;
    private String name;
    private RealmList<RealmString> facts;

    // runtime fields
    private String nameWithoutBlank;
    private String categoryKor; // 경제, 군사, 기타

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
            default:
                return null;
        }

    }

    @Override
    public int getInt(String field) {
        if (FIELD_ID.equals(field)) {
            return id;
        }
        return 0;
    }
}
