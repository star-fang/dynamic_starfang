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

    protected interface ResultCode {
        int SUCCESS = 0;
        int RE_SIGN_IN = 1;
        int NULL_KEY = 3;
        int ERROR = 4;
    }

    protected interface ProgressCode {
        int START = 0;
        int DONE = 1;
        int PROGRESS = 2;
        int ERROR = 3;
    }

    protected interface ProgressArgs {
        String PROGRESS_CODE = "progressCode";
        String TASK_COUNT = "count";
        String TASK_INDEX = "index";
        String TITLE = "title";
        String BYTE_DONE = "byte_done";
        String BYTE_TOTAL = "byte_total";
    }


}
