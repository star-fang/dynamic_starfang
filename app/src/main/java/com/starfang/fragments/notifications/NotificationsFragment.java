package com.starfang.fragments.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.starfang.R;
import com.starfang.StarfangConstants;
import com.starfang.realm.notifications.Notifications;
import com.starfang.services.StarfangService;
import com.starfang.utilities.ServiceUtils;

import org.jetbrains.annotations.NotNull;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.Sort;

public class NotificationsFragment extends Fragment {
    /*
    onAttach <> onDetach
    onCreate <> onDestroy
    onCreateView <> on DestroyView
    onActivityCreated
    onStart <> onStop
    onResume <> onPause
    Active

     */
    private final static String TAG = "FANG_FRAG_NOTIFY";


    private BroadcastReceiver mReceiver;
    private NotificationAdapter mAdapter;
    private Context mContext;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private Realm realm;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if (mContext != null) {
            this.mReceiver = new NotifyReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(StarfangService.ACTION_NOTIFICATION_ADDED);
            mContext.registerReceiver(mReceiver, intentFilter);
        }

        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (mContext != null)
            mContext.unregisterReceiver(mReceiver);
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }


    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mRecyclerView = view.findViewById(R.id.recycler_notifications);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        OrderedRealmCollection<Notifications> collection =
                realm.where(Notifications.class).findAll().sort(Notifications.FIELD_WHEN, Sort.ASCENDING);
        Log.d(TAG, collection.size() + "record(s) found");
        mAdapter = new NotificationAdapter(collection, false);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);


        FloatingActionButton fab = view.findViewById(R.id.button_setting);
        fab.setOnClickListener(v -> {

            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);

        });

        SwitchCompat switch_record = view.findViewById(R.id.switch_record);

        SharedPreferences sharedPref = mContext.getSharedPreferences(
                StarfangConstants.SHARED_PREF_STORE,
                Context.MODE_PRIVATE);

        int recordStatus = sharedPref.getInt(
                StarfangConstants.BOT_RECORD_KEY,
                StarfangConstants.BOT_STATUS_STOP);
        Log.d(TAG, "record status : " + recordStatus);
        switch_record.setChecked(recordStatus == StarfangConstants.BOT_STATUS_START);
        switch_record.setOnCheckedChangeListener((v, isChecked) -> {
            Log.d(TAG, "switch_record clicked");
            if (isChecked) {
                if (sharedPref.edit().putInt(
                        StarfangConstants.BOT_RECORD_KEY,
                        StarfangConstants.BOT_STATUS_START).commit()) {
                    //Snackbar.make(root, "대화 녹화 시작", Snackbar.LENGTH_SHORT).show();
                } else {
                    //Snackbar.make(root, "대화 녹화 시작 실패", Snackbar.LENGTH_SHORT).show();
                    switch_record.setChecked(false);
                }

            } else {
                if (sharedPref.edit().putInt(
                        StarfangConstants.BOT_RECORD_KEY,
                        StarfangConstants.BOT_STATUS_STOP).commit()) {
                    //Snackbar.make(root, "대화 녹화 정지", Snackbar.LENGTH_SHORT).show();
                } else {
                    //Snackbar.make(root, "대화 녹화 정지 실패", Snackbar.LENGTH_SHORT).show();
                    switch_record.setChecked(true);
                }
            }
        });

        SwitchCompat switch_bot = view.findViewById(R.id.switch_bot);

        switch_bot.setChecked(false);
        switch_bot.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) {
                if (!ServiceUtils.isServiceExist(mContext, StarfangService.class, false)) {
                    //Snackbar.make(root, "알림 읽기 권한 설정 하세요", Snackbar.LENGTH_SHORT).show();
                    switch_bot.setChecked(false);
                } else {
                    if (sharedPref.edit().putInt(
                            StarfangConstants.BOT_STATUS_KEY,
                            StarfangConstants.BOT_STATUS_START).commit()) {
                        //Snackbar.make(root, "냥봇 시작", Snackbar.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext, StarfangService.class);
                        intent.putExtra(
                                StarfangConstants.BOT_STATUS_KEY,
                                StarfangConstants.BOT_STATUS_START);
                        mContext.startService(intent);
                    } else {
                        switch_bot.setChecked(false);
                        //Snackbar.make(root, "냥봇 시작 실패", Snackbar.LENGTH_SHORT).show();
                    }


                }
            } else {
                if (sharedPref.edit().putInt(
                        StarfangConstants.BOT_STATUS_KEY,
                        StarfangConstants.BOT_STATUS_STOP).commit()) {
                    //Snackbar.make(root, "냥봇 정지", Snackbar.LENGTH_SHORT).show();
                    Intent intent = new Intent(mContext, StarfangConstants.class);
                    intent.putExtra(
                            StarfangConstants.BOT_STATUS_KEY,
                            StarfangConstants.BOT_STATUS_STOP);
                    mContext.startService(intent);
                } else {
                    //Snackbar.make(root, "냥봇 정지 실패", Snackbar.LENGTH_SHORT).show();
                    switch_bot.setChecked(true);
                }


            }
        });
        int botStatus = sharedPref.getInt(
                StarfangConstants.BOT_STATUS_KEY,
                StarfangConstants.BOT_STATUS_STOP);
        Log.d(TAG, "sharedPref botStatus : " + botStatus);

        if (botStatus == StarfangConstants.BOT_STATUS_START) {
            if (ServiceUtils.isServiceExist(mContext, StarfangService.class, false)) {
                Log.d(TAG, "service already bound: start service");
                switch_bot.setChecked(true);
            } else {
                if (sharedPref.edit().putInt(
                        StarfangConstants.BOT_STATUS_KEY,
                        StarfangConstants.BOT_STATUS_STOP).commit()) {
                    Log.d(TAG, "service not exist: rewrite sharedPref: status stop");
                } else {
                    Log.d(TAG, "fail to change shared preference");
                }
            }
        }

    }

    private class NotifyReceiver extends BroadcastReceiver {

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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
        this.mContext = context;
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        realm.close();
        Log.d(TAG, "onDetach");
    }

    private static class NotificationAdapter extends RealmRecyclerViewAdapter<Notifications, RecyclerView.ViewHolder> {


        NotificationAdapter(@Nullable OrderedRealmCollection<Notifications> data, boolean autoUpdate) {
            super(data, autoUpdate);
            Log.d(TAG, "NotificationAdapter constructed");
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_notifications_list_row, parent, false);
            return new NotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Notifications notification = getItem(position);
            NotificationViewHolder viewHolder = (NotificationViewHolder) holder;
            viewHolder.bind(notification);
        }

        private static class NotificationViewHolder extends RecyclerView.ViewHolder {
            //private TextView text_id;
            private TextView text_package;
            //private TextView text_is_active;
            //private TextView text_when;
            //private TextView text_tag;

            NotificationViewHolder(View itemView) {
                super(itemView);
                //Log.d(TAG, "viewHolder constructed");
                //text_id = itemView.findViewById(R.id.notification_id);
                text_package = itemView.findViewById(R.id.notification_package);
                //text_is_active = itemView.findViewById(R.id.notification_is_active);
                //text_when = itemView.findViewById(R.id.notification_when);
                //text_tag = itemView.findViewById(R.id.notification_tag);
            }

            void bind(final Notifications notification) {
                if (notification != null) {
                    //Log.d(TAG, notification.getActiveId() + "bound!!!");
                    //text_id.setText(String.valueOf(notification.getActiveId()));
                    text_package.setText(notification.getAppPackage());
                    //text_is_active.setText(notification.isActive() ? "O" : "X");
                    //text_when.setText(String.valueOf(notification.getWhen()));
                    //text_tag.setText(String.valueOf(notification.getTag()));
                }
            }
        }


    }


}
