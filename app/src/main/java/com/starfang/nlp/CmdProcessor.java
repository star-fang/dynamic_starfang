package com.starfang.nlp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;

import com.starfang.CMDActivity;
import com.starfang.StarfangConstants;
import com.starfang.realm.Cmd;
import com.starfang.realm.transaction.rok.ReadJsonFileTask;
import com.starfang.services.StarfangService;
import com.starfang.utilities.ServiceUtils;

import java.lang.ref.WeakReference;


import io.realm.Realm;

public class CmdProcessor extends AsyncTask<String, String, Void> {

    protected interface CmdMods {
        //int DEFAULT = 0;
        //int SYNC = 1;
        //int FANGCAT = 2;
        String CMD_NOTIFICATION = "알람";
        String CMD_SYNC = "동기화";
        String CMD_START = "시작";
        String CMD_STOP = "정지";
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
                    Cmd cmd = new Cmd();
                    cmd.setName("시스템");
                    cmd.setText(message);
                    cmd.setWhen(System.currentTimeMillis());
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
    protected Void doInBackground(String... strings) {
        String text = strings[0];
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
                    ReadJsonFileTask task = new ReadJsonFileTask(context);
                    task.execute("rok.json");
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
                            publishProgress("냥봇 시작");
                        } else {
                            publishProgress("냥봇 시작 실패");
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
                        publishProgress("냥봇 정지");
                    } else {
                        publishProgress("냥봇 정지 실패");
                    }

                    break;
                default:
                    FangcatNlp fangcatNlp = new FangcatNlp(context,null);
                    fangcatNlp.execute(text);
            }
        }
        return null;
    }
}
