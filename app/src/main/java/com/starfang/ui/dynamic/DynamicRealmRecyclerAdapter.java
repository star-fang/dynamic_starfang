package com.starfang.ui.dynamic;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.starfang.R;
import com.starfang.realm.source.Source;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.realm.OrderedRealmCollection;
import io.realm.RealmObject;
import io.realm.RealmRecyclerViewAdapter;

public class DynamicRealmRecyclerAdapter<T extends RealmObject>
        extends RealmRecyclerViewAdapter<T, DynamicRealmRecyclerAdapter<T>.DynamicViewHolder> implements Filterable {

    private static final String TAG = "FANG_DYNAMIC_ADAPTER";
    private Set<String> fieldNames;
    private Filter filter;
    private OrderedRealmCollection<T> entireCollection;
    private WeakReference<DisplayMetrics> metricsWeakReference;

    DynamicRealmRecyclerAdapter(OrderedRealmCollection<T> collection, Set<String> fieldNames, boolean autoUpdate, boolean updateOnModification, DisplayMetrics displayMetrics) {
        super(collection, autoUpdate, updateOnModification);

        Log.d(TAG, getItemCount() + "record(s) added");

        this.entireCollection = collection;
        this.fieldNames = fieldNames;
        this.filter = new DynamicFilter();
        this.metricsWeakReference = new WeakReference<>(displayMetrics);
    }


    @NonNull
    @Override
    public DynamicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dynamic_row, parent, false);
        return new DynamicViewHolder(view);
    }

    public void onBindViewHolder(@NonNull DynamicRealmRecyclerAdapter.DynamicViewHolder holder, int position) {
        T record = getItem(position);
        if( record instanceof Source ) {
            holder.bind((Source)record);
        }
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    public class DynamicFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (!TextUtils.isEmpty(constraint)) {
                Gson gson = new Gson();
                JSONObject jsonObject = gson.fromJson(constraint.toString(), JSONObject.class);
                if (jsonObject != null) {
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = jsonObject;
                }
            }

            return null;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null) {
                // fieldname
                // filednames
                //updateData(realm.where(clazz).contains(Source.FIELD_NAME, "순").findAll());
            } else {
                //updateData(realm.where(clazz).findAll());
            }
            notifyDataSetChanged();
        }
    }

    public class DynamicViewHolder extends RecyclerView.ViewHolder {

        private Map<String, AppCompatTextView> textViews;

        DynamicViewHolder(@NonNull View itemView) {
            super(itemView);
            textViews = new HashMap<>();
            for (String field : fieldNames) {
                AppCompatTextView textView = new AppCompatTextView(itemView.getContext());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                float widthInPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,40, metricsWeakReference.get());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) widthInPixel,LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(5,2,5,2);
                textView.setLayoutParams(layoutParams);

                if (itemView instanceof LinearLayout) {
                    ((LinearLayout) itemView).addView(textView);
                    textViews.put(field, textView);
                }
            }
        } // viewHolder constructor

        void bind( @NonNull final Source source) {
            for (String field : fieldNames) {
                AppCompatTextView textView = textViews.get(field);
                if (textView != null) {
                    String data = source.getString(field);
                    textView.setText(data);
                    //Log.d(TAG, field + ": " + data);
                }
            }

        } // bind()
    }
}
