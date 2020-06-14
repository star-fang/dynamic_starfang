package com.starfang.nlp;

import com.starfang.realm.source.Passives;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class ReadDescription {

    List<String> read(String text ) {
        try(Realm realm = Realm.getDefaultInstance()) {
            RealmResults<Passives> passives = realm.where(Passives.class).contains(Passives.FIELD_NAME,text).findAll();
        }
        return  null;
    }
}
