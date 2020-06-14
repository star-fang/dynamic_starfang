package com.starfang.utilities;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.primitive.RealmString;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public class RealmListCaster<T, R extends RealmObject>{

    private RealmList<R> realmList;

    public RealmListCaster(RealmList<R> realmList) {
        this.realmList = realmList;
    }
    public List<T> toList(Class<T> clazz) throws ClassCastException {
        List<T> list = new ArrayList<>();
        for( R r : realmList) {
            if( r instanceof RealmString) {
                list.add( clazz.cast(r.toString()) );
            } else if( r instanceof RealmInteger) {
                list.add( clazz.cast(((RealmInteger) r).getValue()) );
            }
        }
        return list;
    }



    public T[] toArray(Class<T[]> aClazz, Class<T> clazz) throws ClassCastException {
        int size = realmList.size();
        T[] array = aClazz.cast(Array.newInstance(aClazz,size));
        if( array != null ) {
            for (int i = 0; i < size; i++) {
                R r = realmList.get(i);
                if (r instanceof RealmString) {
                    array[i] = clazz.cast(r.toString());
                } else if( r instanceof  RealmInteger ){
                    array[i] = clazz.cast(((RealmInteger) r).getValue());
                }
            }
        }
        return array;
    }
}
