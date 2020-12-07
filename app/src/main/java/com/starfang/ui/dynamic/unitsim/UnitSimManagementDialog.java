package com.starfang.ui.dynamic.unitsim;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.starfang.R;
import com.starfang.realm.simulator.UnitSim;
import com.starfang.realm.source.caocao.Units;
import com.starfang.ui.dynamic.DynamicDialog;

public class UnitSimManagementDialog extends DynamicDialog {

    private static final String TAG = "FANG_MANAGE_DIALOG";

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.unit_sim_tab_1, R.string.unit_sim_tab_2, R.string.unit_sim_tab_3, R.string.unit_sim_tab_4};

    public static UnitSimManagementDialog newInstance(int id) {
        UnitSimManagementDialog unitSimManagementDialog = new UnitSimManagementDialog();
        Bundle args = new Bundle();
        args.putInt(UnitSim.FIELD_ID, id);
        unitSimManagementDialog.setArguments(args);
        return unitSimManagementDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {


        Bundle args = getArguments();
        View view = View.inflate(mContext, R.layout.dialog_unit_sim_manager, null);


        if (mContext instanceof FragmentActivity && args != null) {
            int id = args.getInt(UnitSim.FIELD_ID);

            UnitSim unitSim = realm.where(UnitSim.class).equalTo(UnitSim.FIELD_ID, id).findFirst();

            if (unitSim != null) {
                AppCompatTextView text_unit_name = view.findViewById(R.id.text_unit_name);
                AppCompatTextView text_alias = view.findViewById(R.id.text_alias);

                text_unit_name.setText(unitSim.getUnit().getString(Units.FIELD_NAME));
                text_alias.setText(unitSim.getAlias());
            }

            final View floatView = view.findViewById(R.id.layout_float_unit_sim);
            final ViewPager2 viewPager = view.findViewById(R.id.view_pager_unit_sim);
            final TabLayout tabs = view.findViewById(R.id.tabs_unit_sim);
            final AppBarLayout appBarLayout = view.findViewById(R.id.app_bar);

            viewPager.setAdapter(new SlidePagerAdapter((FragmentActivity) mContext, id));
            new TabLayoutMediator(tabs, viewPager,
                    ((tab, position) -> tab.setText(mContext.getText(TAB_TITLES[position])))).attach();

            ViewTreeObserver floatViewObserver = floatView.getViewTreeObserver();
            if (floatViewObserver.isAlive()) {
                floatViewObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        floatView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        final int appBarLength = floatView.getHeight();
                        final int floatLength = floatView.getWidth();

                        Log.d(TAG, "appBarLength : " + appBarLength);
                        Log.d(TAG, "floatLength : " + floatLength);

                        ViewTreeObserver appBarObserver = appBarLayout.getViewTreeObserver();
                        if (appBarObserver.isAlive()) {
                            appBarObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    appBarLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    final int appBarThickness = appBarLayout.getHeight();
                                    final int viewPagerLength = floatLength - appBarThickness;

                                    Log.d(TAG, "appBarThickness : " + appBarThickness);
                                    Log.d(TAG, "viewPagerLength : " + viewPagerLength);

                                    ViewGroup.LayoutParams appBarLayoutLayoutParams = appBarLayout.getLayoutParams();
                                    appBarLayoutLayoutParams.width = appBarLength;
                                    appBarLayout.setLayoutParams(appBarLayoutLayoutParams);

                                    ViewGroup.LayoutParams viewPagerLayoutParams = viewPager.getLayoutParams();
                                    viewPagerLayoutParams.width = viewPagerLength;
                                    viewPager.setLayoutParams(viewPagerLayoutParams);
 
                                    appBarLayout.setTranslationX(floatLength / 2f);
                                    appBarLayout.setTranslationY((-1) * appBarLength / 2f);
                                }
                            });
                        }


                    }
                });
            }
        }

        Dialog dialog = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        return dialog;
    }

    private static class SlidePagerAdapter extends FragmentStateAdapter {

        private int id;

        public SlidePagerAdapter(@NonNull FragmentActivity fragmentActivity, int id) {
            super(fragmentActivity);
            this.id = id;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return UnitSimPlaceHolderFragment.newInstance(position, id);
        }

        @Override
        public int getItemCount() {
            return TAB_TITLES.length;
        }
    }

}
