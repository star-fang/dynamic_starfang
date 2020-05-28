package com.starfang.realm.transaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.starfang.R;
import com.starfang.StarfangConstants;
import com.starfang.realm.ProgressRequest;
import com.starfang.realm.TableList;
import com.starfang.realm.Transaction;
import com.starfang.ui.progress.ProgressFragment;
import com.starfang.ui.progress.ProgressViewModel;
import com.starfang.utilities.ArithmeticUtils;
import com.starfang.utilities.CipherUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.realm.Case;
import io.realm.Realm;

/*
MySQL <1> PHP <2> App <3> User
 <1> : Mysql tables interpreted by php and converted to Json form
 <2> : App download json and convert it to realm objects
 <3> : User can watch view by realm-adaptors
 */

public class DownloadJsonTask extends Transaction<String, Bundle, String> {
    private static final String TAG = "FANG_DOWNLOAD";

    private WeakReference<Context> contextWeakReference;

    private WeakReference<ProgressViewModel> viewModelWeakRef;

    private Map<Integer, Long> byteSizeMap;

    DownloadJsonTask(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        final Context context = contextWeakReference.get();
        final ProgressFragment progressFragment = ProgressFragment.newInstance(null);
        if (context instanceof AppCompatActivity) {

            final FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(android.R.id.content, progressFragment).commitNow();

            viewModelWeakRef = new WeakReference<>(new ViewModelProvider(progressFragment).get(ProgressViewModel.class));
            ProgressViewModel progressViewModel = viewModelWeakRef.get();

            progressViewModel.setIndeterminate(false);
            progressViewModel.setQuitVisibility(View.GONE);
            progressViewModel.setTopStartText("다운로드");
            this.byteSizeMap = new HashMap<>();

            return;
        }
        this.cancel(true);


    }

