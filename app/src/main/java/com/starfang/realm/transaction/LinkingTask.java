package com.starfang.realm.transaction;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.starfang.realm.Transaction;
import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.Banners;
import com.starfang.realm.source.Conditions;
import com.starfang.realm.source.Formula;
import com.starfang.realm.source.Friendships;
import com.starfang.realm.source.PassiveList;
import com.starfang.realm.source.Passives;
import com.starfang.realm.source.Personality;
import com.starfang.realm.source.PrefectSkills;
import com.starfang.realm.source.Source;
import com.starfang.realm.source.Tiles;
import com.starfang.realm.source.UnitGrades;
import com.starfang.realm.source.UnitTypes;
import com.starfang.realm.source.Units;
import com.starfang.realm.source.WarlordSkills;
import com.starfang.ui.progress.ProgressViewModel;


import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class LinkingTask extends Transaction<Bundle, String, String> {

    private static final String TAG = "FANG_LINKING";

    private WeakReference<ProgressViewModel> progressViewWeakReference;

    LinkingTask(ProgressViewModel progressView) {
        progressViewWeakReference = new WeakReference<>(progressView);
    }

    public LinkingTask() {
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (progressViewWeakReference != null) {
            progressViewWeakReference.get().setQuitVisibility(View.VISIBLE);
        }

    }

    @Override
    protected String doInBackground(Bundle... models) {

        try (Realm realm = Realm.getDefaultInstance()) {
            realm.beginTransaction();
            for (Bundle model : models) {
                ArrayList<Integer> ids = model.getIntegerArrayList(Linking.idList);
                if (ids != null) {
                    Integer[] idArray = ids.toArray(new Integer[0]);
                    switch (model.getString(Linking.modelName, "")) {
                        case "Banners":
                            RealmResults<Banners> banners = realm.where(Banners.class).in(Source.FIELD_ID, idArray).findAll();
                            for(Banners banner : banners) {
                                banner.setNameWithoutBlank();
                            }
                            Log.d(TAG, banners.size() + "banners linked");
                            break;
                        case "Conditions":
                            RealmResults<Conditions> conditions = realm.where(Conditions.class).in(Source.FIELD_ID, idArray).findAll();
                            for(Conditions condition : conditions) {
                                condition.setNameWithoutBlank();
                            }
                            Log.d(TAG, conditions.size() + "conditions linked");
                            break;
                        case "Formula":
                            RealmResults<Formula> formulas = realm.where(Formula.class).in(Source.FIELD_ID, idArray).findAll();
                            for(Formula formula : formulas) {
                                formula.setNameWithoutBlank();
                            }
                            Log.d(TAG, formulas.size() + "formulas linked");
                            break;
                        case "PrefectSkills":
                            RealmResults<PrefectSkills> prefectSkills = realm.where(PrefectSkills.class).in(Source.FIELD_ID, idArray).findAll();
                            for(PrefectSkills prefectSkill : prefectSkills) {
                                prefectSkill.setNameWithoutBlank();
                            }
                            Log.d(TAG, prefectSkills.size() + "prefectSkills linked");
                            break;
                        case "WarlordSkills":
                            RealmResults<WarlordSkills> warlordSkills = realm.where(WarlordSkills.class).in(Source.FIELD_ID, idArray).findAll();
                            for(WarlordSkills warlordSkill : warlordSkills) {
                                warlordSkill.setNameWithoutBlank();
                            }
                            Log.d(TAG, warlordSkills.size() + "warlordSkills linked");
                            break;
                        case "Units":
                            RealmResults<Units> units = realm.where(Units.class).in(Source.FIELD_ID, idArray).findAll();
                            for (Units unit : units) {
                                unit.setType(realm.where(UnitTypes.class).equalTo(Source.FIELD_ID, unit.getUnitTypeId()).findFirst());
                                unit.setBanner(realm.where(Banners.class).equalTo(Source.FIELD_ID, unit.getBannerId()).findFirst());
                                unit.setPersonality(realm.where(Personality.class).equalTo(Source.FIELD_ID, unit.getPersonalityId()).findFirst());
                                unit.setPrefectSkill(realm.where(PrefectSkills.class).equalTo(Source.FIELD_ID, unit.getPrefectId()).findFirst());
                                unit.setWarlordSkill(realm.where(WarlordSkills.class).equalTo(Source.FIELD_ID, unit.getWarlordId()).findFirst());
                                RealmResults<PassiveList> passiveListRealmResults = realm.where(PassiveList.class)
                                        .in(Source.FIELD_ID, toIntArray(unit.getPassiveListIds())).findAll();
                                if (passiveListRealmResults != null) {
                                    unit.setPassiveLists(passiveListRealmResults);
                                }
                                RealmResults<Friendships> friendshipsRealmResults = realm.where(Friendships.class)
                                        .equalTo(Friendships.FIELD_UNIT_IDS + "." + RealmInteger.VALUE, unit.getId()).findAll();
                                if (friendshipsRealmResults != null) {
                                    unit.setFriendshipList(friendshipsRealmResults);
                                }
                            }
                            Log.d(TAG, units.size() + "units linked");
                            break;
                        case "Friendships":
                            RealmResults<Friendships> friendships = realm.where(Friendships.class).in(Source.FIELD_ID, idArray).findAll();
                            for (Friendships friendship : friendships) {
                                RealmResults<Units> unitsRealmResults = realm.where(Units.class)
                                        .in(Source.FIELD_ID, toIntArray(friendship.getUnitIds())).findAll();
                                friendship.setUnitList(unitsRealmResults);
                                RealmResults<PassiveList> passiveListRealmResults = realm.where(PassiveList.class)
                                        .in(Source.FIELD_ID, toIntArray(friendship.getPassiveIds())).findAll();
                                friendship.setPassiveList(passiveListRealmResults);
                            }
                            break;

                        case "PassiveList":
                            RealmResults<PassiveList> passiveLists = realm.where(PassiveList.class).in(Source.FIELD_ID, idArray).findAll();
                            for( PassiveList passiveList : passiveLists ) {
                                passiveList.setPassive(realm.where(Passives.class).equalTo(Source.FIELD_ID, passiveList.getPassiveId()).findFirst());
                                Units unit = realm.where(Units.class).equalTo(Units.FIELD_PASV_LIST_IDS+"."+RealmInteger.VALUE, passiveList.getId()).findFirst();
                                if( unit != null ) {
                                    passiveList.setUnitLevel(unit.getPassiveLevel(passiveList.getId()));
                                }
                            }
                            Log.d(TAG, passiveLists.size() + "passiveLists linked");
                            break;
                        case "Passives":
                            RealmResults<Passives> passives = realm.where(Passives.class).in(Source.FIELD_ID, idArray).findAll();
                            for( Passives passive : passives ) {
                                passive.setNameWithoutBlank();
                                passive.setTriggerTile(realm.where(Tiles.class).equalTo(Source.FIELD_ID, passive.getTriggerTileValue()).findFirst());
                            }
                            Log.d(TAG, passives.size() + "passives linked");
                            break;
                        case "UnitTypes":
                            RealmResults<UnitTypes> unitTypes = realm.where(UnitTypes.class).in(Source.FIELD_ID, idArray).findAll();
                            for( UnitTypes type : unitTypes ) {
                                //private RealmList<PassiveList> unitPassiveLists;
                                //private RealmList<Passives> typePassiveList;
                                //private RealmList<UnitGrades> gradeList;
                                RealmResults<PassiveList> passiveListRealmResults = realm.where(PassiveList.class)
                                        .in(Source.FIELD_ID, toIntArray(type.getUnitPassiveListIds())).findAll();
                                type.setUnitPassiveLists(passiveListRealmResults);

                                RealmResults<Passives> passivesRealmResults = realm.where(Passives.class)
                                        .in(Source.FIELD_ID, toIntArray(type.getTypePassiveIds())).findAll();
                                type.setTypePassiveList(passivesRealmResults);

                                RealmResults<UnitGrades> unitGradesRealmResults = realm.where(UnitGrades.class)
                                        .in(Source.FIELD_ID, toIntArray(type.getGradeIds())).findAll();
                                type.setGradeList(unitGradesRealmResults);
                            }
                            Log.d(TAG, unitTypes.size() + "unitTypes linked");
                            break;
                    } // switch
                }
            }


            realm.commitTransaction();
        } catch( RuntimeException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }


    private static Integer[] toIntArray(RealmList<RealmInteger> realmIntegers) {
        int size = realmIntegers.size();
        Integer[] array = new Integer[size];
        for( int i = 0; i < size; i++ ) {
            RealmInteger realmInteger = realmIntegers.get(i);
            if( realmInteger != null ) {
                array[i] = realmInteger.getValue();
            }

        }
        return array;
    }


}
