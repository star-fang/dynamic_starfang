package com.starfang.realm.transaction.rok;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.Source;
import com.starfang.realm.source.rok.BuildContent;
import com.starfang.realm.source.rok.Building;
import com.starfang.realm.source.rok.Civilizations;
import com.starfang.realm.source.rok.Commanders;
import com.starfang.realm.source.rok.Skills;
import com.starfang.realm.source.rok.Specifications;
import com.starfang.realm.source.rok.TechContent;
import com.starfang.realm.source.rok.Technology;

import org.apache.commons.lang3.math.NumberUtils;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class LinkingTask extends AsyncTask<Void, Void, Void> {

    private static final Integer[] DELETE_CONTENTS_ID = {
            900028, 900029, 900030, 900031, 900032, 900033, 900034, 900035,
            900051, 900052, 900053, 900054, 900055, 900056, 900057, 900058
    };

    private static final String TAG = "FANG_LINKING";

    LinkingTask(Context context) {
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        Log.d(TAG, "ROK Linking complete");
    }

    @Override
    protected Void doInBackground(Void... v) {


        try (Realm realm = Realm.getDefaultInstance()) {

            realm.beginTransaction();
            RealmResults<Commanders> commanders = realm.where(Commanders.class).findAll();
            for (Commanders commander : commanders) {

                commander.setCivilization(realm.where(Civilizations.class).equalTo(Source.FIELD_ID, commander.getInt(Commanders.FIELD_CIVIL)).findFirst());
                commander.setSpecifications(realm.where(Specifications.class).in(Source.FIELD_ID, com.starfang.realm.transaction.LinkingTask.toIntArray(
                        commander.getSpecIds()
                )).findAll());
                commander.setSkills(realm.where(Skills.class).in(Source.FIELD_ID, com.starfang.realm.transaction.LinkingTask.toIntArray(
                        commander.getSkillIds()
                )).findAll());
            }

            RealmResults<TechContent> delete_contents = realm.where(TechContent.class).in(Source.FIELD_ID, DELETE_CONTENTS_ID).findAll();
            delete_contents.deleteAllFromRealm();
            RealmResults<Technology> delete_techs = realm.where(Technology.class).in(Technology.FIELD_CONTENT_ID, DELETE_CONTENTS_ID).findAll();
            delete_techs.deleteAllFromRealm();

            RealmResults<Technology> technologies = realm.where(Technology.class).findAll();
            for (Technology technology : technologies) {
                TechContent content = realm.where(TechContent.class).equalTo(Source.FIELD_ID, technology.getInt(Technology.FIELD_CONTENT_ID)).findFirst();
                if (content != null) {
                    technology.setContent(content);
                }
                RealmList<RealmString> required = technology.getRequirements();
                RealmList<Technology> reqTechs = new RealmList<>();
                RealmList<Building> reqBuildings = new RealmList<>();
                for (RealmString requirement : required) {
                    if (requirement != null) {
                        String reqName = requirement.toString().replaceAll("[0-9]|Level", "").trim()
                                .replaceAll("\\s+", "_");
                        int reqLv = NumberUtils.toInt(requirement.toString().replaceAll("[^0-9]", ""), 0);
                        TechContent reqContent = realm.where(TechContent.class)
                                .equalTo(TechContent.FIELD_NAME_ENG, reqName, Case.INSENSITIVE).findFirst();
                        boolean isTech = false;
                        if (reqContent != null) {
                            Technology reqTech = realm.where(Technology.class)
                                    .equalTo(Technology.FIELD_CONTENT_ID, reqContent.getId())
                                    .and().equalTo(Technology.FIELD_LEVEL_VAL, reqLv).findFirst();
                            if (reqTech != null) {
                                isTech = true;
                                reqTechs.add(reqTech);
                                requirement.deleteFromRealm();
                            }
                        }
                        if(!isTech) {
                            BuildContent reqBdContent = realm.where(BuildContent.class)
                                    .equalTo(BuildContent.FIELD_NAME_ENG, reqName, Case.INSENSITIVE).findFirst();
                            if( reqBdContent != null ) {
                                Building reqBd = realm.where(Building.class)
                                        .equalTo(Building.FIELD_CONTENT_ID, reqBdContent.getId())
                                        .and().equalTo(Building.FIELD_LEVEL_VAL, reqLv).findFirst();
                                if( reqBd != null ) {
                                    reqBuildings.add(reqBd);
                                    requirement.deleteFromRealm();
                                }
                            }
                        }
                    }
                }

                if( required.size() > 0 ) {
                    if( content != null )
                    Log.d(TAG, content.getString(TechContent.FIELD_NAME) + ": " + TextUtils.join(",", required));
                }

                technology.setPreTechList(reqTechs);
                technology.setPreBuildList(reqBuildings);
                technology.updateIntValues();
            }

            RealmResults<Building> buildings = realm.where(Building.class).findAll();
            for (Building building : buildings) {
                BuildContent content = realm.where(BuildContent.class).equalTo(Source.FIELD_ID, building.getInt(Technology.FIELD_CONTENT_ID)).findFirst();
                if (content != null) {
                    building.setContent(content);
                }
                RealmList<RealmString> required = building.getRequirements();
                RealmList<Building> reqBuildings = new RealmList<>();
                for (RealmString requirement : required) {
                    if (requirement != null) {
                        String reqName = requirement.toString().replaceAll("[0-9]|Level", "").trim()
                                .replaceAll("\\s+", "_");
                        int reqLv = NumberUtils.toInt(requirement.toString().replaceAll("[^0-9]", ""), 0);
                        BuildContent reqContent = realm.where(BuildContent.class)
                                .equalTo(BuildContent.FIELD_NAME_ENG, reqName, Case.INSENSITIVE).findFirst();
                        if (reqContent != null) {
                            Building reqBuilding = realm.where(Building.class)
                                    .equalTo(Building.FIELD_CONTENT_ID, reqContent.getId())
                                    .and().equalTo(Building.FIELD_LEVEL_VAL, reqLv).findFirst();
                            if (reqBuilding != null) {
                                reqBuildings.add(reqBuilding);
                                requirement.deleteFromRealm();
                            }
                        }
                    }
                }

                building.setReqBuildings(reqBuildings);
                building.updateIntValues();
            }

            realm.commitTransaction();
        } catch (RuntimeException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }


}
