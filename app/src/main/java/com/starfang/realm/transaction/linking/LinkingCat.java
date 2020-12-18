package com.starfang.realm.transaction.linking;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.Source;
import com.starfang.realm.source.cat.Artifacts;
import com.starfang.realm.source.cat.ArtifactsCate;
import com.starfang.realm.source.cat.Banners;
import com.starfang.realm.source.cat.Friendships;
import com.starfang.realm.source.cat.PassiveList;
import com.starfang.realm.source.cat.Passives;
import com.starfang.realm.source.cat.Personality;
import com.starfang.realm.source.cat.PrefectSkills;
import com.starfang.realm.source.cat.Tiles;
import com.starfang.realm.source.cat.UnitGrades;
import com.starfang.realm.source.cat.UnitTypes;
import com.starfang.realm.source.cat.Units;
import com.starfang.realm.source.cat.WarlordSkills;

import java.lang.ref.WeakReference;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class LinkingCat extends LinkingTask<Void, String, Void> {

    private static final String TAG = "FANG_LINKING_CAT";

    public LinkingCat(String title, Context context, ViewModel model, WeakReference<ViewModel> activityVmRef) {
        super(title, context, model, activityVmRef);
    }

    @Override
    protected Void doInBackground(Void... v) {
        try (Realm realm = Realm.getDefaultInstance()) {

            realm.beginTransaction();

            OrderedRealmList<Passives> passivesOrderedRealmList = new OrderedRealmList<>(Passives.class);
            OrderedRealmList<PassiveList> passiveListOrderedRealmList = new OrderedRealmList<>(PassiveList.class);
            OrderedRealmList<Units> unitsOrderedRealmList = new OrderedRealmList<>(Units.class);
            OrderedRealmList<UnitTypes> unitTypesOrderedRealmList = new OrderedRealmList<>(UnitTypes.class);
            OrderedRealmList<UnitGrades> unitGradesOrderedRealmList = new OrderedRealmList<>(UnitGrades.class);

            publishProgress("Artifacts", "10");
            for (Artifacts artifact : realm.where(Artifacts.class).findAll()) {
                artifact.setPassives(passivesOrderedRealmList.getRealmList(realm, artifact.getPassiveIds()));
                RealmList<RealmInteger> unitIds = artifact.getUnitId();
                if (unitIds != null ) {
                    for( RealmInteger unitId : unitIds ) {
                        if( unitId != null ) {
                            artifact.setUnit(realm.where(Units.class)
                                    .equalTo(Source.FIELD_ID, unitId.getValue()).findFirst());
                        }
                    }
                }
                artifact.setCategory(
                        realm.where(ArtifactsCate.class)
                                .equalTo(Source.FIELD_ID, artifact.getInt(Artifacts.FIELD_CATEGORY)).findFirst());
                artifact.setUnitTypes(unitTypesOrderedRealmList.getRealmList(realm, artifact.getUnitTypeIds()));
            } // for artifacts

            publishProgress("ArtifactsCate", "20");
            for (ArtifactsCate cate : realm.where(ArtifactsCate.class).findAll()) {
                cate.setUnitTypes(unitTypesOrderedRealmList.getRealmList(realm, cate.getUnitTypeIds()));
            } // for ArtifactsCate

            publishProgress("Friendships", "30");
            for (Friendships friendship : realm.where(Friendships.class).findAll()) {
                friendship.setPassiveLists(passiveListOrderedRealmList.getRealmList(realm, friendship.getPassiveIds()));
                friendship.setUnitList(unitsOrderedRealmList.getRealmList(realm, friendship.getUnitIds()));
                RealmList<Units> unitList = friendship.getUnitList();
                if (unitList != null) {
                    for (Units unit : unitList) {
                        unit.addFriendship(friendship);
                    }
                }

                //Log.d(TAG, "friendship" + friendship.getString(Source.FIELD_NAME)+" linked");
            } // for Friendships

            publishProgress("Passives", "40");
            for (Passives passive : realm.where(Passives.class).findAll()) {
                passive.setTriggerTile(realm.where(Tiles.class).equalTo(Source.FIELD_ID, passive.getTriggerTileValue()).findFirst());
            }

            publishProgress("PassiveList", "50");
            for (PassiveList passiveList : realm.where(PassiveList.class).findAll()) {
                passiveList.setPassive(realm.where(Passives.class).equalTo(Source.FIELD_ID, passiveList.getPassiveId()).findFirst());
            }

            publishProgress("Units", "90");
            final RealmResults<Units> allUnits = realm.where(Units.class).findAll();
            for (Units unit : allUnits) {
                unit.calcStatSum();
                unit.setType(realm.where(UnitTypes.class).equalTo(Source.FIELD_ID, unit.getUnitTypeId()).findFirst());
                unit.updateTypeAndName();
                unit.setBanner(realm.where(Banners.class).equalTo(Source.FIELD_ID, unit.getBannerId()).findFirst());
                unit.setPersonality(realm.where(Personality.class).equalTo(Source.FIELD_ID, unit.getPersonalityId()).findFirst());
                unit.setPrefectSkill(realm.where(PrefectSkills.class).equalTo(Source.FIELD_ID, unit.getPrefectId()).findFirst());
                unit.setWarlordSkill(realm.where(WarlordSkills.class).equalTo(Source.FIELD_ID, unit.getWarlordId()).findFirst());
                unit.setPassiveLists(passiveListOrderedRealmList.getRealmList(realm, unit.getPassiveListIds()));
                unit.setNamesakeCount((int) allUnits.where().equalTo(Source.FIELD_NAME, unit.getString(Source.FIELD_NAME)).count());

                //Log.d(TAG, "unit" + unit.getString(Source.FIELD_NAME)+" linked");
            }

            publishProgress("UnitTypes", "100");
            for (UnitTypes type : realm.where(UnitTypes.class).findAll()) {
                type.setUnitPassiveLists(passiveListOrderedRealmList.getRealmList(realm, type.getUnitPassiveListIds()));
                type.setTypePassiveList(passivesOrderedRealmList.getRealmList(realm, type.getTypePassiveIds()));
                type.setGradeList(unitGradesOrderedRealmList.getRealmList(realm, type.getGradeIds()));

                //Log.d(TAG, "unitType" + type.getString(Source.FIELD_NAME)+" linked");
            }

            realm.commitTransaction();
        } catch (RuntimeException e) {
            Log.d(TAG, Log.getStackTraceString(e));
            this.cancel(true);
        }
        return null;
    }
}