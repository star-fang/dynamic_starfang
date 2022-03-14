package com.starfang.realm.source.rok;

import com.starfang.realm.primitive.RealmIntegerPair;
import com.starfang.realm.source.Source;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Land extends RealmObject implements Source {

    public static final String FIELD_NAME_ID = "nameId";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ZONE = "zone";
    public static final String FIELD_SERVER = "server";

    @PrimaryKey
    private int id;
    private int zone;
    private int nameId;
    private int server;
    private RealmIntegerPair center;
    private RealmList<RealmIntegerPair> boundary;

    // runtime fields
    private RokName name;

    public void setName(RokName name) {
        this.name = name;
    }

    public RokName getName() {
        return name;
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
        switch( field ) {
            case FIELD_NAME_ID:
                return nameId;
            case FIELD_ZONE:
                return zone;
            case FIELD_SERVER:
                return server;
            default:
                return 0;
        }
    }
}
