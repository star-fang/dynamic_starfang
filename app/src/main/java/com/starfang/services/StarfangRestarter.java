package com.starfang.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class StarfangRestarter extends BroadcastReceiver {

    private static final String TAG = "FANG_RESTARTER";
    public static final String ACTION_RESTART = "starfang_restart_action";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"restart receiver activated");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(new Intent(context, StarfangService.class));
        else
            context.startService(new Intent(context, StarfangService.class));
    }
}
