package com.starfang;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.starfang.realm.notifications.Conversation;
import com.starfang.realm.notifications.Forum;
import com.starfang.realm.notifications.Notifications;
import com.starfang.services.StarfangService;
import com.starfang.utilities.VersionUtils;

import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.Sort;

public class TalkActivity extends AppCompatActivity {

    private final static String TAG = "FANG_ACT_TALK";

    //private NotificationManager mNM;
    private static final int NOTIFICATION_ID = 303;
    private static final String CHANNEL_ID = "channel-starfang";
    private static final String CHANNEL_NAME = "Starfang";
    private static final String CONTENT_SUFFIX = "\r\n＿＿＿＿＿＿＿＿＿＿＿\r\nreply by Starfang app";

    private Realm realm;
    private BroadcastReceiver mReceiver;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private TalkAdapter mAdapter;
    private long forumId;
    //private ReplyAction replyAction;


    @Override
    public void onDestroy() {
        if (realm != null) {
            realm.close();
            realm = null;
        }

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        //mNM.cancel(NOTIFICATION_ID);
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);
        Log.d(TAG, "onCreate");
        Intent intent = getIntent();
        this.realm = Realm.getDefaultInstance();
        forumId = intent.getLongExtra(StarfangConstants.INTENT_KEY_FORUM_ID, 0L);
        final Forum forum = realm.where(Forum.class).equalTo(Forum.FIELD_ID, forumId).findFirst();

        if (forum != null) {
            OrderedRealmCollection<Conversation> collection =
                    forum.getConversationList().sort(Conversation.FIELD_WHEN, Sort.ASCENDING);
            mAdapter = new TalkAdapter(collection, false, this.getBaseContext());
            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView = findViewById(R.id.recycler_talks);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);

            final AppCompatEditText text_conversation = findViewById(R.id.text_conversation);
            final AppCompatImageButton button_clear_talk = findViewById(R.id.button_clear_talk);
            final FloatingActionButton button_send_talk = findViewById(R.id.button_send_talk);

