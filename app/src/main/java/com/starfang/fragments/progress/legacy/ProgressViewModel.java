package com.starfang.fragments.progress.legacy;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProgressViewModel extends ViewModel {

    private MutableLiveData<String> mTitleText;
    private MutableLiveData<String> mTopStartText;
    private MutableLiveData<String> mTopEndText;
    private MutableLiveData<String> mBelowText;
    private MutableLiveData<String> mAboveText;
    private MutableLiveData<Integer> mProgress;
    private MutableLiveData<Integer> mQuitVisibility;
    private MutableLiveData<Boolean> mIndeterminate;
    private MutableLiveData<Long> mMaxByte;

    public ProgressViewModel() {
        mTitleText = new MutableLiveData<>();

        mProgress = new MutableLiveData<>();
        mProgress.setValue(0);

        mTopStartText = new MutableLiveData<>();
        //mTopStartText.setValue("top-start");

        mTopEndText = new MutableLiveData<>();
        //mTopEndText.setValue("top-end");

        mBelowText = new MutableLiveData<>();
        //mBelowText.setValue("below");

        mAboveText = new MutableLiveData<>();
        //mAboveText.setValue("above");

        mQuitVisibility = new MutableLiveData<>();
        mQuitVisibility.setValue(View.GONE);

        mIndeterminate = new MutableLiveData<>();
        mIndeterminate.setValue(false);

        mMaxByte = new MutableLiveData<>();
        mMaxByte.setValue(1L);

    }

    LiveData<String> getTitleText() {
        return mTitleText;
    }

    MutableLiveData<String> getTopEndText() {
        return mTopEndText;
    }

    MutableLiveData<String> getTopStartText() {
        return mTopStartText;
    }

    MutableLiveData<String> getBelowText() {
        return mBelowText;
    }

    MutableLiveData<String> getAboveText() {
        return mAboveText;
    }

    MutableLiveData<Integer> getProgress() {
        return mProgress;
    }

    MutableLiveData<Integer> getQuitVisibility() {
        return mQuitVisibility;
    }

    MutableLiveData<Boolean> getIndeterminate() {
        return mIndeterminate;
    }

    Long getMaxByteValue() {
        return mMaxByte.getValue();
    }

    public void setIndeterminate(boolean indeterminate) {
        this.mIndeterminate.setValue(indeterminate);
    }

    public void setQuitVisibility(int visibility) {
        this.mQuitVisibility.setValue(visibility);
    }

    public void setTitleText(String text) {
        this.mTitleText.setValue(text);
    }

    public void setProgress(int progress) {
        this.mProgress.setValue(progress);
    }

    public void setBelowText(String string) {
        this.mBelowText.setValue(string);
    }

    public void setAboveText(String string) {
        this.mAboveText.setValue(string);
    }

    public void setTopStartText(String string) {
        this.mTopStartText.setValue(string);
    }

    public void setTopEndText(String string) {
        this.mTopEndText.setValue(string);
    }

    public void setMaxByte(long maxByte) {
        this.mMaxByte.setValue(maxByte);
    }
}