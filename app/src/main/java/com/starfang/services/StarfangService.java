package com.starfang.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.starfang.nlp.SystemMessage;
import com.starfang.nlp.lambda.LambdaCat;
import com.starfang.nlp.lambda.LambdaRok;
import com.starfang.realm.source.Source;
import com.starfang.realm.source.rok.Vertex;
import com.starfang.utilities.reply.ReplyAction;
import com.starfang.utilities.ReplyUtils;

import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;


import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.realm.Realm;

public class StarfangService extends NotificationListenerService {

    private static final String TAG = "FANG_SERVICE_NF";

    private Query vertex47Query;
    private ListenerRegistration vertex47LR;
    @Override
    public void onCreate() {
        super.onCreate();
        vertex47Query = FirebaseFirestore.getInstance().collection("1947vertex");
        vertex47LR = null;

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O )
            startStarfangForeground();
        else
            startForeground(1, new Notification());

        SystemMessage.insertMessage("Starfang service created", "com.starfang.debug", this);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startStarfangForeground() {
        String NOTIFICATION_CHANNEL_ID = "starfang_service";
        String channelName = "Starfang Background Channel";
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLACK);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if( manager == null )
            return;
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder  = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID );
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Starfang background service is running")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(33, notification);

    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "Listener connected");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "Listener disconnected");
    }

    @Override
    public int onStartCommand(@NotNull Intent intent, int flags, int startId) {
        SystemMessage.insertMessage("냥봇 시작: 종료->알림해제", "com.starfang", this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Notification Listener destroyed ");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(StarfangRestarter.ACTION_RESTART);
        broadcastIntent.setClass(this, StarfangRestarter.class);
        this.sendBroadcast(broadcastIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Notification Listener bind");
        registerFirestoreSnapshotListener();
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Notification Listener un-bind");
        if( vertex47LR != null ) {
            vertex47LR.remove();
            vertex47LR = null;
        }
        return true; // return true : make possible to rebind
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "Notification Listener re-bind");
        registerFirestoreSnapshotListener();
        super.onRebind(intent);
    }

    private void registerFirestoreSnapshotListener() {
        if( vertex47LR != null ) {
            vertex47LR.remove();
        }
        vertex47LR = vertex47Query.addSnapshotListener((snapshots, error) -> {
            if( error != null ) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }

            if( snapshots != null ) {
                try (Realm realm = Realm.getDefaultInstance()) {
                    realm.beginTransaction();
                    for (DocumentChange docChange : snapshots.getDocumentChanges()) {
                        QueryDocumentSnapshot doc = docChange.getDocument();
                        int id = NumberUtils.toInt(doc.getId());
                        Vertex vertex = realm.where(Vertex.class).equalTo(Source.FIELD_ID, id).findFirst();
                        if( vertex != null ) {

                            switch (docChange.getType()) {
                                case ADDED:
                                case MODIFIED:
                                    vertex.setTimeLimit(doc.getLong("timeLimit"));
                                    vertex.setDeadline(doc.getLong("deadline"));
                                    Log.d(TAG, "vertex"+vertex.getId()+" updated");
                                    break;
                                case REMOVED:
                                    vertex.setTimeLimit(0L);
                                    vertex.setDeadline(0L);
                                    Log.d(TAG, "vertex"+vertex.getId()+" unlinked");
                                    break;
                            }
                        }
                    }
                    realm.commitTransaction();
                }
            }
        });
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if( sbn == null )
            return;
        final Notification sbnNotification = sbn.getNotification();
            //final String sbnPackageName = sbn.getPackageName();
            //final String sbnTag = sbn.getTag();
            //final int sbnId = sbn.getId();
            //final long sbnPostTime = sbn.getPostTime();
        final ReplyAction replyAction = ReplyUtils.getQuickReplyAction(sbnNotification);

        if( replyAction == null )
            return;
        /*
        for( String key : sbnNotification.extras.keySet() ) {
            Object extraObj = sbnNotification.extras.get(key);
            if( extraObj != null )
                Log.d(TAG, key + ": " + extraObj );
        }*/

        StarfangCallable callable = new StarfangCallable( replyAction.getContentText(), replyAction.getSendCat() );
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String[]> future = executorService.submit(callable);

        try {
            String[] answers = future.get();
            if( answers.length <= 2 ) {
                for (String answer : answers) {
                    replyAction.sendReply(this, answer);
                }
            } else {
                StringBuilder sb = new StringBuilder("---");
                for (String answer : answers) {
                    sb.append("\r\n").append( answer ).append("\r\n");
                }
                sb.append("---");
                replyAction.sendReply(this, sb.toString());
            }

        } catch (ExecutionException | InterruptedException | PendingIntent.CanceledException e) {
            SystemMessage.insertMessage(e.toString(), "com.starfang.error", this);
        }



    }

    private static class StarfangCallable implements Callable<String[]> {
        private static final String MASTER_CMD_CAT = "냥";
        private final String sendCat;
        private final String text;

        public StarfangCallable( String text, String sendCat) {
            this.sendCat = sendCat;
            this.text = text.trim();
        }
        @Override
        public String[] call() {
            if (text.length() > 2) {
                List<String> answers = text.startsWith(MASTER_CMD_CAT) ? LambdaCat.processReq(text.substring(1)) :
                        text.endsWith(MASTER_CMD_CAT) ? LambdaRok.processReq( text.substring(0, text.length() - 1), sendCat, 0L)
                                : null;
                if( answers != null )
                    return answers.toArray( new String[0] );
            }
            return new String[0];
        }
    }


}