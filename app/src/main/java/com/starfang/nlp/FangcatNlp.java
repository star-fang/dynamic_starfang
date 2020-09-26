package com.starfang.nlp;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.starfang.CMDActivity;
import com.starfang.realm.Cmd;
import com.starfang.utilities.reply.ReplyAction;


import java.lang.ref.WeakReference;
import java.util.List;

import javax.annotation.Nonnull;

import io.realm.Realm;

public class FangcatNlp extends AsyncTask<String, String, Void> {

    private static final String TAG = "fang_nlp";
    private static final String MASTER_CMD_CAT = "냥";
    private static final String MASTER_CMD_DOG = "멍";
    private WeakReference<Context> contextWeakReference;
    private WeakReference<ReplyAction> replyActionWeakReference;

    public FangcatNlp(Context context, ReplyAction replyAction) {
        this.contextWeakReference = new WeakReference<>(context);
        if( replyAction != null ) {
            this.replyActionWeakReference = new WeakReference<>(replyAction);
        } else {
            this.replyActionWeakReference = null;
        }
    }

    private void observeCat(@Nonnull String text) {
        switch (text.length()) {
            case 1:
                publishProgress("뭐");
                break;
            case 2:
                publishProgress(text);
                break;
            default:
                try {
                    try (Realm realm = Realm.getDefaultInstance()) {
                        List<String> messages = RokLambda.processReq(text.substring(0, text.length() - 1), realm);
                        for (String message : messages) {
                            publishProgress(message);
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
        }

    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (values != null) {
            if( replyActionWeakReference != null ) {
                ReplyAction replyAction = replyActionWeakReference.get();
                Context context = contextWeakReference.get();
                for (String message : values) {
                    try {
                        replyAction.sendReply(context, message);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            } else {
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
    }

    @Override
    protected Void doInBackground(String... strings) {
        String text = strings[0];
        if (text != null) {
            text = text.trim();
            if (text.length() > 1) {
                switch (text.substring(text.length() - 1)) {
                    case MASTER_CMD_CAT:
                        observeCat(text);
                        break;
                    case MASTER_CMD_DOG:
                        break;
                    default:
                }
            }
        }
        return null;
    }
}
