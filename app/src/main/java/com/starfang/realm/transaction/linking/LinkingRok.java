package com.starfang.realm.transaction.linking;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.ViewModel;

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
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
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

            publishProgress("Civilization", "10");
            for (Civilization civilization : realm.where(Civilization.class).findAll()) {
                civilization.setAttrs(attrOrderedRealmList.getRealmList(realm, civilization.getAttrIds()));
                civilization.setInitCommander(
                        realm.where(Commander.class)
                                .equalTo(Source.FIELD_ID, civilization.getInt(
                                        Civilization.FIELD_COMMANDER_ID)).findFirst());
                publishProgress("Civilization#" + civilization.getId() + ".attrs");
            } // for civilizations

            publishProgress("Commander", "20");
            for (Commander commander : realm.where(Commander.class).findAll()) {

                commander.setCivilization(realm.where(Civilization.class).equalTo(Source.FIELD_ID, commander.getInt(Commander.FIELD_CIVIL)).findFirst());
                commander.setSpecifications(specificationOrderedRealmList.getRealmList(realm, commander.getSpecIds()));
                commander.setSkills(skillOrderedRealmList.getRealmList(realm, commander.getSkillIds()));
                commander.setRarity(realm.where(Rarity.class).equalTo(Source.FIELD_ID, commander.getInt(Commander.FIELD_RARITY_ID)).findFirst());

                publishProgress("Commander#" + commander.getId() + ".[civil,spec,skill,rarity]");
            } // for commanders

            publishProgress("ItemSet", "30");
            for (ItemSet itemSet : realm.where(ItemSet.class).findAll()) {
                itemSet.setAttrs(attrOrderedRealmList.getRealmList(realm, itemSet.getAttrIds()));
                publishProgress("ItemSet#" + itemSet.getId() + ".attrs");
            } // for item sets

            publishProgress("ItemMaterial", "40");
            for (ItemMaterial material : realm.where(ItemMaterial.class).findAll()) {
                material.setRarity(realm.where(Rarity.class).equalTo(Source.FIELD_ID, material.getInt(ItemMaterial.FIELD_RARITY_ID)).findFirst());
                publishProgress("ItemMaterial#" + material.getId() + ".rarity");
            } // for item materials

            publishProgress("Item", "50");
            for (Item item : realm.where(Item.class).findAll()) {
                item.setCategory(realm.where(ItemCategory.class).equalTo(Source.FIELD_ID, item.getInt(Item.FIELD_CATEGORY_ID)).findFirst());
                item.setRarity(realm.where(Rarity.class).equalTo(Source.FIELD_ID, item.getInt(Item.FIELD_RARITY_ID)).findFirst());
                item.setItemSet(realm.where(ItemSet.class).equalTo(Source.FIELD_ID, item.getInt(Item.FIELD_SET_ID)).findFirst());
                item.setAttrs(attrOrderedRealmList.getRealmList(realm, item.getAttrIds()));
                item.setMaterials(materialOrderedRealmList.getRealmList(realm, item.getMaterialIds()));
                publishProgress("Item#" + item.getId() + ".[cate,rarity,set,attr,material]");
            } // for items

            RealmResults<Building> buildings = realm.where(Building.class).findAll();
            publishProgress("Building", "60");
            for (Building building : buildings) {
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
                    publishProgress("Building#" + building.getId() + ".reqBuild");
                }
            } // for buildings
            for (Building building : buildings) {
                RealmList<Building> preBuildings = new RealmList<>();
                recursivePreBuildingSearch(preBuildings, building);
                if (!preBuildings.isEmpty()) {
                    building.setPreBuildings(preBuildings);
                }
                publishProgress("Building#" + building.getId() + ".preBuild");
            } // for buildings

            publishProgress("deleting unnecessary techs...", "70");
            RealmResults<TechContent> delete_contents = realm.where(TechContent.class).in(Source.FIELD_ID, DELETE_CONTENTS_ID).findAll();
            delete_contents.deleteAllFromRealm();
            RealmResults<Technology> delete_techs = realm.where(Technology.class).in(Technology.FIELD_CONTENT_ID, DELETE_CONTENTS_ID).findAll();
            delete_techs.deleteAllFromRealm();

            publishProgress("Technology", "80");
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
                publishProgress("Technology#" + technology.getId() + ".[reqTech,reqBuild]");
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
                publishProgress("Technology#" + technology.getId() + ".[preTech,preBuild]");
            } // for technologies


            publishProgress("Tech&Build", "90");
            for (BuildContent buildContent : realm.where(BuildContent.class).findAll()) {
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

            }

            publishProgress("done", "100");


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
