package com.starfang.nlp;

import android.app.PendingIntent;
import android.content.Context;
import android.os.AsyncTask;

import com.starfang.activities.CMDActivity;
import com.starfang.nlp.lambda.LambdaCat;
import com.starfang.nlp.lambda.LambdaRok;
import com.starfang.utilities.reply.ReplyAction;


import java.lang.ref.WeakReference;
import java.util.List;

public class FangcatNlp extends AsyncTask<String, String, Void> {

    private static final String TAG = "fang_nlp";
    private static final String MASTER_CMD_CAT = "냥";
    private final WeakReference<Context> contextWeakReference;
    private final WeakReference<ReplyAction> replyActionWeakReference;
    private final String sendCat;
    private final long forumId;

    public FangcatNlp(Context context, ReplyAction replyAction, String sendCat, long forumId) {
        this.contextWeakReference = new WeakReference<>(context);
        if (replyAction != null) {
            this.replyActionWeakReference = new WeakReference<>(replyAction);
        } else {
            this.replyActionWeakReference = null;
        }
        this.sendCat = sendCat;
        this.forumId = forumId;
    }


    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (values != null) {
            Context context = contextWeakReference.get();
            if (replyActionWeakReference != null) {
                ReplyAction replyAction = replyActionWeakReference.get();
                if( values.length < 100 ) { // todo: 메세지가 너무많을 경우 처리
                    for (String message : values) {
                        try {
                            replyAction.sendReply(context, message);
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                for (String message : values) {
                    SystemMessage.insertMessage(message, context, CMDActivity.ACTION_NOTIFY);
                }

            }
        }
    }

    @Override
    protected Void doInBackground(String... strings) {
        String text = strings[0];
        if (text != null) {
            text = text.trim();
            if (text.length() > 2) {
                List<String> messages = null;
                if (text.startsWith(MASTER_CMD_CAT)) {
                    text = text.substring(1);
                    messages = LambdaCat.processReq(
                            text);
                }
                if (text.endsWith(MASTER_CMD_CAT)) {
                    messages = LambdaRok.processReq(
                            text.substring(0, text.length() - 1)
                            , sendCat, forumId);
                }

                if (messages != null) {
                    for (String message : messages) {
                        publishProgress(message);
                    }
                }
            }
        }
        return null;
    }
}
