package com.starfang.realm;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TableList extends RealmObject {

    public static final String FIELD_TABLE = "table";
    public static final String FIELD_LAST_MODIFIED = "lastModified";
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

    public JSONObject toJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(FIELD_TABLE, table);
        jsonObject.put(FIELD_LAST_MODIFIED, lastModified);
        return jsonObject;
    }
}
