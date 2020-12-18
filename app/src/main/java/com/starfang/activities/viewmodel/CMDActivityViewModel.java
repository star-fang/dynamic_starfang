package com.starfang.activities.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CMDActivityViewModel extends ViewModel {
    private MutableLiveData<Boolean> mIsSigningIn;
    private MutableLiveData<Integer> mProcessCount;

    public CMDActivityViewModel() {
        mIsSigningIn = new MutableLiveData<>(false);
        mProcessCount = new MutableLiveData<>(0);
    }

    public boolean getIsSigningInValue() {
        if( mIsSigningIn != null ) {
            Boolean valueObj = mIsSigningIn.getValue();
            if( valueObj != null ) {
                return valueObj;
            }
        }
        return false;
    }

    public LiveData<Boolean> getIsSigningIn() {
        return mIsSigningIn;
    }

    public LiveData<Integer> getProcessCountLiveData() {
        return mProcessCount;
    }

    public void countUpProcess() {
        if(mProcessCount!= null){
            Integer currVal = mProcessCount.getValue();
            if( currVal != null ) {
                mProcessCount.setValue(currVal + 1);
            }
        }
    }

    public void countDownProcess() {
        if(mProcessCount!= null){
            Integer currVal = mProcessCount.getValue();
            if( currVal != null ) {
                mProcessCount.setValue(currVal - 1);
            }
        }
    }

}
