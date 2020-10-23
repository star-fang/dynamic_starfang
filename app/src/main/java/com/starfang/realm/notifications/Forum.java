package com.starfang.realm.notifications;

import com.starfang.utilities.UnicodeTextUtils;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;

public class Forum extends RealmObject {

    public static final String FIELD_ID = "id";
    public static final String FIELD_TAG = "tag";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_PACKAGE_NAME = "packageName";
    public static final String FIELD_CONVERSATION_LIST = "conversationList";
    public static final String FIELD_LAST_MODIFIED = "lastModified";


    @PrimaryKey
    private long id;
    @Index
    private String tag;
    @Index
    private String name;
    @Index
    private long lastModified;

    private boolean isGroupChat;

    private String packageName;

    private RealmList<Conversation> conversationList;

    private int nonReadCount;

    private String lastSimpleConversation;

    public Forum() throws RealmPrimaryKeyConstraintException {
        this.id = UUID.randomUUID().getMostSignificantBits();
        this.conversationList = new RealmList<>();
        this.nonReadCount = 0;
        this.lastSimpleConversation = null;
    }

    public Forum(String tag) throws RealmPrimaryKeyConstraintException {
        this.id = UUID.randomUUID().getMostSignificantBits();
        this.tag = tag;
        this.conversationList = new RealmList<>();
        this.nonReadCount = 0;
        this.lastSimpleConversation = null;
    }

    public long getId() {
        return id;
    }

    public String getLastSimpleConversation() {
        return lastSimpleConversation;
    }

    public String getNonReadCount() {
        return String.valueOf(nonReadCount);
    }

    public void countUpNonRead() {
        this.nonReadCount++;
    }

    public void InitializeNonRead() {
        this.nonReadCount = 0;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setGroupChatOption(boolean isGroupChat) {
        this.isGroupChat = isGroupChat;
    }

    public boolean isGroupChat() {
        return isGroupChat;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addConversation(Conversation conversation) {
        this.conversationList.add(conversation);
        if (conversation != null) {
            String content = conversation.getContent();
            if (content != null && content.length() > 5) {
                content = UnicodeTextUtils.subText(content, 0, 5, "...");
            }
            this.lastSimpleConversation = content;
            this.lastModified = conversation.getWhen();
        }
    }

    public RealmList<Conversation> getConversationList() {
        return conversationList;
    }

    public void deleteConversation(long conversationId) {
        for (Conversation conversation : conversationList) {
            if (conversation.getId() == conversationId) {
                conversationList.remove(conversation);
            }
        }
    }


}
