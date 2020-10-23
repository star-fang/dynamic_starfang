package com.starfang.nlp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;

import com.starfang.CMDActivity;
import com.starfang.StarfangConstants;
import com.starfang.realm.Cmd;
import com.starfang.realm.transaction.rok.ReadJsonFileTask;
import com.starfang.services.StarfangService;
import com.starfang.utilities.ServiceUtils;

import java.lang.ref.WeakReference;


import io.realm.Realm;

public class CmdProcessor extends AsyncTask<String, String, Bundle> {

    protected interface CmdMods {
        //int DEFAULT = 0;
        int SYNC = 1;
        int FANGCAT = 2;
        String CMD_NOTIFICATION = "알림";
        String CMD_SYNC = "연결";
        String CMD_START = "시작";
        String CMD_STOP = "정지";

        String POST_KEY = "postKey";
        String POST_VALUE = "postValue";
    }


    private WeakReference<Context> contextWeakReference;

    public CmdProcessor(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }


    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (values != null) {
            try (Realm realm = Realm.getDefaultInstance()) {
                for (String message : values) {
                    Cmd cmd = new Cmd(false);
                    cmd.setName("멍멍이");
                    cmd.setText(message);
                    realm.beginTransaction();
                    realm.copyToRealm(cmd);
                    realm.commitTransaction();
                    Intent intent = new Intent(CMDActivity.ACTION_CMD_ADDED);
                    contextWeakReference.get().sendBroadcast(intent);
                }
            }
        }
    }

    @Override
    protected void onPostExecute(Bundle bundle) {
        super.onPostExecute(bundle);
        if( bundle != null ) {
            Context context = contextWeakReference.get();
            switch (bundle.getInt(CmdMods.POST_KEY)) {
                case CmdMods.SYNC:
                    ReadJsonFileTask task = new ReadJsonFileTask(context);
                    task.execute(bundle.getStringArray(CmdMods.POST_VALUE));
                    break;
                case CmdMods.FANGCAT:
                    FangcatNlp fangcatNlp = new FangcatNlp(context,null, "얼간이", 0);
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
        if( text != null ) {
            Context context = contextWeakReference.get();
            SharedPreferences sharedPref = context.getSharedPreferences(StarfangConstants.SHARED_PREF_STORE,Context.MODE_PRIVATE);
            Intent intent;
            switch (text) {
                case CmdMods.CMD_NOTIFICATION:
                    intent = new Intent();
                    intent.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    context.startActivity(intent);
                    break;
                case CmdMods.CMD_SYNC:
                    result = new Bundle();
                    result.putInt(CmdMods.POST_KEY, CmdMods.SYNC);
                    result.putStringArray(CmdMods.POST_VALUE, new String[]{"rok.json", "rok_technology.json", "rok_building.json"});
                    return result;
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
