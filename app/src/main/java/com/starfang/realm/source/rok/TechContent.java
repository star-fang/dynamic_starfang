package com.starfang.realm.source.rok;

import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TechContent extends RealmObject implements Source, SearchNameWithoutBlank {

    @PrimaryKey
    private int id;

    private String category; // economic or military
    private String nameEng;
    private String name;
    private String description;
    private int tier;

    // runtime fields
    private String nameWithoutBlank;

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
        return null;
    }

    @Override
    public int getInt(String field) {
        return 0;
    }
}
