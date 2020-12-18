package com.starfang.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.starfang.activities.MainActivity;
import com.starfang.R;
import com.starfang.StarfangConstants;
import com.starfang.utilities.VersionUtils;


public class RestartService extends Service {

    private static final String TAG = "FANG_SERVICE_RESTART";
    private static final String channelId = "StarfangRsc";
    private static final String channelName = "StarfangRestartService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"restart service activated");

        startForegroundService(channelId, channelName);

        Intent startServiceIntent = new Intent(RestartService.this, StarfangService.class);
        startServiceIntent.putExtra(
                StarfangConstants.BOT_STATUS_KEY,
                StarfangConstants.BOT_STATUS_RESTART);
        startService(startServiceIntent);

        stopForeground(true);
        stopSelf();
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startForegroundService(String id, String name) {

        Intent notificationIntent = new Intent(RestartService.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(RestartService.this, 0,
                notificationIntent, 0);

        String channelId = "";
        if (VersionUtils.isOreo()) {
            channelId = createNotificationChannel(id, name);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentTitle("ANDROID")
                .setContentText(VersionUtils.currentVersion())
                .setContentIntent(pendingIntent)
                .build();
        startForeground(101, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel channel = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (service != null) {
            service.createNotificationChannel(channel);
        }
        return channelId;
    }
}
