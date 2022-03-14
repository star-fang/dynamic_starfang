package com.starfang.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.starfang.R;
import com.starfang.nlp.CmdProcessor;
import com.starfang.nlp.SystemMessage;
import com.starfang.realm.Cmd;
import com.starfang.utilities.ScreenUtils;
import com.starfang.activities.viewmodel.CMDActivityViewModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.Sort;

public class CMDActivity extends AppCompatActivity {
    private final static String TAG = "FANG_ACT_CMD";

    public static final String ACTION_SCROLL_TO_BOTTOM = "to_bottom";
    public static final String ACTION_REPLY = "reply";
    public static final String KEY_REPLY_INFO = "reply_info";
    public static final String KEY_REPLY_RESULT = "reply_result";
    private static final int RC_SIGN_IN = 9001;

    public static final String NOTIFICATION_CHANNEL_ID = "starfang_cmd";
    public static final String channelName = "Starfang Cmd Channel";

    private Realm realm;
    private CMDActivityViewModel mViewModel;
    private NotificationManager mNotificationManager;
    private static RecyclerView mRecyclerView;
    private static CMDAdapter mAdapter;
    private static AppCompatEditText text_conversation;
    private static FloatingActionButton button_send_talk;
    private final static BroadcastReceiver mReceiver = new NotifyReceiver();

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SCROLL_TO_BOTTOM);
        intentFilter.addAction(ACTION_REPLY);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        //unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (realm != null) {
            realm.close();
            realm = null;
        }

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

    private void scrollToBottom() {
        if( mRecyclerView != null && mAdapter != null )
            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmd);
        Log.d(TAG, "onCreate");


        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if( mNotificationManager != null )
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE );
                mNotificationManager.createNotificationChannel(channel);
        }

        mViewModel = new ViewModelProvider(this).get(CMDActivityViewModel.class);
        FirebaseFirestore.setLoggingEnabled(true);
        initFireStore();

        this.realm = Realm.getDefaultInstance();
        RealmResults<Cmd> cmdTalks = realm.where(Cmd.class).findAll().sort(Cmd.FIELD_WHEN, Sort.ASCENDING);

        mAdapter = new CMDAdapter(cmdTalks, false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView = findViewById(R.id.recycler_cmd);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new CMDAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                ScreenUtils.hideSoftKeyboard(CMDActivity.this);
            }

            @Override
            public void onItemLongClick(int position, View v) {
                if (v instanceof AppCompatTextView) {
                    CharSequence cs = ((AppCompatTextView) v).getText();
                    if (!StringUtils.isEmpty(cs)) {
                        String txt = cs.toString();
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        if (clipboardManager != null) {
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("Starfang", txt));
                            Toast.makeText(CMDActivity.this.getBaseContext(), "클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        scrollToBottom();
        text_conversation = findViewById(R.id.text_conversation);
        final AppCompatImageButton button_clear_talk = findViewById(R.id.button_clear_talk);
        button_send_talk = findViewById(R.id.button_send_talk);

        final AppCompatImageButton button_to_bottom = findViewById(R.id.button_to_bottom);
        button_to_bottom.setOnClickListener(v -> scrollToBottom());

        mViewModel.getProcessCountLiveData().observe(this, count -> {
            if (count > 0) {
                button_send_talk.setEnabled(false);
                setAbleET(text_conversation, false);
                ScreenUtils.hideSoftKeyboard(CMDActivity.this);
                Log.d(TAG, "disable et");
            } else {
                button_send_talk.setEnabled(true);
                setAbleET(text_conversation, true);
                Log.d(TAG, "enable et");
            }
        });

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


                realm.executeTransactionAsync(bgRealm -> {
                    Cmd talk = new Cmd( true);
                    talk.setText(content);
                    bgRealm.copyToRealm(talk);
                }, () -> {
                    mAdapter.notifyDataSetChanged();
                    scrollToBottom();
                });

                CmdProcessor processor = new CmdProcessor(this, mNotificationManager, mViewModel);
                processor.execute(content);
                Log.d(TAG, content);


            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();

        // Start sign in if necessary
        // if (shouldStartSignIn()) {
        //     startSignIn();
        //     return;
        // }

        // Apply filters
        //onFilter(mViewModel.getFilters());

    }


    private static void notifyAdapter() {
        if( mAdapter != null ) {
            mAdapter.notifyDataSetChanged();
            if( mRecyclerView != null )
                mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    public static class NotifyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "broadcast received");
            String action = intent.getAction();
            if( action == null )
                return;
            if (action.equals(ACTION_SCROLL_TO_BOTTOM)) {
                notifyAdapter();
            } else if( action.equals(ACTION_REPLY)) {
                final Bundle replyInfo = intent.getBundleExtra(KEY_REPLY_INFO);
                final CharSequence resultChars = RemoteInput.getResultsFromIntent(intent).getCharSequence(KEY_REPLY_RESULT);
                if( replyInfo == null || resultChars == null )
                    return;
                try ( Realm realm = Realm.getDefaultInstance() ) {
                    realm.executeTransactionAsync( bgRealm-> {
                       Cmd cmd = new Cmd( false );
                       cmd.setName(replyInfo.getString(Notification.EXTRA_TITLE) );
                       cmd.setText(resultChars.toString());
                       bgRealm.copyToRealm( cmd );
                    }, CMDActivity::notifyAdapter);
                } catch ( RuntimeException e ) {
                    SystemMessage.insertMessage( e.toString(), "com.starfang.error", context);
                }
            }

        }
    }


    static class CMDAdapter extends RealmRecyclerViewAdapter<Cmd, RecyclerView.ViewHolder> {

        private static ClickListener clickListener;

        CMDAdapter(@Nullable OrderedRealmCollection<Cmd> data, boolean autoUpdate) {
            super(data, autoUpdate);
        }

        @NonNull
        @Override
        public CMDAdapter.TalkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_cmd_list_row, parent, false);
            return new TalkViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            CMDAdapter.TalkViewHolder viewHolder = (CMDAdapter.TalkViewHolder) holder;
            viewHolder.bind(position > 0 ? getItem(position - 1) : null
                    , getItem(position)
                    , position < getItemCount() - 1 ? getItem(position + 1) : null);
        }

        static class TalkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

            private final AppCompatTextView text_talk_sendCat;
            private final AppCompatTextView text_talk_content;
            private final AppCompatTextView text_talk_timestamp;


            TalkViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
                text_talk_sendCat = itemView.findViewById(R.id.text_talk_sendCat);
                text_talk_content = itemView.findViewById(R.id.text_talk_content);
                text_talk_timestamp = itemView.findViewById(R.id.text_talk_timestamp);
            }

            void bind(final Cmd preTalk, final Cmd talk, final Cmd postTalk) {
                if (talk != null) {
                    final String name = talk.getName();
                    final boolean isUser = talk.isUser();
                    final long when = talk.getWhen();

                    if (isUser) {
                        this.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                    } else {
                        this.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                    }

                    if (compareCard(name, when, isUser, preTalk)) {
                        text_talk_sendCat.setVisibility(View.GONE);
                    } else {
                        text_talk_sendCat.setVisibility(View.VISIBLE);
                        text_talk_sendCat.setText(name);
                    }

                    if (compareCard(name, when, isUser, postTalk)) {
                        text_talk_timestamp.setVisibility(View.GONE);
                    } else {
                        text_talk_timestamp.setVisibility(View.VISIBLE);
                        text_talk_timestamp.setText(new SimpleDateFormat("aa hh:mm", Locale.KOREA).format(
                                new Date(when)
                        ));
                    }

                    text_talk_content.setText(talk.getText());


                }
            } // bind

            private static boolean compareCard(String name, long when, boolean isUser, Cmd other) {
                if (other != null) {
                    final String otherName = other.getName();
                    if (otherName != null && otherName.equals(name)) {
                        return other.isUser() == isUser
                                && other.getWhen() / 60000 == when / 60000;
                    }
                }
                return false;
            }

            @Override
            public void onClick(View v) {
                clickListener.onItemClick(getAdapterPosition(), v);
            }

            @Override
            public boolean onLongClick(View v) {
                clickListener.onItemLongClick(getAdapterPosition(), v);
                return false;
            }
        }

        void setOnItemClickListener(ClickListener clickListener) {
            CMDAdapter.clickListener = clickListener;
        }

        interface ClickListener {
            void onItemClick(int position, View v);

            void onItemLongClick(int position, View v);
        }

    }

    private static void setAbleET(AppCompatEditText et, boolean able) {
        et.setEnabled(able);
        et.setClickable(able);
        et.setFocusable(able);
        et.setFocusableInTouchMode(able);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            //mViewModel.setIsSigningIn(false);

            if (resultCode != RESULT_OK && shouldStartSignIn()) {
                startSignIn();
            }
        }
    }

    private void initFireStore() {
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    }

    private void startSignIn() {
        // Sign in with FirebaseUI
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                .setIsSmartLockEnabled(false)
                .build();

        startActivityForResult(intent, RC_SIGN_IN);
        //mViewModel.setIsSigningIn(true);
    }

    private boolean shouldStartSignIn() {
        return (!mViewModel.getIsSigningIn().getValue() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

}
