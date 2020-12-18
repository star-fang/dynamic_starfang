package com.starfang.fragments.progress.row;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProgressRowViewModel extends ViewModel {

    private MutableLiveData<String> mStepText;
    private MutableLiveData<String> mDetailText;
    private MutableLiveData<Integer> mProgress;
    private MutableLiveData<Boolean> mIndeterminate;
    private MutableLiveData<Boolean> mRemoveFragment;

    public ProgressRowViewModel() {

        mStepText = new MutableLiveData<>();
        mDetailText = new MutableLiveData<>();
        mProgress = new MutableLiveData<>(0);
        mIndeterminate = new MutableLiveData<>(false);
        mRemoveFragment = new MutableLiveData<>(false);
    }

    LiveData<String> getStepTextLiveData() {
        return mStepText;
    }
    LiveData<String> getDetailTextLiveData() {
        return mDetailText;
    }
    LiveData<Integer> getProgressLiveData() {
        return mProgress;
    }
    LiveData<Boolean> getIndeterminate() {
        return mIndeterminate;
    }
    LiveData<Boolean> getRemoveFragment() {
        return mRemoveFragment;
    }

    public void setIndeterminate(boolean indeterminate) {
        this.mIndeterminate.setValue(indeterminate);
    }

    public void setDetailText(String text) {
        this.mDetailText.setValue(text);
    }

    public void setStepText(String text) {
        this.mStepText.setValue(text);
    }

    public void setProgress(int progress) {
        this.mProgress.setValue(progress);
    }

    public void setRemoveFragment(boolean remove ) {
        this.mRemoveFragment.setValue(remove);
    }
}