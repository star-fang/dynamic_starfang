package com.starfang.viewmodel;

import androidx.lifecycle.ViewModel;

public class CMDActivityViewModel extends ViewModel {
    private boolean mIsSigningIn;

    public CMDActivityViewModel() {
        mIsSigningIn = false;
    }

    public boolean getIsSigningIn() {
        return mIsSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.mIsSigningIn = mIsSigningIn;
    }
}
