package com.starfang.realm.source.rok;

import com.starfang.realm.source.Source;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RokName extends RealmObject implements Source {

    public static final String FIELD_ENG = "eng";
    public static final String FIELD_KOR = "kor";

    @PrimaryKey
    private int id;
    private String eng;
    private String kor;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getString(String field) {
        switch ( field ) {
            case FIELD_ENG:
                return eng;
            case FIELD_KOR:
                return kor;
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {
      return 0;
    }
}
