package com.starfang.ui.dynamic.unitsim;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.starfang.realm.simulator.UnitSim;

import io.realm.Realm;

public class UnitSimPlaceHolderFragment extends Fragment {

    protected Context mContext;
    protected Realm realm;

    public static UnitSimPlaceHolderFragment newInstance( int position, int id ) {
        Bundle arguments = new Bundle();
        arguments.putInt(UnitSim.FIELD_ID, id);
        UnitSimPlaceHolderFragment unitSimPlaceHolderFragment;
        switch( position ) {
            case 0:
               unitSimPlaceHolderFragment = new GradeControlFragment();
               break;
            case 1:
                unitSimPlaceHolderFragment = new PassiveFragment();
                break;
            case 2:
                unitSimPlaceHolderFragment = new SwitchItemFragment();
                break;
            case 3:
                unitSimPlaceHolderFragment = new RelicFragment();
                break;
            default:
                unitSimPlaceHolderFragment = null;
        }
        if( unitSimPlaceHolderFragment != null ) {
            unitSimPlaceHolderFragment.setArguments( arguments );
        }
        return unitSimPlaceHolderFragment;
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
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }
}
