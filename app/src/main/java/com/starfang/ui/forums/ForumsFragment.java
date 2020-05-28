package com.starfang.ui.forums;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.starfang.TalkActivity;
import com.starfang.R;
import com.starfang.StarfangConstants;
import com.starfang.realm.notifications.Forums;
import com.starfang.services.StarfangService;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.Sort;

public class ForumsFragment extends Fragment {
    private final static String TAG = "FANG_FRAG_FORUM";

    private BroadcastReceiver mReceiver;
    private ForumAdapter mAdapter;
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
            intentFilter.addAction(StarfangService.ACTION_CONVERSATION_ADDED);
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
        ForumsViewModel notificationsViewModel = new ViewModelProvider(this).get(ForumsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_forums, container, false);
        final TextView textView = root.findViewById(R.id.text_forums);
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }


    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.recycler_forums);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        OrderedRealmCollection<Forums> collection =
                realm.where(Forums.class).findAll().sort(Forums.FIELD_LAST_MODIFIED, Sort.DESCENDING);
        Log.d(TAG, collection.size() + "record(s) found");
        mAdapter = new ForumAdapter(collection, false, mContext);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());


    }

    private class NotifyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAdapter != null) {
                Log.d(TAG, "notify : change ui");
                mAdapter.notifyDataSetChanged();
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

    private static class ForumAdapter extends RealmRecyclerViewAdapter<Forums, RecyclerView.ViewHolder> {

        private WeakReference<Context> contextWeakReference;

        ForumAdapter(@Nullable OrderedRealmCollection<Forums> data, boolean autoUpdate, Context context) {
            super(data, autoUpdate);
            contextWeakReference = new WeakReference<>(context);
        }

        @NonNull
        @Override
        public ForumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_forums_list_row, parent, false);
            return new ForumViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Forums forum = getItem(position);
            ForumViewHolder viewHolder = (ForumViewHolder) holder;
            viewHolder.bind(forum);
        }

        private class ForumViewHolder extends RecyclerView.ViewHolder {
            AppCompatTextView text_forum_name;
            AppCompatTextView text_forum_last_conversation;
            AppCompatTextView text_forum_last_modified;
            AppCompatTextView text_forum_count_non_read;

            ForumViewHolder(View itemView) {
                super(itemView);
                text_forum_name = itemView.findViewById(R.id.text_forum_name);
                text_forum_last_conversation = itemView.findViewById(R.id.text_forum_last_conversation);
                text_forum_last_modified = itemView.findViewById(R.id.text_forum_last_modified);
                text_forum_count_non_read = itemView.findViewById(R.id.text_forum_count_non_read);
            }

            void bind(final Forums forum) {
                if (forum != null) {
                    text_forum_name.setText(forum.getName());
                    text_forum_last_conversation.setText(forum.getLastSimpleConversation());
                    text_forum_count_non_read.setText(forum.getNonReadCount());
                    long lastModified = forum.getLastModified();
                    Calendar calendar_lm = Calendar.getInstance();
                    calendar_lm.setTimeInMillis(lastModified);
                    Calendar calendar_now = Calendar.getInstance();
                    calendar_now.setTime(new Date());
                    int year_now = calendar_now.get(Calendar.YEAR);
                    int year_lm = calendar_lm.get(Calendar.YEAR);
                    int day_of_year_now = calendar_now.get(Calendar.DAY_OF_YEAR);
                    int day_of_year_lm = calendar_lm.get(Calendar.DAY_OF_YEAR);
                    String dateFormat;
                    if (year_lm == year_now) {
                        if (day_of_year_lm == day_of_year_now) {
                            dateFormat = "hh.mm aa";
                        } else if (day_of_year_lm == day_of_year_now - 1) {
                            dateFormat = "어제";
                        } else {
                            dateFormat = "MM월 dd일";
                        }
                    } else {
                        dateFormat = "yyyy.MM.dd";
                    }

                    text_forum_last_modified.setText(new SimpleDateFormat(dateFormat, Locale.KOREA).format(new Date(lastModified)));
                    this.itemView.setOnClickListener( view -> {
                        Context context = contextWeakReference.get();
                        Intent intent = new Intent(context, TalkActivity.class);
                        intent.putExtra(StarfangConstants.INTENT_KEY_FORUM_ID,forum.getId());
                        context.startActivity(intent);
                    });

                }
            }
        }


    }

}