    @Override
    protected void onPostExecute(String summary) {
        //25 actionable tasks: 5 executed, 20 up-to-date
        Log.d(TAG, summary);

        ProgressViewModel progressViewModel = viewModelWeakRef.get();
        progressViewModel.setQuitVisibility(View.VISIBLE);
        progressViewModel.setTitleText(summary);

        long totalByteSize = 0L;
        for (Long bytes : byteSizeMap.values()) {
            try {
                totalByteSize = ArithmeticUtils.longSum(totalByteSize, bytes);
            } catch (ArithmeticException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        progressViewModel.setBelowText(MessageFormat.format("total size: {0}byte", totalByteSize));

    }

    @Override
    protected final void onProgressUpdate(Bundle... argsArray) {
        Bundle args = argsArray[0];
        if (args != null) {
            final ProgressViewModel progressViewModel = viewModelWeakRef.get();
            int progressCode = args.getInt(ProgressArgs.PROGRESS_CODE, -1);
            switch (progressCode) {
                case ProgressCode.PROGRESS:
                    long downloadedByte = args.getLong(ProgressArgs.BYTE_DONE, 0);
                    long totalByte = args.getLong(ProgressArgs.BYTE_TOTAL, 1);

                    progressViewModel.setMaxByte(totalByte);
                    byteSizeMap.put(args.getInt(ProgressArgs.TASK_INDEX, 0), totalByte);

                    float percentage = (downloadedByte / ((float) totalByte)) * 100;
                    progressViewModel.setProgress((int)percentage);
                    //progressViewModel.setAboveTextText(MessageFormat.format("{0}%", percentage));
                    //progressViewModel.setBelowText(MessageFormat.format("{0} / {1}", downloadedByte, totalByte));

                    //Log.d(TAG, downloaded + " / " + total + " (" + percentage + "%)");
                    break;
                case ProgressCode.START:
                    progressViewModel.setProgress(0);
                    progressViewModel.setTitleText(args.getString(ProgressArgs.TITLE, null));
                    progressViewModel.setTopEndText(MessageFormat.format("{0} / {1}"
                            , (args.getInt(ProgressArgs.TASK_INDEX, 0) + 1)
                            , args.getInt(ProgressArgs.TASK_COUNT, 0)));
                    //progressBar.setProgress(0);
                    //titleTextWeakRef.get().setText(args.getString(ProgressArgs.TITLE, null));
                    //topEndTextWeakRef.get().setText(MessageFormat.format("{0} / {1}"
                    //       , (args.getInt(ProgressArgs.TASK_INDEX, 0) + 1)
                    //       , args.getInt(ProgressArgs.TASK_COUNT, 0)));
                    break;
                case ProgressCode.DONE:
                    progressViewModel.setProgress(101);
                    //progressBar.setProgress(100);
                    break;
            }
        }
    }

    @Override
    protected String doInBackground(@NotNull String... tableNames) {

        StringBuilder stringBuilder = new StringBuilder();
        final Context context = contextWeakReference.get();

        RequestQueue requestQueue = Volley.newRequestQueue(contextWeakReference.get());

        final SharedPreferences sharedPreferences = context.getSharedPreferences(StarfangConstants.SHARED_PREF_STORE, Context.MODE_PRIVATE);
        final String uid = FirebaseAuth.getInstance().getUid();
        final String id = sharedPreferences.getString(StarfangConstants.PREF_ID_KEY, null);
        String stringKey;
        try {
            stringKey = CipherUtils.getSecureSharedPreferences(context)
                    .getString(StarfangConstants.SECURE_PREF_SECRET_KEY, null);
        } catch (GeneralSecurityException | IOException e) {
            stringKey = null;
        }

        if (uid == null || id == null || stringKey == null) {
            return "에러댜옹";
        }

        int count = tableNames.length;
        stringBuilder.append(count).append("actionable tasks: ");
        int executedCount = 0;
        for (int i = 0; i < count; i++) {
            if (isCancelled()) {
                break;
            }

            Log.d(TAG, i + "--------------------------------------");
            String tableName = tableNames[i];
            Bundle args = new Bundle();
            args.putInt(ProgressArgs.PROGRESS_CODE, ProgressCode.START);
            args.putInt(ProgressArgs.TASK_INDEX, i);
            args.putString(ProgressArgs.TITLE, tableName);
            args.putInt(ProgressArgs.TASK_COUNT, count);
            publishProgress(args);
            final String stringIv = sharedPreferences.getString(StarfangConstants.PREF_IV_KEY, null);
            boolean success = false;
            if (stringIv != null) {
                try {
                    JSONObject echo = postTable(
                            i, id, stringKey, stringIv, uid,
                            requestQueue,
                            context,
                            tableName
                            , context.getString(R.string.url_sync_default) + context.getString(R.string.php_transaction));

                    if (echo != null) {
                        try {
                            Log.d(TAG, "echo: " + echo);
                            //Log.d(TAG, "str length: " + echo.toString().length() );
                            String newIv = echo.getString(Echo.NEW_IV);
                            if (sharedPreferences.edit().putString(StarfangConstants.PREF_IV_KEY, newIv).commit()) {
                                String status = echo.getString(Echo.STATUS);
                                String message = echo.getString(Echo.MESSAGE);
                                JSONArray tuples = echo.getJSONArray(Echo.TUPLES);
                                Bundle doneArgs = new Bundle();
                                doneArgs.putInt(ProgressArgs.PROGRESS_CODE, ProgressCode.DONE);
                                publishProgress(doneArgs);
                                success = true;
                                executedCount++;
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }

                } catch (GeneralSecurityException | InterruptedException | ExecutionException | TimeoutException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, Log.getStackTraceString(e));
                Thread.currentThread().interrupt();
            }

            if (!success) {
                Bundle errorArgs = new Bundle();
                errorArgs.putInt(ProgressArgs.PROGRESS_CODE, ProgressCode.ERROR);
                publishProgress(errorArgs);
            }

        }

        stringBuilder.append(executedCount).append(" executed, ")
                .append(count - executedCount).append(" error occurred");

        return stringBuilder.toString();
    }


    private JSONObject postTable(int index, String id, String stringKey, String stringIv, String uid, RequestQueue requestQueue, Context context, final String tableName, String url) throws GeneralSecurityException, InterruptedException, ExecutionException, TimeoutException {

        final String eeUid = Base64.encodeToString(
                CipherUtils.encrypt(
                        context.getString(R.string.cipher_algorithm)
                        , context.getString(R.string.cipher_instance)
                        , stringKey
                        , uid.getBytes(StandardCharsets.UTF_8)
                        , Base64.decode(stringIv, Base64.DEFAULT)
                ), Base64.DEFAULT);

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();

        Log.d(TAG, "url:" + url);

        Map<String, String> params = new HashMap<>();
        params.put(Params.ID, id);
        params.put(Params.EE_UID, eeUid);
        params.put(Params.TABLE_NAME, tableName);
        params.put(Params.LAST_MODIFIED, getTableLastModified(tableName));
        params.put(Params.TRANSACTION, String.valueOf(StarfangConstants.TRANSACTION_DOWNLOAD_JSON));

        ProgressRequest request = new ProgressRequest(
                Request.Method.POST, url, new JSONObject(params)
                , requestFuture
                , requestFuture, (transferredBytes, totalSize) -> {
            Bundle progArgs = new Bundle();
            progArgs.putInt(ProgressArgs.TASK_INDEX, index);
            progArgs.putInt(ProgressArgs.PROGRESS_CODE, ProgressCode.PROGRESS);
            progArgs.putLong(ProgressArgs.BYTE_DONE, transferredBytes);
            progArgs.putLong(ProgressArgs.BYTE_TOTAL, totalSize);
            publishProgress(progArgs);

        });

        Log.d(TAG, "request:" + new String(request.getBody()));

        requestQueue.add(request);
        return requestFuture.get(20, TimeUnit.SECONDS);
    }

    private String getTableLastModified(String tableName) {

        long lastModified = 0L;

        try (Realm realm = Realm.getDefaultInstance()) {
            TableList tableList = realm.where(TableList.class)
                    .equalTo(TableList.FIELD_TABLE, tableName, Case.INSENSITIVE).findFirst();
            if (tableList != null) {
                lastModified = tableList.getLastModified();
            }
        } catch (RuntimeException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return String.valueOf(lastModified);
    }


}
