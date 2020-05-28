package com.starfang;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class TableListTest{


    private static final String API_KEY = "MV5YZSXleCCsXNEG";
    private static final String DATABASE = "fangcat";
    private static final String KEY_API = "key";
    private static final String KEY_DB = "db";

    private static final String KEY_TABLE = "table";
    private static final String KEY_LAST_MODIFIED = "lastModified";


    @Test
    public void downloadTest() {
        doInBackground("https://192.168.0.7/fangcat/TableList.php");
    }


    private String doInBackground(String url) {

        RequestQueue requestQueue = Volley.newRequestQueue(InstrumentationRegistry.getInstrumentation().getContext());
        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, null,
                requestFuture, requestFuture) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }

            @Override
            public Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();
                params.put(KEY_API, API_KEY);
                params.put(KEY_DB, DATABASE);
                return params;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjectRequest);

        try {
            JSONObject echo = requestFuture.get(20, TimeUnit.SECONDS);
            Log.d("fangfang",echo.toString());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            //callback.onError(tableName, e.getClass().getSimpleName());
        }

        return null;
    }




    private String getTableCount(String tableName) {

        long count = 0L;

        /*
        Realm realm = Realm.getDefaultInstance();
        try {
            History history = realm.where(History.class)
                    .equalTo(History.FIELD_TABLE, tableName, Case.INSENSITIVE).findFirst();
            if (history != null) {
                count = history.getTableCount();
            }
        } catch (RuntimeException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            realm.close();
            Log.d(TAG, "realm instance closed");
        }
         */

        return String.valueOf(count);
    }
}
