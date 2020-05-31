package com.starfang.ui.dynamic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.starfang.R;
import com.starfang.StarfangConstants;
import com.starfang.realm.Source;
import com.starfang.realm.TableList;
import com.starfang.realm.source.Units;

import java.util.Calendar;
import java.util.Set;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import io.realm.Sort;

public class DynamicSpreadSheetFragment extends DialogFragment {

    private static final String TAG = "FANG_DYNAMIC_SHEET";
    private Context mContext;
    private Realm realm;

    public static DynamicSpreadSheetFragment newInstance() {
        DynamicSpreadSheetFragment dynamicSpreadSheetFragment = new DynamicSpreadSheetFragment();
        return dynamicSpreadSheetFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (this.realm != null) {
            this.realm.close();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = View.inflate(mContext, R.layout.fragment_spread_sheet, null );

        DiagonalScrollRecyclerView diagonal_fixed = view.findViewById(R.id.diagonal_fixed);
        RecyclerView recycler_fixed = view.findViewById(R.id.recycler_fixed);
        recycler_fixed.setLayoutManager(new LinearLayoutManager(mContext));
        diagonal_fixed.setRecyclerView(recycler_fixed);

        DiagonalScrollRecyclerView diagonal_float = view.findViewById(R.id.diagonal_float);
        RecyclerView recycler_float = view.findViewById(R.id.recycler_float);
        recycler_float.setLayoutManager(new LinearLayoutManager(mContext));
        diagonal_float.setRecyclerView(recycler_float);

        RealmResults<TableList> tableLists = realm.where(TableList.class).findAll();

        final RealmSchema schema = realm.getSchema();

        for (TableList tableList : tableLists) {
            String tableName = tableList.getTableName();
            if (tableName != null) {
                long lastModified = tableList.getLastModified();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(lastModified);
                String modelName = tableName.substring(0, 1).toUpperCase() + tableName.substring(1);
                //Log.d(TAG, "------------model: " + modelName + "---------");
                //Log.d(TAG, "lastModified: " + calendar.getTime());
                try {

                    if( modelName.equals("Units")) {
                        Class<? extends RealmObject> clazz = Class.forName(StarfangConstants.REALM_MODEL_SOURCE + modelName)
                                .asSubclass(RealmObject.class);
                        Log.d(TAG, clazz.getName() + "-----------------------");

                        RealmObjectSchema realmObjectSchema = schema.get(modelName);
                        if( realmObjectSchema != null ) {
                            Set<String> fieldNames = realmObjectSchema.getFieldNames();
                            OrderedRealmCollection<? extends  RealmObject> collection = realm.where(clazz).findAll().sort(Source.FIELD_ID, Sort.ASCENDING);

                            DynamicRealmRecyclerAdapter<?> adapter = new DynamicRealmRecyclerAdapter<>(
                                    collection, fieldNames, true,  true);
                            recycler_float.setAdapter(adapter);
                        }
                    }

                } catch (ClassNotFoundException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }

            }
        }

        Dialog dialog = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView( view );
        Window window = dialog.getWindow();
        if( window != null ) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        return dialog;
    }

}
