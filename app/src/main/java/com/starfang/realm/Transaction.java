package com.starfang.realm;

import android.os.AsyncTask;

public abstract class Transaction<PARAMS, PROGRESS, RESULT> extends AsyncTask<PARAMS, PROGRESS, RESULT> {
    protected interface Echo {
        String NEW_IV = "newIv";
        String STATUS = "status";
        String MESSAGE = "message";
        String TUPLES = "tuples";
        String TABLES = "tables";
        String TABLE_NAME = "table";
        String TUPLES_COUNT = "count";
        String TUPLES_SIZE = "size";
        String LAST_MODIFIED = "lastModified";
    }

    protected interface Params {
        String ID = "id";
        String EE_UID = "ee_uid";
        String TRANSACTION = "transaction";
        String TABLE_LIST = "table_list";
        String TABLE_NAME = "table";
        String LAST_MODIFIED = "last_modified";
    }

    protected interface Status {
        String SUCCESS = "succ";
        String FAIL_DECRYPTION = "fail_dec";
        String FAIL = "fail";
    }

    protected interface Linking {
        String modelName = "model";
        String idList = "idList";
    }






}
