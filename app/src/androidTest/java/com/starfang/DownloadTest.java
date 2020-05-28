package com.starfang;

import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DownloadTest{

    private static final String TAG = "FANG_DOWNLOAD";

    private static final int ECHO_EXECUTE = 0;
    private static final int ECHO_UP_TO_DATE = 1;
    private static final int ECHO_FAIL = -1;

    enum TaskCount { ALL, EXECUTE, UP_TO_DATE, FAIL}

    private static final String KEY_TABLE_NAME = "table";
    private static final String KEY_TABLE_COUNT = "count";

    private static final String KEY_ECHO = "echo";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_ARRAY = "array";


    interface PostCallback {
        void onSuccess(int echo, String message, List<JSONObject> capsuleList, String tableName, long tableCount);

        void onError( String tableName, String error);
    }

    @Test
    public void downloadTest() {
        doInBackground("localhost/fangcat/DownloadJson.php");
    }


    protected String doInBackground(String url, @NotNull String... tableNames) {

        final int[] taskCounts = new int[TaskCount.values().length];
        taskCounts[TaskCount.ALL.ordinal()] = tableNames.length;

        for (String tableName : tableNames) {
            postTable(
                    url,
                    // Unit types => unit_types
                    tableName.replaceAll("\\s+", "_").toLowerCase()
                    , new PostCallback() {
                        @Override
                        public void onSuccess(int echo, @Nullable String message, List<JSONObject> capsuleList, String tableName, long tableCount) {

                            switch (echo) {
                                case ECHO_EXECUTE:
                                    //ConvertToRealmTask convertToRealmTask = new ConvertToRealmTask(tableName, tableCount);
                                    //convertToRealmTask.doInBackground(capsuleList.toArray(new JSONObject[0]));
                                    taskCounts[TaskCount.EXECUTE.ordinal()]++;
                                    break;
                                case ECHO_UP_TO_DATE:
                                    taskCounts[TaskCount.UP_TO_DATE.ordinal()]++;
                                    break;
                                case ECHO_FAIL:
                                    taskCounts[TaskCount.FAIL.ordinal()]++;
                                    break;
                                default:
                            }

                            System.out.println(tableName + ": " + message);
                            //publishProgress(tableName, message);
                        }

                        @Override
                        public void onError(String tableName, String error) {
                            taskCounts[TaskCount.FAIL.ordinal()]++;
                            System.out.println(tableName + ": " + error);
                            //publishProgress(tableName, error);
                        }
                    });

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                //Log.e(TAG, Log.getStackTraceString(e));
                Thread.currentThread().interrupt();
            }


        }

        StringBuilder resultSb = new StringBuilder();
        resultSb.append(taskCounts[TaskCount.ALL.ordinal()]).append(" actionable tasks: ");
        int executeCount = taskCounts[TaskCount.EXECUTE.ordinal()];
        if(executeCount> 0) resultSb.append(executeCount).append( " executed");
        int upToDateCount = taskCounts[TaskCount.UP_TO_DATE.ordinal()];
        int failCount = taskCounts[TaskCount.FAIL.ordinal()];
        return null;
    }



    private void postTable(String url, final String tableName, PostCallback callback) {

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
                params.put(KEY_TABLE_NAME, tableName);
                params.put(KEY_TABLE_COUNT, getTableCount(tableName));
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
            JSONArray capsuleArray = echo.getJSONArray(KEY_ARRAY);
            List<JSONObject> capsuleList = new ArrayList<>();
            for( int i = 0; i < capsuleArray.length(); i++ ) {
                capsuleList.add(capsuleArray.getJSONObject(i));
            }
            callback.onSuccess(
                    echo.getInt(KEY_ECHO),
                    echo.getString(KEY_MESSAGE),
                    capsuleList,
                    echo.getString(KEY_TABLE_NAME).replaceAll("\\s+", "_").toLowerCase(),
                    echo.getLong(KEY_TABLE_COUNT)
            );
        } catch (InterruptedException | TimeoutException | ExecutionException | JSONException e) {
            callback.onError(tableName, e.getClass().getSimpleName());
        }
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
