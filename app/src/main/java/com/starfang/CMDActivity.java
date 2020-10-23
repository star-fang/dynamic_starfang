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
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.starfang.nlp.CmdProcessor;
import com.starfang.nlp.FangcatNlp;
import com.starfang.realm.Cmd;
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
import io.realm.RealmResults;
import io.realm.Sort;

public class CMDActivity extends AppCompatActivity {
    private final static String TAG = "FANG_ACT_CMD";

    public static final String ACTION_CMD_ADDED = "addCMD";

    private Realm realm;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private CMDAdapter mAdapter;
    private NotifyReceiver mReceiver;


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
        if( mRecyclerView != null && mAdapter != null ) {
            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmd);
        Log.d(TAG, "onCreate");
        this.realm = Realm.getDefaultInstance();
        RealmResults<Cmd> cmdTalks = realm.where(Cmd.class).findAll().sort(Cmd.FIELD_WHEN, Sort.ASCENDING);

        if (cmdTalks.size() == 0) {
            Cmd welcomeCmd = new Cmd( false );
            welcomeCmd.setName("멍멍이");
            welcomeCmd.setText("연결 -> 알림 -> 시작 순서로 입력 하라멍");
            realm.beginTransaction();
            realm.copyToRealm(welcomeCmd);
            realm.commitTransaction();
        }

        mAdapter = new CMDAdapter(cmdTalks, false, this.getBaseContext());
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView = findViewById(R.id.recycler_cmd);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        scrollToBottom();
        final AppCompatEditText text_conversation = findViewById(R.id.text_conversation);
        final AppCompatImageButton button_clear_talk = findViewById(R.id.button_clear_talk);
        final FloatingActionButton button_send_talk = findViewById(R.id.button_send_talk);

        final AppCompatImageButton button_to_bottom = findViewById(R.id.button_to_bottom);
        button_to_bottom.setOnClickListener( v-> scrollToBottom());


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
                    Cmd talk = new Cmd();
                    //talk.setName("얼간이");
                    talk.setText(content);
                    bgRealm.copyToRealm(talk);
                    //forum.addConversation(conversation);
                }, () -> {
                    mAdapter.notifyDataSetChanged();
                    scrollToBottom();
                });

                CmdProcessor processor = new CmdProcessor(this );
                processor.execute(content);
                Log.d(TAG,content);


            }
        });

        this.mReceiver = new NotifyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CMD_ADDED);
        registerReceiver(mReceiver, intentFilter);

    }


    public class NotifyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAdapter != null) {
                Log.d(TAG, "notify : change ui");
                mAdapter.notifyDataSetChanged();

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


    static class CMDAdapter extends RealmRecyclerViewAdapter<Cmd, RecyclerView.ViewHolder> {

        private WeakReference<Context> contextWeakReference;

        CMDAdapter(@Nullable OrderedRealmCollection<Cmd> data, boolean autoUpdate, Context context) {
            super(data, autoUpdate);
            contextWeakReference = new WeakReference<>(context);
        }

        @NonNull
        @Override
        public CMDAdapter.TalkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_cmd_list_row, parent, false);
            return new TalkViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Cmd talk = getItem(position);
            CMDAdapter.TalkViewHolder viewHolder = (CMDAdapter.TalkViewHolder) holder;
            viewHolder.bind(talk);
        }

        static class TalkViewHolder extends RecyclerView.ViewHolder {

            private AppCompatTextView text_talk_sendCat;
            private AppCompatTextView text_talk_content;
            private AppCompatTextView text_talk_timestamp;

            TalkViewHolder(View itemView) {
                super(itemView);
                text_talk_sendCat = itemView.findViewById(R.id.text_talk_sendCat);
                text_talk_content = itemView.findViewById(R.id.text_talk_content);
                text_talk_timestamp = itemView.findViewById(R.id.text_talk_timestamp);
            }

            void bind(final Cmd talk) {
                if (talk != null) {

                    if (VersionUtils.isJellyBeanMR1()) {
                        if (talk.isUser()) {
                            this.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                        } else {
                            this.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                        }
                    }

                    text_talk_sendCat.setText(talk.getName());
                    text_talk_content.setText(talk.getText());
                    text_talk_timestamp.setText(new SimpleDateFormat("aa hh:mm", Locale.KOREA).format(
                            new Date(talk.getWhen())
                    ));


                }
            }
        }


    }

}
