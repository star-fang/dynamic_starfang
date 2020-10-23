package com.starfang.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.starfang.StarfangConstants;
import com.starfang.nlp.FangcatNlp;
import com.starfang.realm.notifications.Conversation;
import com.starfang.realm.notifications.Forum;
import com.starfang.realm.notifications.Notifications;
import com.starfang.utilities.VersionUtils;
import com.starfang.utilities.reply.ReplyAction;
import com.starfang.utilities.ReplyUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmResults;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class StarfangService extends NotificationListenerService {

    private static final String TAG = "FANG_SERVICE_NF";
    //private static final String KAKAO_CHAT_LOG_ID = "chatLogId";
    public static final String ACTION_CONVERSATION_DEACTIVATE = "deactivateConversation";
    public static final String ACTION_NOTIFICATION_ADDED = "addLog";
    public static final String ACTION_CONVERSATION_ADDED = "addConversation";

    private boolean isWorking;
    private boolean isBound;
    private SharedPreferences sharedPref;


    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = getSharedPreferences(
                StarfangConstants.SHARED_PREF_STORE,
                Context.MODE_PRIVATE);
        String start_count_key = StarfangConstants.BOT_START_COUNT_KEY;
        int startCount = sharedPref.getInt(start_count_key, 0) + 1;
        Log.d(TAG, "NotificationListenerService: " + startCount + "th(st|nd|rd) created");
        sharedPref.edit().putInt(start_count_key, startCount).apply();

        isWorking = false;
        isBound = false;
        super.stopSelf();
    }

    /*
    isWorking
    onCreate false
    onBind
    onStartCommand>start true
    onStartCommand>stop false
    onUnbind false
    onRebind true


    onDestroy >> false: normal // true: abnormal

    create>bind>start>stop>unbind>destroy
    create>bind>unbind>destroy
    create>bind>start>unbind>...>destroy
    create>bind>start>unbind>start>...>destroy>...
    create>bind>start>unbind>rebind>
    create>start>...>destroy>create>....

     */
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void reply(final int id, final String tag, @NonNull final String content) {

        Runnable runnable = () -> {
            for (StatusBarNotification sbn : getActiveNotifications()) {
                Notification notification = sbn.getNotification();
                int sbnId = sbn.getId();
                String sbnTag = sbn.getTag();
                //Log.d("TAG", "compare: " + sbnId + "vs" + id);
                if (notification != null && sbnId == id) {
                    if (tag == null || tag.equals(sbnTag)) {
                        ReplyAction replyAction = ReplyUtils.getQuickReplyAction(notification);
                        if (replyAction != null) {
                            //Log.d("TAG", "active notification found: " + id);
                            try {
                                replyAction.sendReply(getBaseContext(), content);
                                Log.d(TAG, "reply success");
                            } catch (PendingIntent.CanceledException e) {
                                Log.e(TAG, Log.getStackTraceString(e));
                            }
                            break;
                        }
                    }


                }
            }
        };

        AsyncTask.execute(runnable);
    }

    @Override
    public int onStartCommand(@NotNull Intent intent, int flags, int startId) {

        Log.d(TAG, "receive startCommand : [flags,startId] = ["
                + flags + "," + startId + "]");
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object replyIdObj = bundle.get("reply_id");
            if (replyIdObj instanceof Integer && VersionUtils.isKitKat()) {
                Integer replyId = (Integer) replyIdObj;
                String tag = bundle.getString("reply_tag", null);
                String content = bundle.getString("reply_content", null);
                if (content != null) {
                    Log.d(TAG, "command : reply to:" + replyId);
                    reply(replyId, tag, content);
                }
            } else {
                int command = bundle.getInt(
                        StarfangConstants.BOT_STATUS_KEY,
                        StarfangConstants.BOT_STATUS_START
                );
                if (command == StarfangConstants.BOT_STATUS_STOP) {
                /*
                *state and works
                 : activated + bound >> notification listener service works
                 : deactivated + bound >> it still works
                 : activated + unbound >> non-working service will be destroyed soon
                 : deactivated + unbound >> service destroyed immediately

                 *[activated] becomes
                          : true by onCreate(default)
                          : true by onStartCommand by startService
                          : false by stopSelf by onStartCommand or stopService
                 *startService, stopService
                          : activity or  foregroundService call
                 *startService by backgroundService
                          : forbidden since Oreo
                 *stopSelf << service call
                 */
                    if (isWorking = !this.stopSelfResult(startId)) {
                        Log.d(TAG, "fail to stop service");
                    } else {
                        Log.d(TAG, "switch off service");
                    }

                } else { // start or restart
                    if (command == StarfangConstants.BOT_STATUS_RESTART) { // restart
                        String restart_count_key = StarfangConstants.BOT_RESTART_COUNT_KEY;
                        int restartCount = sharedPref.getInt(
                                restart_count_key, 0) + 1;
                        Log.d(TAG, "service restart: " + restartCount + "th(st|nd|rd) restart command");
                        sharedPref.edit().putInt(restart_count_key, restartCount).apply();
                        isWorking = true;
                    } else if (isBound && !isWorking) {
                        isWorking = true;
                        Log.d(TAG, "service activated");
                    }
                    return super.onStartCommand(intent, flags, startId);
                }
            }
        }
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isWorking) {
            Log.d(TAG, "Notification Listener destroyed abnormally");
            final Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            c.add(Calendar.SECOND, 3);
            Intent intent = new Intent(StarfangService.this, RestartAlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(StarfangService.this, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
            }
        } else {
            sharedPref.edit().putInt(
                    StarfangConstants.BOT_STATUS_KEY,
                    StarfangConstants.BOT_STATUS_STOP
            ).apply();

            Log.d(TAG, "Notification Listener destroyed ");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Notification Listener bind");
        isBound = true;
        if (sharedPref.getInt(StarfangConstants.BOT_STATUS_KEY, StarfangConstants.BOT_STATUS_STOP) == StarfangConstants.BOT_STATUS_START) {
            isWorking = true;
            Log.d(TAG, "service already activated");
        }
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Notification Listener un-bind");
        isWorking = false;
        isBound = false;
        return true; // return true : make possible to rebind
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "Notification Listener re-bind");
        isWorking = true;
        isBound = true;
        super.onRebind(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        if (sbn != null && isWorking) {

            final Notification sbnNotification = sbn.getNotification();
            final String sbnPackageName = sbn.getPackageName();
            final String sbnTag = sbn.getTag();
            final int sbnId = sbn.getId();
            final long sbnPostTime = sbn.getPostTime();
            final ReplyAction replyAction = ReplyUtils.getQuickReplyAction(sbnNotification);

            if (replyAction != null) {

                //String botName = sharedPref.getString(
                //        StarfangConstants.BOT_NAME_KEY,
                //        getResources().getString(R.string.bot_name_default)
                //);
                for( String key : sbnNotification.extras.keySet() ) {
                    Object extraObj = sbnNotification.extras.get(key);
                    if( extraObj != null )
                    Log.d(TAG, key + ": " + extraObj );
                }
                Log.d(TAG, "---------------------------"  );
                new FangcatNlp(this, replyAction, replyAction.getSendCat(), 0L).execute(replyAction.getContentText());
                //new FangcatHandler(this, from, room, sbn, isLocalRequest, botName, record).execute(text);



            } // if isAvailablePackage

            Runnable runnable = () -> {
                int botRecord = sharedPref.getInt(
                        StarfangConstants.BOT_RECORD_KEY,
                        StarfangConstants.BOT_STATUS_START); // default : start record

                if (botRecord == StarfangConstants.BOT_STATUS_START) {

                    try (Realm realm = Realm.getDefaultInstance()) {
                        realm.executeTransaction(bgRealm -> {
                            //Log.d(TAG, "new notification: " + sbnId);
                            Notifications notificationLog = new Notifications(sbnId);
                            notificationLog.setTag(sbnTag);
                            notificationLog.setAppPackage(sbnPackageName);
                            notificationLog.setWhen(sbnPostTime);
                            notificationLog = bgRealm.copyToRealm(notificationLog);
                            if (replyAction != null) {
                                final String sendCat = replyAction.getSendCat();
                                final String forumName = replyAction.getForumName();
                                final long lastModified = sbnNotification.when;
                                Conversation conversation = new Conversation();
                                conversation.setContent(replyAction.getContentText());
                                conversation.setWhen(lastModified);
                                conversation.setSendCat(sendCat);
                                conversation = bgRealm.copyToRealm(conversation);
                                notificationLog.activate();
                                conversation.setNotification(notificationLog);
                                RealmResults<Forum> forums = bgRealm.where(Forum.class).equalTo(Forum.FIELD_TAG, sbnTag).findAll();
                                Forum forum;
                                final boolean isGroupChat = forumName != null;
                                final String refinedForumName = isGroupChat ? forumName : sendCat;
                                Log.d(TAG, sbn.getPackageName() + ">> from: " + sendCat + ", forum: " + refinedForumName);
                                switch (forums.size()) {
                                    case 0:
                                        forum = new Forum(sbnTag);
                                        forum.setPackageName(sbnPackageName);
                                        forum.setGroupChatOption(isGroupChat);
                                        forum.setName(refinedForumName);
                                        forum = bgRealm.copyToRealm(forum);
                                        break;
                                    case 1:
                                        forum = forums.first();
                                        break;
                                    default:
                                        forum = forums.where().equalTo(Forum.FIELD_PACKAGE_NAME, sbnTag).findFirst();
                                }

                                if (forum != null) {
                                    if (!forum.getName().equals(refinedForumName)) {
                                        forum.setName(refinedForumName);
                                    }
                                    forum.addConversation(conversation);
                                    //forum.setLastModified(lastModified);
                                    forum.countUpNonRead();
                                    Intent intent = new Intent();
                                    intent.setAction(ACTION_CONVERSATION_ADDED);
                                    //intent.putExtra("replyAction",replyAction);
                                    intent.putExtra(StarfangConstants.INTENT_KEY_FORUM_ID, forum.getId());
                                    sendBroadcast(intent);
                                }

                            } // if isConversation

                            Intent intent_notify = new Intent();
                            intent_notify.setAction(ACTION_NOTIFICATION_ADDED);
                            sendBroadcast(intent_notify);
                        });
                    } catch (RuntimeException e) {
                        Log.d(TAG, Log.getStackTraceString(e));
                    }
                }


            };
            AsyncTask.execute(runnable);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransactionAsync(bgRealm -> {
                RealmResults<Conversation> talks = bgRealm.where(Conversation.class)
                        .isNotNull(Conversation.FIELD_NOTIFICATION)
                        .equalTo(Conversation.FIELD_NOTIFICATION + "." + Notifications.FIELD_IS_ACTIVE, true)
                        .equalTo(Conversation.FIELD_NOTIFICATION + "." + Notifications.FIELD_SBN_ID, sbn.getId())
                        .equalTo(Conversation.FIELD_NOTIFICATION + "." + Notifications.FIELD_PACKAGE, sbn.getPackageName()).findAll();
                for (Conversation talk : talks) {
                    Notifications log = talk.getNotification();
                    if (log != null) {
                        log.deactivate();
                        Intent intent = new Intent();
                        intent.setAction(ACTION_CONVERSATION_DEACTIVATE);
                        intent.putExtra("id", talk.getId());
                        sendBroadcast(intent);
                    }
                }
            }, () -> Log.d(TAG, "deactivate notification"), error -> Log.e(TAG, Log.getStackTraceString(error)));
        } catch (RuntimeException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }


        //Log.d(TAG, "Notification [" + sbn.getKey() + "] Removed:\n");
    }

}