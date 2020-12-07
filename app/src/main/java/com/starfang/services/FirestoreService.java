package com.starfang.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.starfang.CMDActivity;
import com.starfang.R;

import org.apache.commons.lang3.math.NumberUtils;

import io.realm.Realm;

public class FirestoreService extends Service implements EventListener<QuerySnapshot> {
    private static final String TAG = "FANG_FIRESTORE_SERVICE";
    private static final String CHANNEL_ID = "FirestoreServiceChannel";
    public static final String FS_MESSAGE = "fsm";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra(FS_MESSAGE);

        // 안드로이드 O버전 이상에서는 알림창을 띄워야 포그라운드 사용 가능
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, CMDActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("냥봇")
                .setContentText(input).setSmallIcon(R.mipmap.ic_launcher).setContentIntent(pendingIntent).build();
        startForeground(1, notification);


        // stopSelf();
        /* START_STICKY : Service가 강제 종료되었을 경우 시스템이 다시 Service를 재시작 시켜 주지만
        intent 값을 null로 초기화 시켜서 재시작 합니다.  Service 실행시 startService(Intent service)
        메서드를 호출 하는데 onStartCommand(Intent intent, int flags, int startId) 메서드에 intent로
        value를 넘겨 줄 수 있습니다. 기존에 intent에 value값이 설정이 되있다고 하더라도 Service 재시작시
        intent 값이 null로 초기화 되서 재시작 됩니다.

        START_NOT_STICKY : 이 Flag를 리턴해 주시면, 강제로 종료 된 Service가 재시작 하지 않습니다.
        시스템에 의해 강제 종료되어도 괸찮은 작업을 진행 할 때 사용해 주시면 됩니다.*/
        return START_NOT_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID
                    , "Firestore Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(serviceChannel);
        }
    }


    @Override
    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
        // Handle errors
        if (error != null) {
            Log.w(TAG, "onEvent:error", error);
            return;
        }

        if( value == null)
            return;

        // Dispatch the event
        for (DocumentChange change : value.getDocumentChanges()) {
            // Snapshot of the changed document
            DocumentSnapshot snapshot = change.getDocument();
            int id = NumberUtils.toInt(snapshot.getId(), -1);
            if (id > -1) {
                try(Realm realm = Realm.getDefaultInstance()) {

                }
                switch (change.getType()) {
                    case ADDED:
                        Log.d(TAG, "add : " + snapshot.toString() );
                        break;
                    case MODIFIED:
                        Log.d(TAG, "modified : " + snapshot.toString() );
                        break;
                    case REMOVED:
                        Log.d(TAG, "removed : " + snapshot.toString() );
                        break;
                }
            }
        }
    }
}