            text_conversation.addTextChangedListener(
                    new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (TextUtils.isEmpty(s)) {
                                button_clear_talk.setVisibility(View.GONE);
                                button_send_talk.setVisibility(View.INVISIBLE);
                            } else {
                                if (button_clear_talk.getVisibility() != View.VISIBLE)
                                    button_clear_talk.setVisibility(View.VISIBLE);
                                if (button_send_talk.getVisibility() != View.VISIBLE)
                                    button_send_talk.setVisibility(View.VISIBLE);
                                if (!button_send_talk.isEnabled())
                                    button_send_talk.setEnabled(true);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (TextUtils.isEmpty(s)) {
                                button_clear_talk.setVisibility(View.GONE);
                                button_send_talk.setVisibility(View.INVISIBLE);
                            } else {
                                if (button_clear_talk.getVisibility() != View.VISIBLE)
                                    button_clear_talk.setVisibility(View.VISIBLE);
                                if (button_send_talk.getVisibility() != View.VISIBLE)
                                    button_send_talk.setVisibility(View.VISIBLE);
                                if (!button_send_talk.isEnabled())
                                    button_send_talk.setEnabled(true);
                            }
                        }
                    });

            button_clear_talk.setOnClickListener(v -> {
                button_send_talk.setEnabled(false);
                text_conversation.setText(null);
            });

            button_send_talk.setOnClickListener(v -> {
                button_send_talk.setEnabled(false);
                Editable editable = text_conversation.getText();
                if (TextUtils.isEmpty(editable)) {
                    button_send_talk.setVisibility(View.INVISIBLE);
                    button_clear_talk.setVisibility(View.GONE);
                } else {
                    text_conversation.setText(null);
                    final String content = editable.toString();

                    OrderedRealmCollection<Conversation> talks = mAdapter.getData();
                    if( talks != null ) {
                        for (Conversation talk : talks.where().isNotNull(Conversation.FIELD_NOTIFICATION).findAll()) {
                            Notifications log = talk.getNotification();
                            if( log != null ) {
                                Integer replyId = log.getSbnId();
                                String replyTag = log.getTag();
                                Intent replyIntent = new Intent(TalkActivity.this, StarfangService.class);
                                replyIntent.putExtra("reply_content",content + CONTENT_SUFFIX);
                                replyIntent.putExtra("reply_tag",replyTag);
                                replyIntent.putExtra("reply_id", replyId);
                                Log.d(TAG,"replyId:" + replyId);
                                startService(replyIntent);
                                break;
                            }
                        }
                    }


                    realm.executeTransactionAsync(bgRealm -> {
                        Conversation conversation = new Conversation();
                        conversation.itIsMe();
                        conversation.setSendCat("냥냥이");
                        conversation.setWhen(System.currentTimeMillis());
                        conversation.setContent(content);
                        conversation = bgRealm.copyToRealm(conversation);
                        Forum bgForum = bgRealm.where(Forum.class).equalTo(Forum.FIELD_ID, forumId).findFirst();
                        if (bgForum != null) {
                            bgForum.addConversation(conversation);
                        }
                        //forum.addConversation(conversation);
                    }, () -> mAdapter.notifyDataSetChanged());

                }
            });
        }

        this.mReceiver = new NotifyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StarfangService.ACTION_CONVERSATION_ADDED);
        registerReceiver(mReceiver, intentFilter);
    }

    private class NotifyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAdapter != null) {

                if (intent.getLongExtra(StarfangConstants.INTENT_KEY_FORUM_ID, 0L) == forumId) {
                    Log.d(TAG, "notify : change ui");
                    mAdapter.notifyDataSetChanged();
                    //replyAction = intent.getParcelableExtra("replyAction");

                    int itemPosition = mLayoutManager.findLastVisibleItemPosition();
                    int itemLastPosition = (mAdapter.getItemCount() - 1);

                    if (mRecyclerView != null
                            && itemLastPosition >= 0
                            && itemPosition > itemLastPosition - 4) {
                        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                    }
                }

            }
        }
    }


    static class TalkAdapter extends RealmRecyclerViewAdapter<Conversation, RecyclerView.ViewHolder> {

        private WeakReference<Context> contextWeakReference;

        TalkAdapter(@Nullable OrderedRealmCollection<Conversation> data, boolean autoUpdate, Context context) {
            super(data, autoUpdate);
            contextWeakReference = new WeakReference<>(context);
        }

        @NonNull
        @Override
        public TalkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_talk_list_row, parent, false);
            return new TalkViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Conversation talk = getItem(position);
            TalkViewHolder viewHolder = (TalkViewHolder) holder;
            viewHolder.bind(talk);
        }

        class TalkViewHolder extends RecyclerView.ViewHolder {

            private AppCompatImageView image_profile;
            private AppCompatTextView text_talk_sendCat;
            private AppCompatTextView text_talk_content;
            private AppCompatTextView text_talk_timestamp;

            TalkViewHolder(View itemView) {
                super(itemView);
                image_profile = itemView.findViewById(R.id.image_profile);
                text_talk_sendCat = itemView.findViewById(R.id.text_talk_sendCat);
                text_talk_content = itemView.findViewById(R.id.text_talk_content);
                text_talk_timestamp = itemView.findViewById(R.id.text_talk_timestamp);
            }

            void bind(final Conversation talk) {
                if (talk != null) {

                    if (VersionUtils.isJellyBeanMR1()) {
                        if (talk.isMe()) {
                            image_profile.setVisibility(View.GONE);
                            this.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                        } else {
                            image_profile.setVisibility(View.VISIBLE);
                            this.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                        }
                    }

                    text_talk_sendCat.setText(talk.getSendCat());
                    text_talk_content.setText(talk.getContent());
                    text_talk_timestamp.setText(new SimpleDateFormat("aa hh:mm", Locale.KOREA).format(
                            new Date(talk.getWhen())
                    ));


                }
            }
        }


    }

}
