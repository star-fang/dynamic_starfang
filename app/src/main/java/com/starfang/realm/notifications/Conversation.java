package com.starfang.realm.notifications;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;

public class Conversation extends RealmObject {

    public static final String FIELD_ID = "id";
    public static final String FIELD_SEND_CAT = "sendCat";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_WHEN = "when";
    public static final String FIELD_NOTIFICATION = "notification";


    @PrimaryKey
    private long id;

    @Index
    private String sendCat;
    private String content;
    @Index
    private long when;

    private Notifications notification;

    private boolean isMe;


    public long getId() {return id;}

    public Conversation() throws RealmPrimaryKeyConstraintException {
        this.id = UUID.randomUUID().getMostSignificantBits();
        this.isMe = false;

    }

    public Conversation(long id) throws RealmPrimaryKeyConstraintException {
        this.id = id;
        this.isMe = false;
    }

    public boolean isMe() {
        return isMe;
    }

    public void itIsMe() {
        this.isMe = true;
    }

    public void setSendCat(String sendCat) {
        this.sendCat = sendCat;
    }

    public String getSendCat() {
        return sendCat;
    }

    public void setWhen(long when) {
        this.when = when;
    }

    public long getWhen() {
        return when;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setNotification(Notifications notification) {
        this.notification = notification;
    }

    public Notifications getNotification() {
        return notification;
    }
}
