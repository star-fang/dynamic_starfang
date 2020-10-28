package com.starfang.realm;

import com.starfang.realm.source.SearchNameWithoutBlank;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Memo extends RealmObject implements SearchNameWithoutBlank {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_SEND_CAT = "sendCat";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_WHEN = "when";
    public static final String FIELD_FORUM_ID = "forumId";

    @PrimaryKey
    private String name;
    private String nameWithoutBlank;
    private String content;
    private String sendCat;
    private long forumId;
    private long when;

    public Memo() {

    }
    public Memo( String name, String content, String sendCat, long forumId) {
        this.name = name;
        this.when = System.currentTimeMillis();
        this.content = content;
        this.sendCat = sendCat;
        this.forumId = forumId;
        setNameWithoutBlank();
    }

    public String getName() {
        return name;
    }

    public String getNameWithoutBlank() {
        return nameWithoutBlank;
    }

    public String getContent() {
        return content;
    }

    public String getSendCat() {
        return sendCat;
    }

    public long getForumId() {
        return forumId;
    }

    public long getWhen() {
        return when;
    }

    public void setForumId(long forumId) {
        this.forumId = forumId;
    }

    public void setSendCat(String sendCat) {
        this.sendCat = sendCat;
    }


    public void setContent(String content) {
        this.content = content;
    }

    public void setWhen(long when) {
        this.when = when;
    }

    @Override
    public void setNameWithoutBlank() {
        this.nameWithoutBlank = name.replaceAll("\\s+", "");
    }

}
