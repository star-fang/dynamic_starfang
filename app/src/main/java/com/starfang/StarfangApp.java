package com.starfang;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.starfang.realm.DynamicMigrations;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;

public class StarfangApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        try {
            RealmConfiguration migrationConfig = new RealmConfiguration.Builder().
                    name("realm.starfang").schemaVersion(4)
                    .migration(new DynamicMigrations())
                    //.deleteRealmIfMigrationNeeded()
                    .build();
            Realm.setDefaultConfiguration(migrationConfig);
        } catch (IllegalStateException e) {
            Log.e("FANG_APP", Log.getStackTraceString(e));

        }

        try {
            Realm realm = Realm.getDefaultInstance();
            realm.close();
        } catch (RealmMigrationNeededException e) {
            Log.e("FANG_APP", Log.getStackTraceString(e));
        }

    }

    @Override
    public void onTerminate() {
        Realm.getDefaultInstance().close();
        super.onTerminate();
    }
}
