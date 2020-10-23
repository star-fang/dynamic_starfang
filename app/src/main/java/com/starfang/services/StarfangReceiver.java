package com.starfang.services;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.RemoteInput;

import com.starfang.StarfangConstants;
import com.starfang.realm.notifications.Conversation;
import com.starfang.realm.notifications.Forum;

import org.apache.http.util.TextUtils;

import io.realm.Realm;


public class StarfangReceiver extends BroadcastReceiver {

    private static final String TAG = "FANG_RECEIVER";
    public static final String EXTRA_KEY_FORUM_ID = "forumId";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle information = intent.getBundleExtra(StarfangConstants.EXTRA_INFORMATION);
        if( information != null ) {
            Object forumIdObj = information.get(EXTRA_KEY_FORUM_ID);
            if (forumIdObj != null) {
                final String sendCat = information.getString(Notification.EXTRA_TITLE, "");
                final long forumId = Long.parseLong(forumIdObj.toString());
                CharSequence msgChars = getMessageText(intent);
                if (!TextUtils.isEmpty(msgChars)) {

                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.executeTransactionAsync(bgRealm -> {

                            Forum forum = bgRealm.where(Forum.class)
                                    .equalTo(Forum.FIELD_ID, forumId)
                                    .findFirst();

                            if( forum != null ) {
                                Conversation conversation = bgRealm.createObject(Conversation.class);
                                conversation.setContent(msgChars.toString());
                                conversation.setWhen(System.currentTimeMillis());
                                conversation.setSendCat(sendCat);
                                forum.addConversation(conversation);
                            }
                        }, () -> {
                            Log.d(TAG, "receive success");
                        }, error -> {
                            Log.e(TAG, Log.getStackTraceString(error));
                        });
                    } catch (RuntimeException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
            }
        }
    }

    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent( intent );
        if( remoteInput != null ) {
            return remoteInput.getCharSequence(StarfangConstants.REPLY_KEY_LOCAL);

        }
        return null;
    }
}
