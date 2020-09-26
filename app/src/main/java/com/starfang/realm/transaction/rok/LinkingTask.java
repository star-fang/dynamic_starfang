package com.starfang.realm.transaction.rok;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.starfang.realm.source.Source;
import com.starfang.realm.source.rok.Civilizations;
import com.starfang.realm.source.rok.Commanders;
import com.starfang.realm.source.rok.Skills;
import com.starfang.realm.source.rok.Specifications;
import com.starfang.realm.source.rok.Technologies;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class LinkingTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "FANG_LINKING";

    LinkingTask(Context context) {
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        Log.d(TAG,"ROK Linking complete");
    }

    @Override
    protected Void doInBackground(Void... v) {



        try (Realm realm = Realm.getDefaultInstance()) {

            realm.beginTransaction();
            RealmResults<Commanders> commanders = realm.where(Commanders.class).findAll();
            for( Commanders commander : commanders ) {

                commander.setCivilization(realm.where(Civilizations.class).equalTo(Source.FIELD_ID,commander.getInt(Commanders.FIELD_CIVIL)).findFirst());
                commander.setSpecifications(realm.where(Specifications.class).in(Source.FIELD_ID, com.starfang.realm.transaction.LinkingTask.toIntArray(
                        commander.getSpecIds()
                )).findAll());
                commander.setSkills(realm.where(Skills.class).in(Source.FIELD_ID, com.starfang.realm.transaction.LinkingTask.toIntArray(
                        commander.getSkillIds()
                )).findAll());
            }


            RealmResults<Technologies> technologies = realm.where(Technologies.class).findAll();
            for( Technologies technology : technologies ) {

            }

            realm.commitTransaction();
        } catch( RuntimeException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }



}
