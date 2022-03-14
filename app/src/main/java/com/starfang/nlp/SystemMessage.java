package com.starfang.nlp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.starfang.activities.CMDActivity;
import com.starfang.realm.Cmd;

import io.realm.Realm;

public class SystemMessage {


    private static final String TAG = "FANG_MESSAGE";

    public static void insertMessage(@NonNull String message, @NonNull String title, Context callbackContext) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransactionAsync(bgRealm -> {
                        final Cmd messageCmd = new Cmd(false);
                        messageCmd.setName(title);
                        messageCmd.setText(message);
                        bgRealm.copyToRealm(messageCmd);
                    }
                    , () -> callbackContext.sendBroadcast(new Intent(CMDActivity.ACTION_SCROLL_TO_BOTTOM)));
        } catch( RuntimeException e ) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

}
