package com.starfang.nlp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;

import androidx.lifecycle.ViewModel;

import com.starfang.activities.CMDActivity;
import com.starfang.StarfangConstants;
import com.starfang.realm.transaction.ReadJsonFileTask;
import com.starfang.services.FirestoreService;
import com.starfang.services.StarfangService;
import com.starfang.utilities.ScreenUtils;
import com.starfang.utilities.ServiceUtils;

import java.lang.ref.WeakReference;

public class CmdProcessor extends AsyncTask<String, String, Bundle> {

    protected interface CmdMods {
        //int DEFAULT = 0;
        int SYNC = 1;
        int FANGCAT = 2;
        String CMD_NOTIFICATION = "알림";
        String CMD_SYNC = "연결";
        String CMD_START = "시작";
        String CMD_STOP = "정지";
        String CMD_FS_START = "동기화";

        String POST_KEY = "postKey";
        String POST_VALUE = "postValue";
    }


    private final WeakReference<Context> contextWeakReference;
    private final WeakReference<ViewModel> mViewModelRef;

    public CmdProcessor(Context context, WeakReference<ViewModel> viewModelRef) {
        this.contextWeakReference = new WeakReference<>(context);
        this.mViewModelRef = viewModelRef;
    }


    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (values != null) {
            for (String message : values) {
                SystemMessage.insertMessage(message,
                        contextWeakReference.get(), CMDActivity.ACTION_NOTIFY);
            }
        }
    }

    @Override
    protected void onPostExecute(Bundle bundle) {
        super.onPostExecute(bundle);
        if (bundle != null) {
            Context context = contextWeakReference.get();
            switch (bundle.getInt(CmdMods.POST_KEY)) {
                case CmdMods.SYNC:

                    ReadJsonFileTask readRok = new ReadJsonFileTask(
                            context
                            , StarfangConstants.REALM_MODEL_SOURCE_ROK
                            , "라오킹"
                            , ScreenUtils.dip2pix(context, 100)
                            , mViewModelRef);
                    ReadJsonFileTask readCat = new ReadJsonFileTask(
                            context
                            , StarfangConstants.REALM_MODEL_SOURCE_CAT
                            , "조조전"
                            , ScreenUtils.dip2pix(context, 200)
                            , mViewModelRef);
                    readRok.execute("rok2.json", "rok_technology.json", "rok_building.json", "vertex.json");
                    readCat.execute("cat.json");
                    break;
                case CmdMods.FANGCAT:
                    FangcatNlp fangcatNlp = new FangcatNlp(context, null, "ㅁㅁㅁ", 0);
                    fangcatNlp.execute(bundle.getString(CmdMods.POST_VALUE));
                    break;
                default:
            }
        }
    }

    @Override
    protected Bundle doInBackground(String... strings) {
        String text = strings[0];
        Bundle result;
        if (text != null) {
            Context context = contextWeakReference.get();
            SharedPreferences sharedPref = context.getSharedPreferences(StarfangConstants.SHARED_PREF_STORE, Context.MODE_PRIVATE);
            Intent intent;
            switch (text) {
                case CmdMods.CMD_NOTIFICATION:
                    intent = new Intent();
                    intent.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    context.startActivity(intent);
                    break;
                case CmdMods.CMD_SYNC:
                    publishProgress("데이터 연결 중...");
                    result = new Bundle();
                    result.putInt(CmdMods.POST_KEY, CmdMods.SYNC);
                    return result;
                case CmdMods.CMD_FS_START:
                    Intent fsIntent = new Intent(context, FirestoreService.class);
                    fsIntent.putExtra(FirestoreService.FS_MESSAGE, "동기화 중...");
                    context.startService(fsIntent);
                    break;
                case CmdMods.CMD_START:
                    if (!ServiceUtils.isServiceExist(context, StarfangService.class, false)) {
                        publishProgress("'알림'을 입력하여 권한을 얻으세요.");
                    } else {
                        if (sharedPref.edit().putInt(
                                StarfangConstants.BOT_STATUS_KEY,
                                StarfangConstants.BOT_STATUS_START).commit()) {
                            intent = new Intent(context, StarfangService.class);
                            intent.putExtra(
                                    StarfangConstants.BOT_STATUS_KEY,
                                    StarfangConstants.BOT_STATUS_START);
                            context.startService(intent);
                            publishProgress("냥봇 시작이다멍");
                        } else {
                            publishProgress("냥봇 시작 실패다멍");
                        }


                    }
                    break;
                case CmdMods.CMD_STOP:
                    if (sharedPref.edit().putInt(
                            StarfangConstants.BOT_STATUS_KEY,
                            StarfangConstants.BOT_STATUS_STOP).commit()) {
                        intent = new Intent(context, StarfangConstants.class);
                        intent.putExtra(
                                StarfangConstants.BOT_STATUS_KEY,
                                StarfangConstants.BOT_STATUS_STOP);
                        context.startService(intent);
                        publishProgress("냥봇 정지다멍");
                    } else {
                        publishProgress("냥봇 정지 실패다멍");
                    }

                    break;
                default:
                    result = new Bundle();
                    result.putInt(CmdMods.POST_KEY, CmdMods.FANGCAT);
                    result.putString(CmdMods.POST_VALUE, text);
                    return result;
            }
        }
        return null;
    }
}
