package com.starfang.realm.source.rok;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.Source;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Vertex extends RealmObject implements Source {

    public static final int VC_GATE = 0;
    public static final int VC_BAULUR = 1;
    public static final int VC_KEEP = 2;
    public static final String FIELD_X = "x";
    public static final String FIELD_Y = "y";
    public static final String FIELD_VC = "vc";
    public static final String FIELD_SERVER = "server";
    public static final String FIELD_NAME_ID = "nameId";
    public static final String FIELD_DEADLINE = "deadline";
    public static final String FIELD_TIME_LIMIT = "timeLimit";
    public static final String FIELD_LANDS = "lands";
    public static final String FIELD_LAND_IDS = "landIds";

    @PrimaryKey
    private int id;
    private int x;
    private int y;
    private int vc; // 0 : 관문, 1 : 브롤, 2 : 요새
    private int server; // bound while reading json
    private RealmList<RealmInteger> landIds;
    private int nameId;

    private RokName name;
    private RealmList<Land> lands;

    private long deadline;
    private long timeLimit;

    public RokName getName() {
        return name;
    }

    public void setName(RokName name) {
        this.name = name;
    }

    public RealmList<RealmInteger> getLandIds() {
        return landIds;
    }

    public RealmList<Land> getLands() {
        return lands;
    }

    public void setLands(RealmList<Land> lands) {
        this.lands = lands;
    }

    public void setDeadline(Long deadline) {
        this.deadline = deadline == null ? 0L : deadline;
    }

    public long getDeadline() {
        return deadline;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Long timeLimit) {
        this.timeLimit = timeLimit == null ? 0L : timeLimit;
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
        switch ( field ) {
            case FIELD_X: return x;
            case FIELD_Y: return y;
            case FIELD_VC: return vc;
            case FIELD_SERVER: return server;
            case FIELD_NAME_ID: return nameId;
            default: return -1;
        }
    }
}
