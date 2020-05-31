package com.starfang.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TableList extends RealmObject {

    public static final String FIELD_TABLE = "table";

    @PrimaryKey
    private String table;
    private long lastModified;

    public void setTable(String table) {
        this.table = table;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getTableName() {
        return table;
    }
}
