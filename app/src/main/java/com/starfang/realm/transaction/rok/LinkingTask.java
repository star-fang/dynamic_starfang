package com.starfang.realm.transaction.rok;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.starfang.CMDActivity;
import com.starfang.realm.Cmd;
import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.Source;
import com.starfang.realm.source.rok.Attribute;
import com.starfang.realm.source.rok.BuildContent;
import com.starfang.realm.source.rok.Building;
import com.starfang.realm.source.rok.Civilization;
import com.starfang.realm.source.rok.Commander;
import com.starfang.realm.source.rok.Item;
import com.starfang.realm.source.rok.ItemCategory;
import com.starfang.realm.source.rok.ItemMaterial;
import com.starfang.realm.source.rok.ItemSet;
import com.starfang.realm.source.rok.Rarity;
import com.starfang.realm.source.rok.Skill;
import com.starfang.realm.source.rok.Specification;
import com.starfang.realm.source.rok.TechContent;
import com.starfang.realm.source.rok.Technology;

import org.apache.commons.lang3.math.NumberUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class LinkingTask extends AsyncTask<Void, Void, Void> {

    private static final Integer[] DELETE_CONTENTS_ID = {
            900028, 900029, 900030, 900031, 900032, 900033, 900034, 900035,
            900051, 900052, 900053, 900054, 900055, 900056, 900057, 900058
    };

    private static final String TAG = "FANG_LINKING";

    private final WeakReference<Context> contextWeakReference;

    LinkingTask(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void v) {
        super.onPostExecute(v);
        Log.d(TAG, "ROK Linking complete");
    }


    private static class OrderedList<T extends RealmObject> {

        private final Class<T> clazz;

        OrderedList(Class<T> clazz) {
            this.clazz = clazz;
        }

        List<T> getList(Realm realm, List<RealmInteger> idArr) {
            List<T> list = new ArrayList<>();
            for (RealmInteger value : idArr) {
                T a = value == null ? null : realm.where(clazz).equalTo(Source.FIELD_ID, value.getValue()).findFirst();
                list.add(a);
            }
            return list;
        }
    }

    @Override
    protected Void doInBackground(Void... v) {


        try (Realm realm = Realm.getDefaultInstance()) {

            realm.beginTransaction();

            OrderedList<Attribute> attrOrderedList = new OrderedList<>(Attribute.class);
            OrderedList<ItemMaterial> materialOrderedList = new OrderedList<>(ItemMaterial.class);
            OrderedList<Skill> skillOrderedList = new OrderedList<>(Skill.class);
            OrderedList<Specification> specificationOrderedList = new OrderedList<>(Specification.class);

            for (Civilization civilization : realm.where(Civilization.class).findAll()) {
                civilization.setAttrs(attrOrderedList.getList(realm, civilization.getAttrIds()));
            } // for civilizations

            for (Commander commander : realm.where(Commander.class).findAll()) {

                commander.setCivilization(realm.where(Civilization.class).equalTo(Source.FIELD_ID, commander.getInt(Commander.FIELD_CIVIL)).findFirst());
                commander.setSpecifications(specificationOrderedList.getList(realm, commander.getSpecIds()));
                commander.setSkills(skillOrderedList.getList(realm, commander.getSkillIds()));

                commander.setRarity(realm.where(Rarity.class).equalTo(Source.FIELD_ID, commander.getInt(Commander.FIELD_RARITY_ID)).findFirst());
            } // for commanders

            for (ItemSet itemSet : realm.where(ItemSet.class).findAll()) {
                itemSet.setAttrs(attrOrderedList.getList(realm, itemSet.getAttrIds()));
            } // for item sets

            for (ItemMaterial material : realm.where(ItemMaterial.class).findAll()) {
                material.setRarity(realm.where(Rarity.class).equalTo(Source.FIELD_ID, material.getInt(ItemMaterial.FIELD_RARITY_ID)).findFirst());
            } // for item materials

            for (Item item : realm.where(Item.class).findAll()) {
                item.setCategory(realm.where(ItemCategory.class).equalTo(Source.FIELD_ID, item.getInt(Item.FIELD_CATEGORY_ID)).findFirst());
                item.setRarity(realm.where(Rarity.class).equalTo(Source.FIELD_ID, item.getInt(Item.FIELD_RARITY_ID)).findFirst());
                item.setItemSet(realm.where(ItemSet.class).equalTo(Source.FIELD_ID, item.getInt(Item.FIELD_SET_ID)).findFirst());
                item.setAttrs(attrOrderedList.getList(realm, item.getAttrIds()));
                item.setMaterials(materialOrderedList.getList(realm, item.getMaterialIds()));
            } // for items


            RealmResults<Building> buildings = realm.where(Building.class).findAll();
            for (Building building : buildings) {
                BuildContent content = realm.where(BuildContent.class).equalTo(Source.FIELD_ID, building.getInt(Technology.FIELD_CONTENT_ID)).findFirst();
                if (content != null) {
                    building.setContent(content);
                    RealmList<RealmString> required = building.getRequirements();
                    RealmList<Building> reqBuildings = new RealmList<>();
                    for (RealmString requirement : required) {
                        if (requirement != null) {
                            String reqName = requirement.toString().replaceAll("Level [0-9]{1,2}", "").trim()
                                    .replaceAll("\\s+", "_");
                            int reqLv = NumberUtils.toInt(requirement.toString().replaceAll("[^0-9]", ""), 0);
                            BuildContent reqContent = realm.where(BuildContent.class)
                                    .equalTo(BuildContent.FIELD_NAME_ENG, reqName, Case.INSENSITIVE).findFirst();
                            //Log.d(TAG, "find: " + reqName + " Lv." + reqLv);
                            if (reqContent != null) {
                                Building reqBuilding = realm.where(Building.class)
                                        .equalTo(Building.FIELD_CONTENT_ID, reqContent.getId())
                                        .and().equalTo(Building.FIELD_LEVEL, reqLv + "").findFirst();
                                if (reqBuilding != null) {
                                    if (!reqBuildings.contains(reqBuilding))
                                        reqBuildings.add(reqBuilding);
                                    //required.remove(requirement);
                                }
                            }
                        }
                    }
                    building.setReqBuildings(reqBuildings);
                    building.updateIntValues();
                }
            } // for buildings

            for (Building building : buildings) {
                RealmList<Building> preBuildings = new RealmList<>();
                recursivePreBuildingSearch(preBuildings, building);
                if (!preBuildings.isEmpty()) {
                    building.setPreBuildings(preBuildings);
                }
            } // for buildings

            RealmResults<TechContent> delete_contents = realm.where(TechContent.class).in(Source.FIELD_ID, DELETE_CONTENTS_ID).findAll();
            delete_contents.deleteAllFromRealm();
            RealmResults<Technology> delete_techs = realm.where(Technology.class).in(Technology.FIELD_CONTENT_ID, DELETE_CONTENTS_ID).findAll();
            delete_techs.deleteAllFromRealm();

            RealmResults<Technology> technologies = realm.where(Technology.class).findAll();

            for (Technology technology : technologies) {
                TechContent content = realm.where(TechContent.class).equalTo(Source.FIELD_ID, technology.getInt(Technology.FIELD_CONTENT_ID)).findFirst();
                if (content != null) {
                    technology.setContent(content);
                    RealmList<RealmString> required = technology.getRequirements();
                    RealmList<Technology> reqTechs = new RealmList<>();
                    RealmList<Building> reqBuildings = new RealmList<>();
                    for (RealmString requirement : required) {
                        if (requirement != null) {
                            String reqName = requirement.toString().replaceAll("Level [0-9]{1,2}", "").trim()
                                    .replaceAll("\\s+", "_");
                            int reqLv = NumberUtils.toInt(requirement.toString().replaceAll("[^0-9]", ""), 0);

                            TechContent reqContent = realm.where(TechContent.class)
                                    .equalTo(TechContent.FIELD_NAME_ENG, reqName, Case.INSENSITIVE).findFirst();
                            boolean isValid = false;
                            String check = "";
                            if (reqContent != null) {
                                check += reqContent.getString(Source.FIELD_NAME) + " Lv.";
                                Technology reqTech = realm.where(Technology.class)
                                        .equalTo(Technology.FIELD_CONTENT_ID, reqContent.getId())
                                        .and().equalTo(Technology.FIELD_LEVEL, reqLv + "").findFirst();
                                if (reqTech != null) {
                                    check += reqTech.getString(Technology.FIELD_LEVEL);
                                    if (!reqTechs.contains(reqTech))
                                        reqTechs.add(reqTech);
                                    isValid = true;
                                }
                            }
                            if (!isValid) {
                                BuildContent reqBdContent = realm.where(BuildContent.class)
                                        .equalTo(BuildContent.FIELD_NAME_ENG, reqName, Case.INSENSITIVE).findFirst();
                                //Log.d(TAG, "bd find : " + reqName + reqLv);
                                if (reqBdContent != null) {
                                    //Log.d(TAG, "BdContent : " + reqBdContent.getString(Source.FIELD_NAME) );
                                    Building reqBd = realm.where(Building.class)
                                            .equalTo(Building.FIELD_CONTENT_ID, reqBdContent.getId())
                                            .and().equalTo(Building.FIELD_LEVEL_VAL, reqLv).findFirst();
                                    if (reqBd != null) {
                                        if (!reqBuildings.contains(reqBd)) {
                                            reqBuildings.add(reqBd);
                                        }
                                        isValid = true;
                                    }
                                }
                            }

                            if (!isValid) {
                                Log.d(TAG, content.getString(TechContent.FIELD_NAME)
                                        + ": " + requirement.toString() + " [invalid] " + check);
                            }
                        } // if requirement != null
                    } // for required

                    technology.setReqTechs(reqTechs);
                    technology.setReqBuildings(reqBuildings);
                    technology.updateIntValues();
                } // if content != null
            } // for technologies

            for (Technology technology : technologies) {
                RealmList<Technology> preTechs = new RealmList<>();
                RealmList<Building> preBuildings = new RealmList<>();
                recursivePreTechSearch(preTechs, preBuildings, technology);
                if (!preTechs.isEmpty()) {
                    technology.setPreTechs(preTechs);
                }
                if (!preBuildings.isEmpty()) {
                    technology.setPreBuildings(preBuildings);
                }
            } // for technologies

            Cmd cmd = new Cmd(false);
            cmd.setName("멍멍이");
            cmd.setText("데이터 연결 완료다멍");
            realm.copyToRealm(cmd);
            realm.commitTransaction();
        } catch (RuntimeException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        Intent broadcastIntent = new Intent(CMDActivity.ACTION_ENABLE_ET);
        contextWeakReference.get().sendBroadcast(broadcastIntent);

        return null;
    }

    private void recursivePreTechSearch(RealmList<Technology> preTechs, RealmList<Building> preBuildings, Technology currentTech) {
        RealmList<Technology> reqTechs = currentTech.getReqTechs();
        RealmList<Building> reqBuildings = currentTech.getReqBuildings();

        if (reqBuildings != null) {
            for (Building reqBuilding : reqBuildings) {
                if (!preBuildings.contains(reqBuilding)) {
                    preBuildings.add(reqBuilding);
                    for (Building preBuilding : reqBuilding.getPreBuildings()) {
                        if (!preBuildings.contains(preBuilding)) {
                            preBuildings.add(preBuilding);
                        }
                    }
                }
            }
        }

        if (reqTechs != null && !reqTechs.isEmpty()) {
            for (Technology reqTech : reqTechs) {
                if (!preTechs.contains(reqTech)) {
                    preTechs.add(reqTech);
                    recursivePreTechSearch(preTechs, preBuildings, reqTech);
                }
            }
        }
    }


    private void recursivePreBuildingSearch(RealmList<Building> preBuildings, Building currentBuilding) {
        RealmList<Building> reqBuildings = currentBuilding.getReqBuildings();

        if (reqBuildings != null) {
            for (Building reqBuilding : reqBuildings) {
                if (!preBuildings.contains(reqBuilding)) {
                    preBuildings.add(reqBuilding);
                    recursivePreBuildingSearch(preBuildings, reqBuilding);
                }
            }
        }
    }

}
