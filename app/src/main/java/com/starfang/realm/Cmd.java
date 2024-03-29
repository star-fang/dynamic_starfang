package com.starfang.realm;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;

public class Cmd extends RealmObject {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TEXT = "text";
    public static final String FIELD_WHEN = "when";


    @PrimaryKey
    private long id;

    @Index
    private String name;
    private String text;

    private boolean isUser;
    @Index
    private long when;

    public long getId() {return id;}

    public Cmd() throws RealmPrimaryKeyConstraintException {
        this.id = UUID.randomUUID().getMostSignificantBits();
        this.isUser = true;
        this.when = System.currentTimeMillis();
    }


    public Cmd( boolean isUser ) throws RealmPrimaryKeyConstraintException {
        this.id = UUID.randomUUID().getMostSignificantBits();
        this.isUser = isUser;
        this.when = System.currentTimeMillis();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getWhen() {
        return when;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }

}
