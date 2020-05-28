package com.starfang.utilities.reply;

import android.app.PendingIntent;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

public class Replier extends AsyncTask<String, Integer, String> {

    private static final String TAG = "FANG_REPLY";


    private WeakReference<Context> contextWeakReference;
    private ReplyAction replyAction;

    public Replier(Context context, ReplyAction replyAction, boolean record) {
        this.contextWeakReference = new WeakReference<>(context);
        this.replyAction = replyAction;
    }

    @Override
    protected String doInBackground(String... answers) {
        if( replyAction != null ) {
            for( String answer : answers ) {
                try {
                    replyAction.sendReply(contextWeakReference.get(),answer);
                } catch (PendingIntent.CanceledException e) {
                    Log.d(TAG,Log.getStackTraceString(e));
                }
            }
        }
        return null;
    }


}
