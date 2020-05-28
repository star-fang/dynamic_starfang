package com.starfang.realm.transaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.starfang.R;
import com.starfang.StarfangConstants;
import com.starfang.realm.TableList;
import com.starfang.realm.Transaction;
import com.starfang.utilities.CipherUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.realm.Realm;
import io.realm.RealmResults;

public class SyncTask extends Transaction<Void, Void, Pair<Integer, JSONArray>> {

    private static final String TAG = "FANG_SYNC";



    //private String url_check_table;
    private WeakReference<Context> contextWeakReference;

    public SyncTask(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected void onPostExecute(Pair<Integer, JSONArray> pair) {
        AlertDialog.Builder builder = new AlertDialog.Builder(contextWeakReference.get());
        builder.setCancelable(true);
        if (pair != null) {
            Integer resultCode = pair.first;
            if (resultCode != null) {
                switch (resultCode) {
                    case ResultCode.SUCCESS:
                        JSONArray tableJsonArray = pair.second;
                        if (pair.second != null) {
                            int sizeSum = 0;
                            List<String> tableList = new ArrayList<>();
                            for (int i = 0; i < tableJsonArray.length(); i++) {
                                try {
                                    JSONObject tableInfo = tableJsonArray.getJSONObject(i);
                                    String tableName = tableInfo.getString(Echo.TABLE_NAME);
                                    int count = tableInfo.getInt(Echo.TUPLES_COUNT);
                                    int size = tableInfo.getInt(Echo.TUPLES_SIZE);
                                    sizeSum += size;
                                    tableList.add(tableName);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            final String[] tables = tableList.toArray(new String[0]);

                            builder.setNeutralButton("자세한 내용 보기", null)
                                    .setTitle("다운로드")
                                    .setMessage(String.format(Locale.KOREA, "약 %d byte를 다운로드 합니다.", sizeSum))
                                    .setPositiveButton(R.string.start, (dialog, which) -> {
                                        new DownloadJsonTask(contextWeakReference.get()).execute(tables);
                                    });
                        }

                        break;
                    case ResultCode.RE_SIGN_IN:
                        builder.setTitle("암호화 실패")
                                .setMessage("암호화 모듈 복구 작업을 시작합니다.")
                                .setPositiveButton(R.string.start, (dialog, which) -> {

                                });
                        //todo : sign-out by force and request SignInActivity
                        break;
                    case ResultCode.NULL_KEY:
                        builder.setTitle("키 읽기 실패")
                                .setMessage("키 파일로 부터 키를 읽어옵니다.")
                                .setPositiveButton(R.string.start, (dialog, which) -> {

                                });
                        // todo : request SecureActivity
                        break;
                    case ResultCode.ERROR:
                        builder.setTitle("에러 발생")
                                .setMessage("뭐가 뭔지..");
                        // todo : show error message
                        break;
                }
            }

        }
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .create().show();


    }


    @Override
    protected Pair<Integer, JSONArray> doInBackground(Void... voids) {

        Pair<Integer, JSONArray> pair;
        String uid = FirebaseAuth.getInstance().getUid();
        Context context = contextWeakReference.get();
        SharedPreferences sharedPreferences = context.getSharedPreferences(StarfangConstants.SHARED_PREF_STORE, Context.MODE_PRIVATE);
        String id = sharedPreferences.getString(StarfangConstants.PREF_ID_KEY, null);
        String defaultUrl = context.getString(R.string.url_sync_default);
        String transactionPhp = context.getString(R.string.php_transaction);
        String algorithm = context.getString(R.string.cipher_algorithm);
        String cipherInstance = context.getString(R.string.cipher_instance);
        String stringIv = sharedPreferences.getString(StarfangConstants.PREF_IV_KEY, null);
        if (uid == null || id == null || stringIv == null) {
            pair = new Pair<>(ResultCode.RE_SIGN_IN, null);
        } else {
            try {
                String stringKey = CipherUtils.getSecureSharedPreferences(context)
                        .getString(StarfangConstants.SECURE_PREF_SECRET_KEY, null);

                if (stringKey == null) {
                    pair = new Pair<>(ResultCode.NULL_KEY, null);
                } else {
                    final String eeUid = Base64.encodeToString(
                            CipherUtils.encrypt(
                                    algorithm
                                    , cipherInstance
                                    , stringKey
                                    , uid.getBytes(StandardCharsets.UTF_8)
                                    , Base64.decode(stringIv, Base64.DEFAULT)
                            ), Base64.DEFAULT);
                    //Log.d(TAG, "eeUid : " + eeUid);


                    JSONObject echo = checkTableList(id, eeUid, defaultUrl + transactionPhp);
                    Log.d(TAG, "echo:" + echo);
                    String status = echo.getString(Echo.STATUS);


                    switch (status) {
                        case Status.SUCCESS:
                            String newIv = null;
                            try {
                                newIv = echo.getString(Echo.NEW_IV);
                            } catch (JSONException e) {
                                Log.e(TAG, Log.getStackTraceString(e));
                            }
                            if (newIv != null
                                    && CipherUtils.checkIv(cipherInstance, Base64.decode(newIv, Base64.DEFAULT))
                                    && sharedPreferences.edit().putString(StarfangConstants.PREF_IV_KEY, newIv).commit()) {
                                JSONArray tableList = echo.getJSONArray(Echo.TABLES);
                                pair = new Pair<>(ResultCode.SUCCESS, tableList);
                            } else {
                                pair = new Pair<>(ResultCode.RE_SIGN_IN, null);
                            }
                            break;
                        case Status.FAIL_DECRYPTION:
                            pair = new Pair<>(ResultCode.RE_SIGN_IN, null);
                            break;
                        case Status.FAIL:
                            pair = new Pair<>(ResultCode.ERROR, null);
                            break;
                        default:
                            return null;
                    }

                }
            } catch (GeneralSecurityException | IOException | JSONException | InterruptedException | ExecutionException | TimeoutException e) {
                pair = new Pair<>(ResultCode.ERROR, null);
            }


        }

        return pair;

    }

    private JSONObject checkTableList(final String id, final String eeUid, final String url) throws
            InterruptedException, ExecutionException, TimeoutException {

        Realm realm = Realm.getDefaultInstance();
        RealmResults<TableList> tableList = realm.where(TableList.class).findAll();
        JSONArray tableListJson = new JSONArray(tableList);

        RequestQueue requestQueue = Volley.newRequestQueue(contextWeakReference.get());
        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();

        Map<String, String> params = new HashMap<>();
        params.put(Params.ID, id);
        params.put(Params.EE_UID, eeUid);
        params.put(Params.TRANSACTION, String.valueOf(StarfangConstants.TRANSACTION_CHECK_TABLE));
        params.put(Params.TABLE_LIST, tableListJson.toString());

        Log.d(TAG, "url: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, new JSONObject(params),
                requestFuture, requestFuture) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Log.d(TAG, "request: " + new String(jsonObjectRequest.getBody()));

        requestQueue.add(jsonObjectRequest);

        return requestFuture.get(20, TimeUnit.SECONDS);

    }
}
