package com.starfang.nlp;

import android.app.PendingIntent;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.starfang.realm.source.Units;
import com.starfang.utilities.reply.ReplyAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import javax.annotation.Nonnull;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;

public class NlpSplitter extends AsyncTask<String, String, Void> {

    private static final String TAG = "fang_nlp";
    private static final String MASTER_CMD_CAT = "냥";
    private static final String MASTER_CMD_DOG = "멍";
    private WeakReference<Context> contextWeakReference;
    private WeakReference<ReplyAction> replyActionWeakReference;

    public NlpSplitter(Context context, ReplyAction replyAction) {
        this.contextWeakReference = new WeakReference<>(context);
        this.replyActionWeakReference = new WeakReference<>(replyAction);
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
                    JSONObject json = SearchUnits.interpret(text.substring(0, text.length() - 1));
                    if (json.has("fields") || json.has("passives")) {
                        try (Realm realm = Realm.getDefaultInstance()) {
                            for (String message : SearchUnits.search(json, realm)) {
                                publishProgress(message);
                            }
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
            ReplyAction replyAction = replyActionWeakReference.get();
            Context context = contextWeakReference.get();
            for (String message : values) {
                try {
                    replyAction.sendReply(context, message);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
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
