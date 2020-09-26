package com.starfang.ui.dynamic;

import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.starfang.R;
import com.starfang.StarfangConstants;
import com.starfang.realm.source.Source;
import com.starfang.realm.TableList;

import java.util.Calendar;
import java.util.Set;

import io.realm.OrderedRealmCollection;
import io.realm.RealmObject;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import io.realm.Sort;

public class DynamicSourcesDialog extends DynamicDialog {

    private static final String TAG = "FANG_DYNAMIC_SHEET";


    public static DynamicSourcesDialog newInstance() {
        DynamicSourcesDialog dynamicSourcesDialog = new DynamicSourcesDialog();
        return dynamicSourcesDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        final View view = View.inflate(mContext, R.layout.fragment_spread_sheet, null);

        final DiagonalScrollRecyclerView diagonal_fixed = view.findViewById(R.id.diagonal_fixed);
        final RecyclerView recycler_fixed = view.findViewById(R.id.recycler_fixed);
        recycler_fixed.setLayoutManager(new LinearLayoutManager(mContext));
        diagonal_fixed.setRecyclerView(recycler_fixed);

        final DiagonalScrollRecyclerView diagonal_float = view.findViewById(R.id.diagonal_float);
        final RecyclerView recycler_float = view.findViewById(R.id.recycler_float);
        recycler_float.setLayoutManager(new LinearLayoutManager(mContext));
        diagonal_float.setRecyclerView(recycler_float);

        final RealmResults<TableList> tableLists = realm.where(TableList.class).findAll();

        final RadioGroup radio_group_table_list = view.findViewById(R.id.radio_group_table_list);

        final RealmSchema schema = realm.getSchema();
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        for (TableList tableList : tableLists) {
            String tableName = tableList.getTableName();
            if (tableName != null) {
                long lastModified = tableList.getLastModified();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(lastModified);
                final String modelName = tableName.substring(0, 1).toUpperCase() + tableName.substring(1);
                Log.d(TAG, "------------model: " + modelName + "---------");
                Log.d(TAG, "lastModified: " + calendar.getTime());
                try {
                    Class<? extends RealmObject> clazz = Class.forName(StarfangConstants.REALM_MODEL_SOURCE + modelName)
                            .asSubclass(RealmObject.class);
                    //Log.d(TAG, clazz.getName() + "-----------------------");


                    final RealmObjectSchema realmObjectSchema = schema.get(modelName);
                    if (realmObjectSchema != null) {


                        AppCompatRadioButton radioButton = new AppCompatRadioButton(mContext);
                        radioButton.setText(modelName);
                        radioButton.setOnClickListener(v -> {
                            Set<String> fieldNames = realmObjectSchema.getFieldNames();
                            Log.d(TAG,"fieldNames: " + fieldNames);
                            OrderedRealmCollection<? extends RealmObject> collection = realm.where(clazz).findAll().sort(Source.FIELD_ID, Sort.ASCENDING);

                            DynamicRealmRecyclerAdapter<?> adapter = new DynamicRealmRecyclerAdapter<>(
                                    collection, fieldNames, true, true, displayMetrics);
                            recycler_float.setAdapter(adapter);
                        });
                        radio_group_table_list.addView(radioButton);
                    }


                } catch (ClassNotFoundException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }

            }
        }

        Dialog dialog = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        return dialog;
    }

}
