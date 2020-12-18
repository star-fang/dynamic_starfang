package com.starfang.nlp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.starfang.realm.Cmd;

import io.realm.Realm;

public class SystemMessage {


    private static final String TAG = "FANG_MESSAGE";

    public static void insertMessage(@NonNull String message, Context callbackContext, String callbackAction) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransactionAsync(bgRealm -> {
                        final Cmd messageCmd = new Cmd(false);
                        messageCmd.setName("멍멍이");
                        messageCmd.setText(message);
                        bgRealm.copyToRealm(messageCmd);
                    }
                    , () -> callbackContext.sendBroadcast(new Intent(callbackAction)));
        } catch( RuntimeException e ) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

}
