package com.starfang.nlp;

import android.app.PendingIntent;
import android.content.Context;

import com.starfang.utilities.reply.ReplyAction;

import javax.annotation.Nonnull;

public class NlpSplitter {

    private static final String MASTER_CMD_CAT = "냥";
    private static final String MASTER_CMD_DOG = "멍";

    public NlpSplitter(@Nonnull String text, Context context, ReplyAction replyAction) {
        text = text.trim();
        if( text.length() > 0 ) {
            switch (text.substring(text.length() - 1)) {
                case MASTER_CMD_CAT:
                    try {
                        observeCat(text, context, replyAction);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                    break;
                case MASTER_CMD_DOG:
                    break;
                default:
            }
        }
    }

    private void observeCat(String text, Context context, ReplyAction replyAction) throws PendingIntent.CanceledException {
        switch( text.length() ) {
            case 1:
                replyAction.sendReply(context, "뭐");
                break;
            case 2:
                replyAction.sendReply(context, text);
                break;
            default:

        }

    }


}
