package com.starfang.realm.source.rok;

import com.starfang.realm.source.Source;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Rarity extends RealmObject implements Source {

    @PrimaryKey
    private int id;
    @Index
    private String name;
    private String nameEng;
    private int val;


    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getString(String field) {
        switch (field ) {
            case FIELD_NAME:
                return name;
            case FIELD_NAME_ENG:
                return nameEng;
        }
        return null;
    }

    @Override
    public int getInt(String field) {
        return 0;
    }
}
