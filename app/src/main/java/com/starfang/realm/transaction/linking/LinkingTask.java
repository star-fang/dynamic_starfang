package com.starfang.realm.transaction.linking;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.ViewModel;

import com.starfang.activities.viewmodel.CMDActivityViewModel;
import com.starfang.fragments.progress.row.ProgressRowViewModel;
import com.starfang.nlp.SystemMessage;
import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.Source;

import org.apache.commons.lang3.math.NumberUtils;

import java.lang.ref.WeakReference;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public abstract  class LinkingTask<PARAMS, PROGRESS, RESULT> extends AsyncTask<PARAMS, PROGRESS, RESULT> {

    protected final WeakReference<Context> mContextRef;
    protected final WeakReference<ViewModel> mViewModelRef;
    protected final WeakReference<ViewModel> mActivityViewModelRef;
    protected final String mTitle;

    public LinkingTask(String title, Context context, ViewModel viewModel, WeakReference<ViewModel> activityVmRef) {
        this.mContextRef = new WeakReference<>(context);
        this.mViewModelRef = new WeakReference<>(viewModel);
        this.mTitle = title;
        this.mActivityViewModelRef = activityVmRef;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ViewModel viewModel = mViewModelRef.get();
        if( viewModel instanceof ProgressRowViewModel ) {
            ((ProgressRowViewModel) viewModel).setStepText("Linking");
        }
    }

    @Override
    protected void onProgressUpdate(PROGRESS[] values) {
        super.onProgressUpdate(values);
        ViewModel model = mViewModelRef.get();
        if (model instanceof ProgressRowViewModel) {
            if (values.length > 1 && values[1] instanceof String) {
                ((ProgressRowViewModel) model).setProgress(
                        NumberUtils.toInt((String)values[1], 0));
            }
            if( values.length > 0 && values[0] instanceof String) {
                ((ProgressRowViewModel) model).setDetailText((String)values[0]);
            }
        }
    }

    @Override
    protected void onPostExecute(RESULT v) {
        super.onPostExecute(v);
        ViewModel activityVm = mActivityViewModelRef.get();
        if( activityVm instanceof CMDActivityViewModel) {
            ((CMDActivityViewModel) activityVm).countDownProcess();
        }

        SystemMessage.insertMessage(mTitle + "데이터 연결 완료",
                "com.starfang",
                mContextRef.get());

        ViewModel viewModel = mViewModelRef.get();
        if( viewModel instanceof ProgressRowViewModel) {
            ((ProgressRowViewModel) viewModel).setRemoveFragment(true);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        ViewModel activityVm = mActivityViewModelRef.get();
        if( activityVm instanceof CMDActivityViewModel) {
            ((CMDActivityViewModel) activityVm).countDownProcess();
        }

        SystemMessage.insertMessage(mTitle + "데이터 연결 취소됨",
                "com.starfang",
                mContextRef.get());

        ViewModel viewModel = mViewModelRef.get();
        if( viewModel instanceof ProgressRowViewModel ) {
            ((ProgressRowViewModel) viewModel).setRemoveFragment(true);
        }
    }

    protected static class OrderedRealmList<T extends RealmObject> {

        private final Class<T> clazz;

        OrderedRealmList(Class<T> clazz) {
            this.clazz = clazz; 
        }

        RealmList<T> getRealmList(Realm realm, RealmList<RealmInteger> idArr) {
            RealmList<T> realmList = new RealmList<>();
            for (RealmInteger value : idArr) {
                T a = value == null ? null : realm.where(clazz).equalTo(Source.FIELD_ID, value.getValue()).findFirst();
                realmList.add(a);
            }
            return realmList;
        }
    }
    
    protected static class ProgressCounter {
        private final int maxProgress;
        private int progress;
        private int percentage;
        ProgressCounter( int maxProgress ) {
            this.maxProgress = maxProgress;
            this.progress = 0;
        }

        String countUp() {
            int percentage = (int)(this.progress++ / this.maxProgress * 100);
            if( this.percentage != percentage ) {
                this.percentage = percentage;
                return String.valueOf(percentage);
            }
            return null;
        }
    }


}
