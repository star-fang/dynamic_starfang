package com.starfang.utilities;

import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.starfang.services.StarfangService;
import com.starfang.utilities.reply.ReplyAction;

public class ReplyUtils {

    private static final String[] REPLY_KEYWORDS = {"reply", "android.intent.extra.text"};
    //private static final CharSequence REPLY_KEYWORD = "reply";
    private static final CharSequence INPUT_KEYWORD = "input";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static ReplyAction getQuickReplyAction(@NonNull Notification notification) {
        NotificationCompat.Action action = null;
        if (VersionUtils.isNougat())
            action = getAndroidReplyAction(notification);
        if (action == null)
            action = getWearReplyAction(notification);
        if (action == null)
            return null;
        Bundle extras = notification.extras;
        return new ReplyAction(action
                , getSendCat(extras)
                , getForumName(extras)
                , getContentText(extras)
                , true);
    }

    private static NotificationCompat.Action getAndroidReplyAction(Notification notification) {
        for (int i = 0; i < NotificationCompat.getActionCount(notification); i++) {
            NotificationCompat.Action action = NotificationCompat.getAction(notification, i);
            if (action.getRemoteInputs() != null) {
                for (int x = 0; x < action.getRemoteInputs().length; x++) {
                    RemoteInput remoteInput = action.getRemoteInputs()[x];
                    if (isKnownReplyKey(remoteInput.getResultKey()))
                        return action;
                }
            }
        }
        return null;
    }

    private static NotificationCompat.Action getWearReplyAction(Notification notification) {
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender(notification);
        for (NotificationCompat.Action action : wearableExtender.getActions()) {
            if (action.getRemoteInputs() != null) {
                for (int x = 0; x < action.getRemoteInputs().length; x++) {
                    RemoteInput remoteInput = action.getRemoteInputs()[x];
                    if (isKnownReplyKey(remoteInput.getResultKey()))
                        return action;
                    else if (remoteInput.getResultKey().toLowerCase().contains(INPUT_KEYWORD))
                        return action;
                }
            }
        }
        return null;
    }

    private static boolean isKnownReplyKey(String resultKey) {
        if (TextUtils.isEmpty(resultKey))
            return false;

        resultKey = resultKey.toLowerCase();
        for (String keyword : REPLY_KEYWORDS)
            if (resultKey.contains(keyword))
                return true;

        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    static String getSendCat(Bundle extras) {
        CharSequence sendCatChars = extras.getCharSequence(Notification.EXTRA_TITLE);
        if (!TextUtils.isEmpty(sendCatChars))
            return sendCatChars.toString();
        else
            return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    static String getContentText(Bundle extras) {
        CharSequence textChars = extras.getCharSequence(Notification.EXTRA_TEXT);
        if (!TextUtils.isEmpty(textChars))
            return textChars.toString();
        else if (!TextUtils.isEmpty((textChars = extras.getString(Notification.EXTRA_SUMMARY_TEXT))))
            return textChars.toString();
        else
            return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    static String getForumName(Bundle extras) {
        CharSequence forumChars = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
        if (!TextUtils.isEmpty(forumChars))
            return forumChars.toString();
        return null;
    }

    /*
    public static ArrayList<ReplyAction> getActions(Notification n, String packageName, ArrayList<ReplyAction> actions) {
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender(n);
        if (wearableExtender.getActions().size() > 0) {
            for (NotificationCompat.Action action : wearableExtender.getActions())
                actions.add(new ReplyAction(action, action.title.toString().toLowerCase().contains(REPLY_KEYWORD)));
        }
        return actions;
    }

     */
}
