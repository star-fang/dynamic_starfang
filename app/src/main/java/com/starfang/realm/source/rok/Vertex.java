package com.starfang.realm.source.rok;

import com.starfang.realm.source.Source;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Vertex extends RealmObject implements Source {

    public static final String FIELD_X = "x";
    public static final String FIELD_Y = "y";
    public static final String FIELD_VC = "vc";

    @PrimaryKey
    private int id;
    private int x;
    private int y;
    private int vc;

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
            case FIELD_X: return x;
            case FIELD_Y: return y;
            case FIELD_VC: return vc;
            default: return -1;
        }
    }
}
