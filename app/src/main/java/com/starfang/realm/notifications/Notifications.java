package com.starfang.realm.notifications;

import io.realm.RealmObject;
import io.realm.annotations.Index;

public class Notifications extends RealmObject {

    public static final String FIELD_SBN_ID = "sbnId";
    public static final String FIELD_PACKAGE = "appPackage";
    public static final String FIELD_IS_ACTIVE = "isActive";
    public static final String FIELD_CONVERSATION = "conversation";
    public static final String FIELD_WHEN = "when";

    @Index
    private int sbnId;
    private String appPackage;
    private boolean isActive;
    private String tag;
    private long when;

    public Notifications() {
        this.isActive = false;
    }

    public Notifications( int sbnId) {
        this.sbnId = sbnId;
        this.isActive = false;
    }

    public void setWhen( long when ) { this.when = when; }

    public long getWhen() { return  when; }

    public void setAppPackage( String appPackage ) {
        this.appPackage = appPackage;
    }

    public String getAppPackage() { return appPackage; }

    public void setTag( String tag ) { this.tag = tag; }

    public String getTag() { return tag; }

    public void activate() {this.isActive = true;}

    public void deactivate() {this.isActive = false;}

    public boolean isActive() { return  isActive; }

    public int getSbnId() { return sbnId; }

}
