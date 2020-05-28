package com.starfang.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.starfang.StarfangConstants;
import com.starfang.utilities.VersionUtils;

public class RestartAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "FANG_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"restart receiver activated");
        if(VersionUtils.isOreo() ) {
            Intent startForeIntent = new Intent(context, RestartService.class);
            context.startForegroundService(startForeIntent);
        } else {
            Intent startBackIntent = new Intent(context, StarfangService.class);
            startBackIntent.putExtra(
                    StarfangConstants.BOT_STATUS_KEY,
                    StarfangConstants.BOT_STATUS_RESTART);
            context.startService(startBackIntent);
        }
    }
}
