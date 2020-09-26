package com.starfang.ui.dynamic.unitsim;

import androidx.annotation.StringRes;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.starfang.R;
import com.starfang.realm.simulator.UnitSim;

public class GradeViewModel extends ViewModel {

    @StringRes
    public static final int[] STAT_NAMES = new int[]{R.string.str, R.string.intel,R.string.cmd, R.string.dex, R.string.lck};
    @StringRes
    public static final int[] GRADE_NAMES = new int[]{R.string.level, R.string.reinforcement,R.string.grade};

    public interface GradeFactorsPosition {
        int LEVEL = 0;
        int REINFORCEMENT = 1;
        int GRADE = 2;
    }

    private MutableLiveData<Integer> mLevel;
    private MutableLiveData<Integer> mGrade;
    private MutableLiveData<Integer> mReinforcement;
    private MutableLiveData<Integer> mStrPlus;
    private MutableLiveData<Integer> mIntelPlus;
    private MutableLiveData<Integer> mCmdPlus;
    private MutableLiveData<Integer> mDexPlus;
    private MutableLiveData<Integer> mLckPlus;
    private MutableLiveData<Integer> mMaxPlusStat;

    public GradeViewModel() {
        this.mLevel = new MutableLiveData<>();
        this.mGrade = new MutableLiveData<>();
        this.mReinforcement = new MutableLiveData<>();
        this.mStrPlus = new MutableLiveData<>();
        this.mIntelPlus = new MutableLiveData<>();
        this.mCmdPlus = new MutableLiveData<>();
        this.mDexPlus = new MutableLiveData<>();
        this.mLckPlus = new MutableLiveData<>();
        this.mMaxPlusStat = new MutableLiveData<>();
    }

    public Integer getMaxPlusStatValue() {
        return this.mMaxPlusStat.getValue();
    }

    public MutableLiveData<Integer> getMaxPlusStat() {
        return this.mMaxPlusStat;
    }

    public void setMaxPlusStat(int max ) {
        this.mMaxPlusStat.setValue( max );
    }

    public void setMaxPlusStatObserver( LifecycleOwner owner, Observer<Integer> observer ) {
        this.mMaxPlusStat.observe(owner, observer);
    }

    public void setLevelObserver(LifecycleOwner owner, Observer<Integer> observer) {
        this.mLevel.observe(owner, observer);
    }

    public Integer getLevelValue() {
        return this.mLevel.getValue();
    }

    public void setLevel(int level) {
        this.mLevel.setValue(level);
    }

    public void setGradeObserver(LifecycleOwner owner, Observer<Integer> observer) {
        this.mGrade.observe(owner, observer);
    }

    public Integer getGradeValue() {
        return this.mGrade.getValue();
    }

    public void setGrade(int grade) {
        this.mGrade.setValue(grade);
    }

    public void setReinforcementObserver(LifecycleOwner owner, Observer<Integer> observer) {
        this.mReinforcement.observe(owner, observer);
    }

    public Integer getReinforcementValue() {
        return this.mReinforcement.getValue();
    }

    public void setReinforcement(int reinforcement) {
        this.mReinforcement.setValue(reinforcement);
    }

    public MutableLiveData<Integer> getGradeFactorByIndex(int i) {
        switch( i ) {
            case GradeFactorsPosition.LEVEL:
                return mLevel;
            case GradeFactorsPosition.REINFORCEMENT:
                return mReinforcement;
            case GradeFactorsPosition.GRADE:
                return mGrade;
            default:
                return null;
        }
    }

    public MutableLiveData<Integer> getPlusStatByIndex(int i) {
        switch (i) {
            case 0:
                return mStrPlus;
            case 1:
                return mIntelPlus;
            case 2:
                return mCmdPlus;
            case 3:
                return mDexPlus;
            case 4:
                return mLckPlus;
            default:
                return null;
        }
    }

    public Integer getPlusStatValue(UnitSim.STAT_CODE code) {
        switch (code) {
            case STR:
                return mStrPlus.getValue();
            case INTEL:
                return mIntelPlus.getValue();
            case CMD:
                return mCmdPlus.getValue();
            case DEX:
                return mDexPlus.getValue();
            case LCK:
                return mLckPlus.getValue();
            default:
                return null;
        }
    }

    public Integer getPlusStatSum() {
        Integer strValue = mStrPlus.getValue();
        Integer intelValue = mIntelPlus.getValue();
        Integer cmdValue = mCmdPlus.getValue();
        Integer dexValue = mDexPlus.getValue();
        Integer lckValue = mLckPlus.getValue();
        if (strValue != null && intelValue != null && cmdValue != null && dexValue != null && lckValue != null) {
            return strValue + intelValue + cmdValue + dexValue + lckValue;
        }
        return null;
    }

    public boolean setPlusStat(UnitSim.STAT_CODE code, int value) {
        MutableLiveData<Integer> data;
        switch (code) {
            case STR:
                data = mStrPlus;
                break;
            case INTEL:
                data = mIntelPlus;
                break;
            case CMD:
                data = mCmdPlus;
                break;
            case DEX:
                data = mDexPlus;
                break;
            case LCK:
                data = mLckPlus;
                break;
            default:
                data = null;
        }
        if (data != null) {
            Integer curValue = data.getValue();
            if (curValue == null) { // initialize
                data.setValue(value);
                return true;
            } else { // update
                Integer gradeValue = mGrade.getValue();
                Integer curPlusStatSum = getPlusStatSum();
                if (curPlusStatSum != null && gradeValue != null) {
                    int diff = value - curValue;
                    int maxPlusStatSum = gradeValue * 100;
                    if (curPlusStatSum + diff <= maxPlusStatSum) {
                        data.setValue(value);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void setPlusStatObserverAndValue(UnitSim.STAT_CODE code, LifecycleOwner owner, Observer<Integer> observer, Integer value) {
        MutableLiveData<Integer> data;
        switch (code) {
            case STR:
                data = mStrPlus;
                break;
            case INTEL:
                data = mIntelPlus;
                break;
            case CMD:
                data = mCmdPlus;
                break;
            case DEX:
                data = mDexPlus;
                break;
            case LCK:
                data = mLckPlus;
                break;
            default:
                data = null;
        }

        if (data != null) {
            if (owner != null && observer != null) {
                data.observe(owner, observer);
            }

            if (value != null) {
                data.setValue(value);
            }
        }
    }

}
