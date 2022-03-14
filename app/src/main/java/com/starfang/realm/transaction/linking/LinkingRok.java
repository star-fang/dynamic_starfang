package com.starfang.realm.transaction.linking;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.ViewModel;

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
import com.starfang.realm.source.rok.Land;
import com.starfang.realm.source.rok.Rarity;
import com.starfang.realm.source.rok.RokName;
import com.starfang.realm.source.rok.Skill;
import com.starfang.realm.source.rok.Specification;
import com.starfang.realm.source.rok.TechContent;
import com.starfang.realm.source.rok.Technology;
import com.starfang.realm.source.rok.Vertex;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class LinkingRok extends LinkingTask<Void, String, Void> {

    private static final Integer[] DELETE_CONTENTS_ID = {
            900028, 900029, 900030, 900031, 900032, 900033, 900034, 900035,
            900051, 900052, 900053, 900054, 900055, 900056, 900057, 900058
    };

    private static final String TAG = "FANG_LINKING_ROK";

    public LinkingRok(String title, Context context, ViewModel model, WeakReference<ViewModel> activityVmRef) {
        super(title, context, model, activityVmRef);
    }


    @Override
    protected Void doInBackground(Void... v) {


        try (Realm realm = Realm.getDefaultInstance()) {

            realm.beginTransaction();

            OrderedRealmList<Attribute> attrOrderedRealmList = new OrderedRealmList<>(Attribute.class);
            OrderedRealmList<ItemMaterial> materialOrderedRealmList = new OrderedRealmList<>(ItemMaterial.class);
            OrderedRealmList<Skill> skillOrderedRealmList = new OrderedRealmList<>(Skill.class);
            OrderedRealmList<Specification> specificationOrderedRealmList = new OrderedRealmList<>(Specification.class);


            RealmResults<Civilization> step1 = realm.where(Civilization.class).findAll();
            RealmResults<Commander> step2 = realm.where(Commander.class).findAll();
            RealmResults<ItemSet> step3 =  realm.where(ItemSet.class).findAll();
            RealmResults<ItemMaterial> step4 = realm.where(ItemMaterial.class).findAll();
            RealmResults<Item> step5 = realm.where(Item.class).findAll();
            RealmResults<Building> step6x2 = realm.where(Building.class).findAll();
            RealmResults<Technology> step7x2 = realm.where(Technology.class).findAll();
            RealmResults<BuildContent> step8 = realm.where(BuildContent.class).findAll();
            RealmResults<Vertex> step9 = realm.where(Vertex.class).findAll();
            RealmResults<Land> step10 = realm.where(Land.class).findAll();

            final int maxProgress = step1.size() + step2.size() + step3.size() + step4.size() + step5.size() +
            step6x2.size() * 2 + step7x2.size() *2 + step8.size() + step9.size() + step10.size();

            ProgressCounter progressCounter = new ProgressCounter(maxProgress);
            publishProgress("Civilization");
            for (Civilization civilization : step1) {
                civilization.setAttrs(attrOrderedRealmList.getRealmList(realm, civilization.getAttrIds()));
                civilization.setInitCommander(
                        realm.where(Commander.class)
                                .equalTo(Source.FIELD_ID, civilization.getInt(
                                        Civilization.FIELD_COMMANDER_ID)).findFirst());

                publishProgress("Civilization#" + civilization.getId() + ".attrs", progressCounter.countUp());
            } // for civilizations

            publishProgress("Commander");
            for (Commander commander : step2) {

                commander.setCivilization(realm.where(Civilization.class).equalTo(Source.FIELD_ID, commander.getInt(Commander.FIELD_CIVIL)).findFirst());
                commander.setSpecifications(specificationOrderedRealmList.getRealmList(realm, commander.getSpecIds()));
                commander.setSkills(skillOrderedRealmList.getRealmList(realm, commander.getSkillIds()));
                commander.setRarity(realm.where(Rarity.class).equalTo(Source.FIELD_ID, commander.getInt(Commander.FIELD_RARITY_ID)).findFirst());

                publishProgress("Commander#" + commander.getId() + ".[civil,spec,skill,rarity]", progressCounter.countUp());
            } // for commanders

            publishProgress("ItemSet");
            for (ItemSet itemSet : step3) {
                itemSet.setAttrs(attrOrderedRealmList.getRealmList(realm, itemSet.getAttrIds()));
                publishProgress("ItemSet#" + itemSet.getId() + ".attrs", progressCounter.countUp());
            } // for item sets

            publishProgress("ItemMaterial");
            for (ItemMaterial material : step4) {
                material.setRarity(realm.where(Rarity.class).equalTo(Source.FIELD_ID, material.getInt(ItemMaterial.FIELD_RARITY_ID)).findFirst());
                publishProgress("ItemMaterial#" + material.getId() + ".rarity", progressCounter.countUp());
            } // for item materials

            publishProgress("Item");
            for (Item item : step5) {
                item.setCategory(realm.where(ItemCategory.class).equalTo(Source.FIELD_ID, item.getInt(Item.FIELD_CATEGORY_ID)).findFirst());
                item.setRarity(realm.where(Rarity.class).equalTo(Source.FIELD_ID, item.getInt(Item.FIELD_RARITY_ID)).findFirst());
                item.setItemSet(realm.where(ItemSet.class).equalTo(Source.FIELD_ID, item.getInt(Item.FIELD_SET_ID)).findFirst());
                item.setAttrs(attrOrderedRealmList.getRealmList(realm, item.getAttrIds()));
                item.setMaterials(materialOrderedRealmList.getRealmList(realm, item.getMaterialIds()));
                publishProgress("Item#" + item.getId() + ".[cate,rarity,set,attr,material]", progressCounter.countUp());
            } // for items


            publishProgress("Building");
            for (Building building : step6x2) {
                BuildContent content = realm.where(BuildContent.class).equalTo(Source.FIELD_ID, building.getInt(Technology.FIELD_CONTENT_ID)).findFirst();
                if (content != null) {
                    building.setContent(content);
                    RealmList<RealmString> required = building.getRequirements();
                    RealmList<Building> reqBuildings = new RealmList<>();
                    for (RealmString requirement : required) {
                        if (requirement != null) {
                            String reqName = requirement.toString().replaceAll("Lv|Level|[0-9]{1,2}", "").trim()
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
                    publishProgress("Building#" + building.getId() + ".reqBuild", progressCounter.countUp());
                }
            } // for buildings
            for (Building building : step6x2) {
                RealmList<Building> preBuildings = new RealmList<>();
                recursivePreBuildingSearch(preBuildings, building);
                if (!preBuildings.isEmpty()) {
                    building.setPreBuildings(preBuildings);
                }
                publishProgress("Building#" + building.getId() + ".preBuild", progressCounter.countUp());
            } // for buildings

            publishProgress("Technology");
            for (Technology technology : step7x2) {
                TechContent content = realm.where(TechContent.class).equalTo(Source.FIELD_ID, technology.getInt(Technology.FIELD_CONTENT_ID)).findFirst();
                if (content != null) {
                    technology.setContent(content);
                    RealmList<RealmString> required = technology.getRequirements();
                    RealmList<Technology> reqTechs = new RealmList<>();
                    RealmList<Building> reqBuildings = new RealmList<>();
                    for (RealmString requirement : required) {
                        if (requirement != null) {
                            String reqName = requirement.toString().replaceAll("Lv|Level|[0-9]{1,2}", "").trim()
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
                publishProgress("Technology#" + technology.getId() + ".[reqTech,reqBuild]", progressCounter.countUp());
            } // for technologies
            for (Technology technology : step7x2) {
                RealmList<Technology> preTechs = new RealmList<>();
                RealmList<Building> preBuildings = new RealmList<>();
                recursivePreTechSearch(preTechs, preBuildings, technology);
                if (!preTechs.isEmpty()) {
                    technology.setPreTechs(preTechs);
                }
                if (!preBuildings.isEmpty()) {
                    technology.setPreBuildings(preBuildings);
                }
                publishProgress("Technology#" + technology.getId() + ".[preTech,preBuild]", progressCounter.countUp());
            } // for technologies


            publishProgress("Tech&Build");
            for (BuildContent buildContent : step8) {
                final RealmList<RealmString> facts = buildContent.getFacts();
                if (facts != null) {
                    for (int i = 0; i < facts.size(); i++) {
                        RealmString fact = facts.get(i);
                        if (fact != null && fact.toString().equals("unlocks")) {
                            facts.remove(i);
                            for (Building building : realm.where(Building.class).equalTo(Building.FIELD_CONTENT_ID, buildContent.getId()).findAll()) {
                                RealmList<RealmString> figures = building.getFigures();
                                if (figures != null) {
                                    RealmList<RealmString> unlocksList = new RealmList<>();
                                    makeUnlocksList(unlocksList, realm, figures.get(i));
                                    if (unlocksList.size() > 0) {
                                        building.setUnlocksList(unlocksList);
                                    }
                                    figures.remove(i);
                                }
                            }
                            break;
                        }
                    }
                }
                publishProgress("Tech&Build", progressCounter.countUp());
            }

            publishProgress("Vertex");
            for(Vertex vertex : step9 ) {
                vertex.setName( realm.where(RokName.class).equalTo(Source.FIELD_ID, vertex.getInt(Vertex.FIELD_NAME_ID)).findFirst());
                RealmList<Land> lands = new RealmList<>();
                for(RealmInteger landIdObj : vertex.getLandIds()) {
                    if( landIdObj != null ) {
                        Land land = realm.where(Land.class).equalTo(Source.FIELD_ID, landIdObj.getValue()).findFirst();
                        if( land != null ) {
                            lands.add( land );
                        }
                    }
                }
                vertex.setLands(lands);
                publishProgress("Vertex", progressCounter.countUp());
            }

            publishProgress("Land");
            for( Land land : step10 ) {
                land.setName( realm.where(RokName.class).equalTo(Source.FIELD_ID, land.getInt(Land.FIELD_NAME_ID)).findFirst());
                publishProgress("Land", progressCounter.countUp());
            }

            publishProgress("deleting unnecessary techs...");
            RealmResults<TechContent> delete_contents = realm.where(TechContent.class).in(Source.FIELD_ID, DELETE_CONTENTS_ID).findAll();
            delete_contents.deleteAllFromRealm();
            RealmResults<Technology> delete_techs = realm.where(Technology.class).in(Technology.FIELD_CONTENT_ID, DELETE_CONTENTS_ID).findAll();
            delete_techs.deleteAllFromRealm();

            publishProgress("done");


            realm.commitTransaction();
        } catch (RuntimeException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            this.cancel(true);
        }


        return null;
    }

    // this function must proceeded in realm transaction
    private void makeUnlocksList(RealmList<RealmString> list, Realm realm, RealmString info) {
        if (info != null) {
            String str = info.toString();
            try {
                JSONArray jsonArray = new JSONArray(str);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String text = jsonArray.getString(i);
                    String inBracket = null;
                    String level = null;
                    Pattern bracketPattern = Pattern.compile("\\(.*?\\)");
                    Pattern levelPattern = Pattern.compile("[0-9]{1,2}-[0-9]{1,2}|[0-9]{1,2}");
                    Matcher bracketMatcher = bracketPattern.matcher(text);
                    Matcher levelMatcher = levelPattern.matcher(text);
                    if (bracketMatcher.find()) {
                        inBracket = bracketMatcher.group();
                        text = text.replaceAll("\\(.*?\\)", "").trim();
                    }
                    if (levelMatcher.find()) {
                        level = "Level " + levelMatcher.group(0);
                        text = text.replaceAll("Level|[0-9]{1,2}-[0-9]{1,2}|[0-9]{1,2}", "").trim();
                    }

                    BuildContent buildContent = realm.where(BuildContent.class).equalTo(BuildContent.FIELD_NAME_ENG, text.replaceAll("\\s+", "_"), Case.INSENSITIVE).findFirst();
                    if (buildContent != null) {
                        text = buildContent.getString(Source.FIELD_NAME);
                    } else {
                        TechContent techContent = realm.where(TechContent.class).equalTo(TechContent.FIELD_NAME_ENG, text.replaceAll("\\s+", "_"), Case.INSENSITIVE).findFirst();
                        if (techContent != null) {
                            text = techContent.getString(Source.FIELD_NAME);
                        }
                    }
                    StringBuilder nameBuilder = new StringBuilder();
                    nameBuilder.append(text);
                    if (level != null) nameBuilder.append(" ").append(level);
                    if (inBracket != null) nameBuilder.append(" ").append(inBracket);
                    RealmString nameObj = new RealmString(nameBuilder.toString());
                    list.add(realm.copyToRealm(nameObj));
                }
            } catch (JSONException e) {
                RealmString nameObj = new RealmString(str);
                list.add(realm.copyToRealm(nameObj));
            }
        }
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
