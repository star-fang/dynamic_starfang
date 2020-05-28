package com.starfang.realm;

import io.realm.RealmObject;

public class TableList extends RealmObject {

    public static final String FIELD_TABLE = "table";

    private String table;
    private long lastModified;

    public void setTable( String table ) {
        this.table = table;
    }

    public void setLastModified( long lastModified ) {
        this.lastModified = lastModified;
    }

    public long getLastModified() {
        return lastModified;
    }
}
