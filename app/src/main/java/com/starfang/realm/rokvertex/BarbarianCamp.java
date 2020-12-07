package com.starfang.realm.rokvertex;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.rok.Vertex;

import io.realm.RealmList;
import io.realm.RealmObject;

public class BarbarianCamp extends RealmObject {
    private long summonTime;
    private RealmList<RealmInteger> allowedAllyIds;
    private Vertex vertex;
}
