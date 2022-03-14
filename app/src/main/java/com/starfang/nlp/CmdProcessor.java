package com.starfang.nlp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.RemoteInput;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModel;

import com.starfang.R;
import com.starfang.activities.CMDActivity;
import com.starfang.StarfangConstants;
import com.starfang.realm.transaction.ReadJsonFileTask;
import com.starfang.services.StarfangService;
import com.starfang.utilities.ScreenUtils;
import com.starfang.utilities.ServiceUtils;

import java.lang.ref.WeakReference;

public class CmdProcessor extends AsyncTask<String, String, Bundle> {

    protected interface CmdMods {
        int SYNC = 1;
        int FANGCAT = 2;
        String CMD_NOTIFICATION = "알림";
        String CMD_SYNC = "연결";
        String CMD_START = "시작";
        String CMD_STOP = "정지";

        String POST_KEY = "postKey";
        String POST_VALUE = "postValue";
    }


    private final WeakReference<Context> contextWeakReference;
    private final WeakReference<ViewModel> mViewModelRef;
    private final WeakReference<NotificationManager> managerWeakReference;

    public CmdProcessor(Context context, NotificationManager manager, ViewModel viewModel) {
        this.contextWeakReference = new WeakReference<>(context);
        this.mViewModelRef = new WeakReference<>(viewModel);
        this.managerWeakReference = new WeakReference<>(manager);
    }


    @Override
    protected void onProgressUpdate( @NonNull String... values) {
        super.onProgressUpdate(values);
        for (String message : values) {
            SystemMessage.insertMessage(message, "com.starfang", contextWeakReference.get());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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
                    readRok.execute("rok3.json", "rok_technology.json", "rok_building.json", "vertex1947.json");
                    readCat.execute("cat.json");
                    break;
                case CmdMods.FANGCAT:
                    try {
                        Intent replyIntent = new Intent(CMDActivity.ACTION_REPLY);
                        Bundle notificationInfo = new Bundle();
                        notificationInfo.putString(Notification.EXTRA_TITLE, "com.starfang.reply");
                        replyIntent.putExtra(CMDActivity.KEY_REPLY_INFO, notificationInfo);

                        RemoteInput remoteInput = new RemoteInput.Builder(CMDActivity.KEY_REPLY_RESULT)
                                .setLabel("starfang reply")
                                .build();
                        PendingIntent replyPendingIntent =
                                PendingIntent.getBroadcast(context.getApplicationContext(),
                                        0, replyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                        NotificationCompat.Action replyAction =
                                new NotificationCompat.Action.Builder(R.drawable.ic_chat_bubble_black_24dp,
                                        "starfang.reply", replyPendingIntent)
                                        .addRemoteInput(remoteInput)
                                        .setAllowGeneratedReplies(true)
                                        .build();
                        Notification notification = new NotificationCompat.Builder(context, CMDActivity.NOTIFICATION_CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle("com.starfang")
                                .setContentText(bundle.getString(CmdMods.POST_VALUE))
                                .setPriority(NotificationManager.IMPORTANCE_MIN)
                                .setCategory(Notification.CATEGORY_SERVICE)
                                .addAction(replyAction)
                                .build();
                        managerWeakReference.get().notify(101, notification);
                    } catch( Exception e ) {
                        SystemMessage.insertMessage( e.toString(), "com.starfang.error", context );
                    }

                    /*
                    FangcatNlp fangcatNlp = new FangcatNlp(context, null, "ㅁㅁㅁ", 0);
                    new CountDownTimer( 5000, 500){

                        @Override
                        public void onTick(long millisUntilFinished) {
                            if( fangcatNlp.isCancelled() || fangcatNlp.getStatus() == AsyncTask.Status.FINISHED )
                                this.cancel();
                        }

                        @Override
                        public void onFinish() {
                            AsyncTask.Status status = fangcatNlp.getStatus();
                            if( status == AsyncTask.Status.PENDING || status == AsyncTask.Status.RUNNING ) {
                                fangcatNlp.setCancelledMsg("타임아웃 에러(5초)");
                                fangcatNlp.cancel(true);
                            }

                        }
                    }.start();
                    fangcatNlp.execute(bundle.getString(CmdMods.POST_VALUE));

                     */
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
                case CmdMods.CMD_START:
                    if (!ServiceUtils.isServiceExist(context, StarfangService.class, false)) {
                        publishProgress("'알림'을 입력하여 시작하세요.");
                    } else {
                        publishProgress("이미 시작했다멍");
                    }
                    break;
                case CmdMods.CMD_STOP:
                    publishProgress("정지할수 없다멍");

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
