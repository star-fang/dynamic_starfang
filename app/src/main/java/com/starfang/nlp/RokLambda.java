package com.starfang.nlp;

import android.text.TextUtils;
import android.util.Log;

import com.starfang.realm.primitive.RealmDouble;
import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.Source;
import com.starfang.realm.source.rok.Attribute;
import com.starfang.realm.source.rok.Building;
import com.starfang.realm.source.rok.Civilization;
import com.starfang.realm.source.rok.Commander;
import com.starfang.realm.source.rok.Item;
import com.starfang.realm.source.rok.ItemCategory;
import com.starfang.realm.source.rok.ItemMaterial;
import com.starfang.realm.source.rok.ItemSet;
import com.starfang.realm.source.rok.Rarity;
import com.starfang.realm.source.rok.RokCalcUtils;
import com.starfang.realm.source.rok.RokUser;
import com.starfang.realm.source.rok.Skill;
import com.starfang.realm.source.rok.TechContent;
import com.starfang.realm.source.rok.Technology;

import org.apache.commons.lang3.math.NumberUtils;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;


class RokLambda {

    private static final String TAG = "FANG_MOD_CAT";

    private enum CMD_ENUM {CMD_DESC, CMD_COMMANDER, CMD_SKILL, CMD_SPEC, CMD_CIVIL, CMD_ITEM, CMD_CALC, CMD_WIKI, CMD_TECH, CMD_RESEARCH, CMD_BUILD, CMD_COMMIT, CMD_DEFAULT}

    private static final String[] CMD_CERTAIN = {"설명", "사령관", "스킬", "특성", "문명", "아이템", "계산", "위키", "기술", "연구", "건설", "완료", "냥"};

    private static final String ROK_WIKI = "http://rok.wiki/";
    private static final String ROK_WIKI_HERO_DIR = "bbs/board.php?bo_table=hero&wr_id=";

    private static final String REGEX_SPACE = "\\s+";
    private static final String REGEX_EXCEPT_DIGITS = "[^0-9]";
    private static final String REGEX_DIGITS = "[0-9]";

    private static CMD_ENUM findCMD(RealmString rString) {
        String req = rString.toString().trim();
        CMD_ENUM cmd = CMD_ENUM.CMD_DEFAULT;
        for (CMD_ENUM certain : CMD_ENUM.values()) {
            try {
                int certainIndex = certain.ordinal();
                if (certainIndex < CMD_CERTAIN.length && req != null) {
                    String probKey = CMD_CERTAIN[certainIndex];
                    int reqLength = req.length();
                    int keyLength = probKey.length();

                    if (reqLength >= keyLength) {

                        try {
                            if (req.substring(reqLength - keyLength).equals(probKey)) {
                                cmd = certain;
                                req = (reqLength == keyLength) ? null : req.substring(0, reqLength - keyLength);
                                rString.setValue(req);
                                break;
                            }
                        } catch (StringIndexOutOfBoundsException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }

                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return cmd;
    }

    public static List<String> processReq(String req, Realm realm, String sendCat, long forumId) {

        RealmString rString = new RealmString(req);
        CMD_ENUM cmd = findCMD(rString);
        req = rString.toString();

        PlayWithCat calc = (l, q) -> {
            if (q == null) {
                l.add("계산\r\n-----------------\r\nex1) Math.floor(1000/24) 계산냥\r\nex2) 1 + 33 % 2 계산냥");
                return;
            }

            org.mozilla.javascript.Context mContext = org.mozilla.javascript.Context.enter();
            mContext.setOptimizationLevel(-1);
            try {
                Scriptable scope = mContext.initSafeStandardObjects();
                Object resultObject = mContext.evaluateString(scope, q, "<cmd>", 1, null);
                if (resultObject != null) {
                    l.add(resultObject.toString());
                }
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
                l.add("몰랑");
            }
        };

        PlayWithCat techByName = ((l, q) -> {
            if (q == null) {
                return;
            }

            int level = NumberUtils.toInt(q.replaceAll("[^0-9]", ""), 0);
            q = q.replaceAll("[0-9]", "");
            q = q.trim();

            RealmResults<TechContent> contents = realm.where(TechContent.class).equalTo(
                    Source.FIELD_NAME, q).or().contains(TechContent.FIELD_NAME_WITHOUT_BLANK, q).findAll();


            for (TechContent content : contents) {
                StringBuilder contentBuilder = new StringBuilder();
                contentBuilder.append("[").append(content.getString(TechContent.FIELD_CATEGORY_KOR))
                        .append(" 기술] ").append(content.getString(Source.FIELD_NAME));

                RealmQuery<Technology> techQuery = realm.where(Technology.class).equalTo(Technology.FIELD_CONTENT_ID, content.getId());
                if (level > 0) {
                    techQuery.and().equalTo(Technology.FIELD_LEVEL_VAL, level);
                }
                RealmResults<Technology> technologies = techQuery.findAll().sort(Technology.FIELD_LEVEL_VAL, Sort.ASCENDING);

                if (technologies.size() == 1) {
                    Technology tech = technologies.first();
                    if (tech != null) {

                        if (level > 0) {
                            contentBuilder.append(" level").append(level);
                        }

                        RealmList<RealmString> facts = content.getFacts();
                        RealmList<RealmString> figures = tech.getFigures();
                        for (int i = 0; i < facts.size(); i++) {
                            RealmString fact = facts.get(i);
                            if (fact != null) {
                                contentBuilder.append("\r\n - ").append(fact);
                                RealmString figure = figures.get(i);
                                if (figure != null) {
                                    contentBuilder.append(" ").append(figure);
                                }
                            }
                        }

                        int seconds = tech.getInt(Technology.FIELD_SECONDS);
                        contentBuilder.append("\r\n - 연구 시간: ").append(RokCalcUtils.secondsToString(seconds))
                                .append(" (").append(seconds).append("초)");

                        RealmList<Technology> reqTechs = tech.getReqTechs();
                        int reqTechsSize = reqTechs.size();
                        if (reqTechsSize > 0) {
                            for (int i = 0; i < reqTechsSize; i++) {
                                Technology reqTech = reqTechs.get(i);
                                if (reqTech != null) {
                                    contentBuilder.append("\r\n").append(" - 요구 기술");
                                    if (reqTechsSize > 1) {
                                        contentBuilder.append((i + 1));
                                    }
                                    contentBuilder.append(": ")
                                            .append(reqTech.getContent().getString(Source.FIELD_NAME))
                                            .append(" Lv.").append(reqTech.getInt(Technology.FIELD_LEVEL_VAL));
                                }
                            }
                        }
                        RealmList<Building> reqBuilds = tech.getReqBuildings();
                        int reqBuildSize = reqBuilds.size();
                        if (reqBuildSize > 0) {
                            for (int i = 0; i < reqBuildSize; i++) {
                                Building reqBuild = reqBuilds.get(i);
                                if (reqBuild != null) {
                                    contentBuilder.append("\r\n").append(" - 요구 건물");
                                    if (reqBuildSize > 1) {
                                        contentBuilder.append((i + 1));
                                    }
                                    contentBuilder.append(": ")
                                            .append(reqBuild.getContent().getString(Source.FIELD_NAME))
                                            .append(" Lv.").append(reqBuild.getInt(Building.FIELD_LEVEL_VAL));
                                }
                            }
                        }

                        int foodCost = tech.getInt(Technology.FIELD_COST_FOOD);
                        if (foodCost > 0)
                            contentBuilder.append("\r\n").append(" - 식량: ").append(RokCalcUtils.quantityToString(foodCost));
                        int woodCost = tech.getInt(Technology.FIELD_COST_FOOD);
                        if (woodCost > 0)
                            contentBuilder.append("\r\n").append(" - 목재: ").append(RokCalcUtils.quantityToString(woodCost));
                        int stoneCost = tech.getInt(Technology.FIELD_COST_STONE);
                        if (stoneCost > 0)
                            contentBuilder.append("\r\n").append(" - 석재: ").append(RokCalcUtils.quantityToString(stoneCost));
                        int goldCost = tech.getInt(Technology.FIELD_COST_GOLD);
                        if (goldCost > 0)
                            contentBuilder.append("\r\n").append(" - 금화: ").append(RokCalcUtils.quantityToString(goldCost));

                    }
                } else {
                    contentBuilder.append("\r\n - ").append(content.getString(TechContent.FIELD_DESCRIPTION));
                    for (Technology technology : technologies) {
                        int seconds = technology.getInt(Technology.FIELD_SECONDS);
                        contentBuilder.append("\r\n").append(" - level").append(technology.getInt(Technology.FIELD_LEVEL_VAL)).append(": ");
                        RealmList<RealmString> figures = technology.getFigures();
                        if (figures != null) {
                            contentBuilder.append(TextUtils.join(", ", figures)).append(", ");
                        }
                        contentBuilder.append(RokCalcUtils.secondsToString(seconds));
                        //.append(" (").append(seconds).append("초)\r\n");
                    }
                }
                l.add(contentBuilder.toString());
            }
        });

        Command searchTech = ((l, q, w) -> {
            if (q == null) {
                l.add("기술 연구\r\n-----------------\r\nex1) 경제 기술냥\r\nex2) 군사 기술냥");
            } else {
                q = q.trim();
                StringBuilder lambdaResult = new StringBuilder();
                if (q.equals("경제") || q.equals("군사")) {
                    lambdaResult.append(q).append(" 기술 ");
                    RealmResults<TechContent> contents = realm.where(TechContent.class).equalTo(TechContent.FIELD_CATEGORY_KOR, q).findAll().sort(TechContent.FIELD_ID, Sort.DESCENDING).sort(TechContent.FIELD_TIER, Sort.DESCENDING);
                    StringBuilder techBuilder = new StringBuilder();
                    int techNum = 0;
                    int techTimeInSec = 0;
                    int totalFood = 0, totalWood = 0, totalStone = 0, totalGold = 0;
                    int curTier = -1;
                    for (TechContent content : contents) {
                        int tier = content.getInt(TechContent.FIELD_TIER);
                        if (tier != curTier) {
                            techBuilder.append("\r\n").append("티어 ").append(tier);
                            curTier = tier;
                        }
                        techBuilder.append("\r\n").append(" - ").append(content.getString(Source.FIELD_NAME));
                        RealmResults<Technology> technologies = realm.where(Technology.class).equalTo(Technology.FIELD_CONTENT_ID, content.getId()).findAll().sort(Technology.FIELD_LEVEL_VAL, Sort.DESCENDING);
                        techNum += technologies.size();
                        for (Technology tech : technologies) {
                            totalFood += tech.getInt(Technology.FIELD_COST_FOOD);
                            totalWood += tech.getInt(Technology.FIELD_COST_WOOD);
                            totalStone += tech.getInt(Technology.FIELD_COST_STONE);
                            totalGold += tech.getInt(Technology.FIELD_COST_GOLD);
                            techTimeInSec += tech.getInt(Technology.FIELD_SECONDS);
                        }

                        if (technologies.size() > 1) {
                            techBuilder.append(" level 1 ~ ").append(technologies.size());
                        }

                    }
                    lambdaResult.append(contents.size()).append("개 (연구 ").append(techNum).append("개)\r\n");
                    lambdaResult.append("총 연구 시간: ").append(RokCalcUtils.secondsToString(techTimeInSec)).append("\r\n");
                    lambdaResult.append("식량: ").append(RokCalcUtils.quantityToString(totalFood)).append("\r\n");
                    lambdaResult.append("목재: ").append(RokCalcUtils.quantityToString(totalWood)).append("\r\n");
                    lambdaResult.append("석재: ").append(RokCalcUtils.quantityToString(totalStone)).append("\r\n");
                    lambdaResult.append("금화: ").append(RokCalcUtils.quantityToString(totalGold)).append("\r\n").append("-----------------");
                    lambdaResult.append(techBuilder.toString());
                    l.add(lambdaResult.toString());
                }
            }
        });

        Command searchSkill = ((l, q, w) -> {
            if (q == null) {
                l.add("\"사령관 이름\"   \"스킬 번호\"   냥 << 이렇게 입력하라옹");
            } else {
                q = q.trim();
                String number = q.replaceAll(REGEX_EXCEPT_DIGITS, "");
                String name = q.replaceAll(REGEX_DIGITS, "");

                RealmResults<Commander> commanders = realm.where(Commander.class)
                        .contains(Commander.FIELD_NAME_WITHOUT_BLANK, name.replaceAll(REGEX_SPACE, ""))
                        .or().equalTo(Commander.FIELD_NAME, name).findAll();

                int numberVal = -1;
                if (!TextUtils.isEmpty(number)) {
                    numberVal = Integer.parseInt(number);
                    if (numberVal > 0 && numberVal < 6) {
                        numberVal -= 1;
                    } else {
                        numberVal = -1;
                    }
                }


                for (Commander commander : commanders) {
                    RealmList<Skill> skills = commander.getSkills();
                    if (skills != null) {
                        for (int i = 0; i < skills.size(); i++) {
                            Skill skill = skills.get(i);
                            if (skill != null && (numberVal == -1 || numberVal == i)) {
                                String skillBuilder = commander.getString(Source.FIELD_NAME) + " > " + (i + 1) + "스킬\r\n" +
                                        skill.getString(Source.FIELD_NAME) + " (" + skill.getString(Skill.FIELD_PROPERTY) + ")\r\n" +
                                        skill.getString(Skill.FIELD_DESCRIPTION).replace("<br>", "\r\n");
                                l.add(skillBuilder);
                            }
                        }
                    }
                }


            }
        });

        Command searchCivil = ((l, q, w) -> {
            RealmResults<Civilization> civilizations;
            String header = "";
            if (q == null) {
                civilizations = realm.where(Civilization.class).findAll().sort(Source.FIELD_NAME);
                header = "총 " + civilizations.size() + "개 문명\r\n-----------------";
            } else {
                q = q.trim();
                civilizations = realm.where(Civilization.class)
                        .contains(Civilization.FIELD_ATTRS + "." + Attribute.FIELD_NAME_WITHOUT_BLANK, q)
                        .findAll().sort(Source.FIELD_NAME);
                if (civilizations.size() > 0) {
                    header = q + " 특성 보유 문명: " + civilizations.size() + "개\r\n-----------------";
                }
            }
            if (civilizations.size() > 0) {
                StringBuilder lambdaResult = new StringBuilder();
                lambdaResult.append(header);
                for (Civilization civilization : civilizations) {
                    lambdaResult.append("\r\n").append(civilization.getString(Source.FIELD_NAME));
                }
                l.add(lambdaResult.toString());
            }
        });

        Command searchWiki = ((l, q, w) -> {
            if (q == null) {
                l.add(ROK_WIKI);
            } else {
                q = q.trim();
                RealmResults<Commander> commanders = realm.where(Commander.class)
                        .contains(Commander.FIELD_NAME_WITHOUT_BLANK, q.replaceAll(REGEX_SPACE, ""))
                        .or().equalTo(Commander.FIELD_NAME, q).findAll();

                if (commanders.size() == 0) {
                    return;
                }

                for (Commander commander : commanders) {
                    int commanderId = commander.getId();
                    if (commanderId > 0) {
                        String url = ROK_WIKI + ROK_WIKI_HERO_DIR + commanderId;
                        l.add(commander.getString(Source.FIELD_NAME) + ": " + url);
                    }
                }

            }
        });

        Command searchCommanders = ((l, q, w) -> {
            String header = "";
            RealmResults<Commander> commanders;
            if (q == null) {
                commanders = realm.where(Commander.class).findAll().sort(Commander.FIELD_RARITY);
            } else {
                q = q.trim();
                commanders = realm.where(Commander.class).equalTo(Commander.FIELD_RARITY, q).findAll();
                if (commanders.size() > 0) {
                    header = "희귀도 \"" + q + "\"";
                } else {
                    commanders = realm.where(Commander.class).contains(Commander.FIELD_SPECS + "." + Source.FIELD_NAME, q).findAll();
                    if (commanders.size() > 0) {
                        header = "\"" + q + "\" 특성 보유";
                    }
                }
            }

            if (commanders.size() > 0) {
                header += " 사령관: " + commanders.size() + "명\r\n-----------------";
                StringBuilder lambdaResult = new StringBuilder();
                lambdaResult.append(header);
                for (Commander commander : commanders) {
                    lambdaResult.append("\r\n").append(commander.getString(Source.FIELD_NAME));
                }

                l.add(lambdaResult.toString());
            }


        });

        PlayWithCat civilByName = ((l, q) -> {
            if (q == null) {
                return;
            }
            q = q.trim();
            Log.d(TAG, "civilByName Activated");

            RealmResults<Civilization> civilizations = realm.where(Civilization.class)
                    .equalTo(Civilization.FIELD_NAME, q).findAll();

            for (Civilization civilization : civilizations) {
                StringBuilder civil_info = new StringBuilder();

                civil_info
                        .append(civilization.getString(Commander.FIELD_NAME)).append("\r\n")
                        .append("\"").append(civilization.getString(Civilization.FIELD_COMMENT)).append("\"").append("\r\n\r\n");


                Commander commander = realm.where(Commander.class).equalTo(Source.FIELD_ID, civilization.getInt(Civilization.FIELD_COMMANDER_ID)).findFirst();
                if (commander != null) {
                    civil_info.append("초기 사령관: ").append(commander.getString(Source.FIELD_NAME)).append("\r\n");
                }

                RealmList<Attribute> attrs = civilization.getAttrs();
                RealmList<RealmDouble> vals = civilization.getAttrVals();
                for (int i = 0, number = 1; i < attrs.size(); i++) {
                    Attribute attr = attrs.get(i);
                    if (attr != null) {
                        RealmDouble valObj = vals.get(i);
                        civil_info.append("보너스").append(number).append(": ")
                                .append(attr.getFormWithValue(valObj == null ? null : valObj.getValue())).append("\r\n");
                        number++;
                    }
                }

                civil_info.append("특수 유닛: ").append(civilization.getString(Civilization.FIELD_SPECIAL_UNIT));
                l.add(civil_info.toString());
            }
        });


        // 사령관 이름으로 정보 검색 : 알렉산더 냥
        PlayWithCat commanderByName = ((l, q) -> {
            if (q == null) {
                return;
            }

            q = q.trim();

            Log.d(TAG, "commanderByName Activated");


            RealmResults<Commander> commanders = realm.where(Commander.class)
                    .contains(Commander.FIELD_NAME_WITHOUT_BLANK, q.replaceAll(REGEX_SPACE, ""))
                    .or().equalTo(Commander.FIELD_NAME, q).findAll();

            if (commanders.size() == 0) {
                return;
            }


            for (Commander commander : commanders) {
                StringBuilder commander_info = new StringBuilder();

                commander_info
                        .append("[사령관] ")
                        .append(commander.getString(Commander.FIELD_NAME)).append("\r\n");
                commander_info.append(commander.getString(Source.FIELD_NAME_ENG)).append(". ")
                        .append(commander.getString(Commander.FIELD_NICKNAME_ENG)).append("\r\n");
                commander_info.append("희귀도: ").append(commander.getString(Commander.FIELD_RARITY)).append("\r\n");
                commander_info.append("별명: ").append(commander.getString(Commander.FIELD_NICKNAME))
                        .append("\r\n");

                String civil = commander.getString(Commander.FIELD_CIVIL);
                if (civil != null) {
                    commander_info.append("문명: ").append(civil).append("\r\n");
                }

                commander_info
                        .append(commander.getString(Commander.FIELD_SPECS))
                        .append(commander.getString(Commander.FIELD_SKILLS));

                String[] gain_split = commander.getString(Commander.FIELD_GAIN).split(",");

                for (int i = 0; i < gain_split.length; i++) {
                    gain_split[i] = gain_split[i].trim();
                }
                commander_info.append("획득: ").append(TextUtils.join(", ", gain_split));
                int gainDays = commander.getInt(Commander.FIELD_GAIN_DAYS);
                if (gainDays > 0) {
                    commander_info.append("(").append(gainDays).append("일)");
                }

                int availableDay = commander.getInt(Commander.FIELD_AVAILABLE_DAYS);
                if (availableDay > 0) {
                    commander_info.append("\r\n사용 가능: ").append(availableDay).append("일");
                }

                String commanderInfo = commander_info.toString();
                //Log.d(TAG, commanderInfo );

                l.add(commanderInfo);
            }


        });

        PlayWithCat skillByName = ((l, q) -> {

            if (q == null) {
                return;
            }

            q = q.trim();

            RealmResults<Skill> skills = (l.size() == 0) ?
                    realm.where(Skill.class).equalTo(Source.FIELD_NAME, q)
                            .or().contains(Skill.FIELD_NAME_WITHOUT_BLANK, q.replaceAll(REGEX_SPACE, "")).findAll()
                    : realm.where(Skill.class).equalTo(Source.FIELD_NAME, q).findAll();

            if (skills.size() > 0) {
                for (Skill skill : skills) {

                    StringBuilder skill_info = new StringBuilder();
                    Commander commander = realm.where(Commander.class).equalTo(Commander.FIELD_SKILLS + "." + Source.FIELD_ID, skill.getId()).findFirst();
                    if (commander != null) {
                        skill_info.append(commander.getString(Source.FIELD_NAME)).append(" 스킬\r\n");
                    }
                    skill_info.append(skill.getString(Source.FIELD_NAME))
                            .append(" (").append(skill.getString(Skill.FIELD_PROPERTY)).append(")\r\n");
                    String desc = skill.getString(Skill.FIELD_DESCRIPTION);
                    skill_info.append(desc.replace("<br>", "\r\n"));
                    l.add(skill_info.toString());
                }
            }
        });

        Command research = ((l, q, s) -> {
            RokUser user = findOrCreateUser(sendCat, forumId, realm);
            if (q == null) {
                if (s) {
                    StringBuilder userResult = new StringBuilder();
                    userResult.append(user.getName()).append(" 연구 목록");
                    for (Technology userTech : user.getTechs().sort(Technology.FIELD_LEVEL_VAL, Sort.DESCENDING)
                            .where().distinct(Technology.FIELD_CONTENT_ID).findAll().sort(Source.FIELD_ID, Sort.DESCENDING)) {
                        userResult.append("\r\n - ").append(userTech.getContent().getString(Source.FIELD_NAME))
                                .append(" Lv.").append(userTech.getInt(Technology.FIELD_LEVEL_VAL));
                    }
                    l.add(userResult.toString());
                } else {
                    l.add("[기술이름] [레벨] 연구 완료냥 << 이렇게 입력하라옹");
                }
                return;
            }


            TechContent content;
            String fact = q.replaceAll(REGEX_DIGITS, "").trim();


            if ((content = findExactTechContent(fact, realm)) != null) {
                String figure = q.replaceAll(REGEX_EXCEPT_DIGITS, "").trim();
                Integer figureInt = figure.isEmpty() ? null : NumberUtils.toInt(figure);

                RealmResults<Technology> techs = user.getTechs().where().equalTo(Technology.FIELD_CONTENT_ID, content.getId()).findAll().sort(Technology.FIELD_LEVEL_VAL, Sort.DESCENDING);


                int figureVal;
                if (figureInt == null) {
                    if (techs.size() == 0) {
                        figureVal = 1;
                    } else {
                        Technology highest = techs.first();
                        if (highest != null) {
                            figureVal = highest.getInt(Technology.FIELD_LEVEL_VAL) + 1;
                        } else {
                            figureVal = 1;
                        }
                    }
                } else {
                    figureVal = figureInt;
                }

                Technology tech = realm.where(Technology.class).equalTo(Technology.FIELD_CONTENT_ID, content.getId()).and()
                        .equalTo(Technology.FIELD_LEVEL_VAL, figureVal).findFirst();

                String contentName = content.getString(Source.FIELD_NAME);
                realm.beginTransaction();
                if (tech == null && figureInt == null) {
                    l.add(contentName + ": 이미 최고 레벨 입니다.");
                } else if (tech == null) {
                    l.add(contentName + ": 레벨 입력 오류");
                } else if (!user.addTech(tech)) {
                    l.add(contentName + " Lv." + figureVal + ": 이미 완료된 연구 입니다.");
                } else {
                    StringBuilder buildResult = new StringBuilder();
                    StringBuilder buildList = new StringBuilder();
                    int bFood = 0, bWood = 0, bStone = 0, bGold = 0, bSeconds = 0, preBuildCount = 0;
                    for (Building building : tech.getPreBuildings().sort(Source.FIELD_ID, Sort.DESCENDING)) {
                        if (user.addBuilding(building)) {
                            buildList.append("\r\n - ").append(building.getContent().getString(Source.FIELD_NAME))
                                    .append("Lv.").append(building.getInt(Building.FIELD_LEVEL_VAL));
                            preBuildCount++;
                            bFood += building.getInt(Building.FIELD_COST_FOOD);
                            bWood += building.getInt(Building.FIELD_COST_WOOD);
                            bStone += building.getInt(Building.FIELD_COST_STONE);
                            bGold += building.getInt(Building.FIELD_COST_GOLD);
                            bSeconds += building.getInt(Building.FIELD_SECONDS);
                        }
                    }

                    StringBuilder researchResult = new StringBuilder();
                    StringBuilder researchList = new StringBuilder();
                    int rFood = tech.getInt(Technology.FIELD_COST_FOOD), rWood = tech.getInt(Technology.FIELD_COST_WOOD), rStone = tech.getInt(Technology.FIELD_COST_STONE), rGold = tech.getInt(Technology.FIELD_COST_GOLD), rSeconds = tech.getInt(Technology.FIELD_SECONDS);
                    String techName = tech.getContent().getString(Source.FIELD_NAME);
                    int techLevel = tech.getInt(Technology.FIELD_LEVEL_VAL);
                    int preTechsCount = 0;
                    for (Technology preTech : tech.getPreTechs().sort(Source.FIELD_ID, Sort.DESCENDING)) {
                        if (user.addTech(preTech)) {
                            preTechsCount++;
                            researchList.append("\r\n - ").append(preTech.getContent().getString(Source.FIELD_NAME)).append(" Lv.")
                                    .append(preTech.getInt(Technology.FIELD_LEVEL_VAL));
                            rFood += preTech.getInt(Technology.FIELD_COST_FOOD);
                            rWood += preTech.getInt(Technology.FIELD_COST_WOOD);
                            rStone += preTech.getInt(Technology.FIELD_COST_STONE);
                            rGold += preTech.getInt(Technology.FIELD_COST_GOLD);
                            rSeconds += preTech.getInt(Technology.FIELD_SECONDS);
                            for (Building preBuilding : preTech.getPreBuildings().sort(Source.FIELD_ID, Sort.DESCENDING)) {
                                if (user.addBuilding(preBuilding)) {
                                    buildList.append("\r\n - ").append(preBuilding.getContent().getString(Source.FIELD_NAME))
                                            .append("Lv.").append(preBuilding.getInt(Building.FIELD_LEVEL_VAL));
                                    preBuildCount++;
                                    bFood += preBuilding.getInt(Building.FIELD_COST_FOOD);
                                    bWood += preBuilding.getInt(Building.FIELD_COST_WOOD);
                                    bStone += preBuilding.getInt(Building.FIELD_COST_STONE);
                                    bGold += preBuilding.getInt(Building.FIELD_COST_GOLD);
                                    bSeconds += preBuilding.getInt(Building.FIELD_SECONDS);
                                }
                            }
                        }
                    }
                    researchResult.append(techName).append("Lv.").append(techLevel);

                    if (preTechsCount > 0) {
                        researchResult.append("& ").append(preTechsCount).append("개 선행 연구");
                    } else {
                        researchResult.append("연구");
                    }
                    researchResult.append(s ? "" : " 완료");
                    if (rFood > 0)
                        researchResult.append("\r\nTOTAL 식량").append(RokCalcUtils.quantityToString(rFood));
                    if (rWood > 0)
                        researchResult.append("\r\nTOTAL 목재").append(RokCalcUtils.quantityToString(rWood));
                    if (rStone > 0)
                        researchResult.append("\r\nTOTAL 석재").append(RokCalcUtils.quantityToString(rStone));
                    if (rGold > 0)
                        researchResult.append("\r\nTOTAL 금화").append(RokCalcUtils.quantityToString(rGold));
                    if (rSeconds > 0)
                        researchResult.append("\r\nTOTAL 시간 ").append(RokCalcUtils.secondsToString(rSeconds));
                    if (preTechsCount > 0) {
                        researchResult.append("\r\n-----------------\r\n*선행 연구 목록").append(researchList);
                    }

                    l.add(researchResult.toString());

                    if (preBuildCount > 0) {
                        buildResult.append(preBuildCount).append("개 선행 건설");
                        buildResult.append(s ? "" : " 완료");
                        if (bFood > 0)
                            buildResult.append("\r\nTOTAL 식량: ").append(RokCalcUtils.quantityToString(bFood));
                        if (bWood > 0)
                            buildResult.append("\r\nTOTAL 목재: ").append(RokCalcUtils.quantityToString(bWood));
                        if (bStone > 0)
                            buildResult.append("\r\nTOTAL 석재: ").append(RokCalcUtils.quantityToString(bStone));
                        if (bGold > 0)
                            buildResult.append("\r\nTOTAL 금화: ").append(RokCalcUtils.quantityToString(bGold));
                        if (bSeconds > 0)
                            buildResult.append("\r\nTOTAL 시간 ").append(RokCalcUtils.secondsToString(bSeconds));
                        buildResult.append("\r\n-----------------\r\n*선행 건설 목록").append(buildList);
                        l.add(buildResult.toString());
                    }

                } // if tech added

                if (s) realm.cancelTransaction();
                else realm.commitTransaction();
            } // if content exist
            else {
                l.add("기술 이름을 정확하게 입력 하세요");
            }


        });

        PlayWithCat findItem = (l, q) -> {
            if (q == null) {
                return;
            }

            q = q.trim();

            String qWithoutBlank = q.replaceAll("\\s+", "");

            RealmResults<Item> items = realm.where(Item.class).contains(Item.FIELD_NAME_WITHOUT_BLANK, qWithoutBlank).findAll();
            List<String> qList = new ArrayList<>(Arrays.asList(q.split("\\s+")));
            if (items.size() == 0) {
                RealmQuery<Item> itemRealmQuery = realm.where(Item.class).alwaysFalse();
                for (String itemName : qList) {
                    itemRealmQuery.or().equalTo(Item.FIELD_NAME_WITHOUT_BLANK, itemName);
                }
                items = itemRealmQuery.findAll();
            }

            if (items.size() == 0) {
                ItemCategory category = realm.where(ItemCategory.class).equalTo(Source.FIELD_NAME, qList.get(qList.size() - 1)).findFirst();
                if (category != null) {
                    qList.remove(qList.size() - 1);
                }

                if (qList.size() > 0) {
                    List<Integer> bases = new ArrayList<>();
                    List<Attribute[]> attrList = new ArrayList<>();
                    for (String attrName : qList) {
                        Attribute[] attrs = realm.where(Attribute.class).contains(Attribute.FIELD_NAME_WITHOUT_BLANK, attrName).findAll().toArray(new Attribute[0]);
                        if (attrs.length > 0) {
                            bases.add(attrs.length);
                            attrList.add(attrs);
                        }
                    }

                    if (bases.size() > 0) {
                        MultiBaseNotation mbn = new MultiBaseNotation(bases);
                        List<int[]> iCombs = mbn.getPositiveBaseDigitsCombination(99999);
                        for (int[] indexes : iCombs) {
                            Integer[] attrIds = new Integer[bases.size()];
                            String[] attrNames = new String[bases.size()];
                            for (int i = 0; i < indexes.length; i++) {
                                Attribute attr = attrList.get(i)[indexes[i]];
                                attrIds[i] = attr.getId();
                                attrNames[i] = attr.getString(Source.FIELD_NAME);
                            }
                            RealmQuery<Item> itemRealmQuery = realm.where(Item.class).alwaysTrue();
                            if (category != null) {
                                itemRealmQuery.and().equalTo(Item.FIELD_CATEGORY_ID, category.getId());
                            }

                            for (int attrId : attrIds) {
                                itemRealmQuery.and().equalTo(Item.FIELD_ATTRS + "." + Source.FIELD_ID, attrId);
                            }
                            RealmResults<Item> itemsByAttr = itemRealmQuery.findAll();
                            StringBuilder itemByAttrBuilder = new StringBuilder();
                            itemByAttrBuilder.append("아이템 속성 검색: ").append(itemsByAttr.size()).append("개\r\n");
                            for (String attrName : attrNames) {
                                itemByAttrBuilder.append("*").append(attrName).append("\r\n");
                            }
                            if (category != null) {
                                itemByAttrBuilder.append("*").append(category.getString(Source.FIELD_NAME)).append("\r\n");
                            }
                            itemByAttrBuilder.append("-----------------");

                            for (Item itemByAttr : itemsByAttr.sort(Item.FIELD_RARITY_ID)) {
                                itemByAttrBuilder.append("\r\n")
                                        .append(itemByAttr.getString(Item.FIELD_RARITY))
                                        .append(" - ")
                                        .append(itemByAttr.getString(Source.FIELD_NAME));
                                if (attrIds.length > 0) {
                                    itemByAttrBuilder.append(": ");
                                    List<String> valList = new ArrayList<>();
                                    for (int attrId : attrIds) {
                                        valList.addAll(Arrays.asList(itemByAttr.getValsStrOfAttr(attrId)));
                                    }
                                    itemByAttrBuilder.append(TextUtils.join(", ", valList));
                                }

                            }

                            if (itemsByAttr.size() > 0) {
                                l.add(itemByAttrBuilder.toString());
                            }

                        }
                    }


                }
                return;
            }

            Map<ItemSet, Integer> setMap = new HashMap<>();
            Map<ItemCategory, Item> equippedMap = new HashMap<>();
            Map<Attribute, Double> attrSumMap = new HashMap<>();
            Map<Attribute, Double> rAttrSumMap = new HashMap<>();

            boolean overlappedEquipment = false;

            for (Item item : items) {
                StringBuilder itemInfoBuilder = new StringBuilder();
                Rarity rarity = item.getRarity();
                ItemCategory cate = item.getCategory();
                if (equippedMap.containsKey(cate)) {
                    overlappedEquipment = true;
                }
                equippedMap.put(cate, item);

                itemInfoBuilder.append("[")
                        .append(cate != null ? cate.getString(Source.FIELD_NAME) : "아이템")
                        .append("] ").append(item.getString(Source.FIELD_NAME));
                String nameEng = item.getString(Source.FIELD_NAME_ENG);
                if (nameEng != null) {
                    itemInfoBuilder.append("\r\n").append(nameEng);
                }

                ItemSet itemSet = item.getItemSet();
                if (itemSet != null) {
                    itemInfoBuilder.append("\r\n").append(itemSet.getString(Source.FIELD_NAME));
                    int setCount;
                    if (setMap.containsKey(itemSet)) {
                        Integer currCount = setMap.get(itemSet);
                        setCount = (currCount == null ? 0 : currCount) + 1;
                    } else {
                        setCount = 1;
                    }
                    setMap.put(itemSet, setCount);
                }

                itemInfoBuilder.append("\r\n희귀도: ").append(rarity != null ? rarity.getString(Source.FIELD_NAME) : "??");


                RealmList<ItemMaterial> materials = item.getMaterials();
                RealmList<RealmInteger> materialCounts = item.getMaterialCounts();

                int secondsSum = 0;
                for (int i = 0, number = 1; i < materials.size(); i++) {
                    ItemMaterial material = materials.get(i);
                    if (material != null) {
                        itemInfoBuilder.append("\r\n재료").append(materials.size() == 1 ? "" : (number++)).append(": ");
                        RealmInteger countObj = materialCounts.get(i);
                        itemInfoBuilder.append(material.getString(Source.FIELD_NAME)).append(countObj != null ? " " + countObj.getValue() + "개" : "");
                        if (countObj != null) {
                            secondsSum += material.getInt(ItemMaterial.FIELD_SECONDS) * countObj.getValue();
                        }
                    }
                }

                if (secondsSum > 0) {
                    itemInfoBuilder.append("\r\n생산(재료 보급): ")
                            .append(RokCalcUtils.secondsToString(secondsSum)).append("(")
                            .append(RokCalcUtils.secondsToString(secondsSum / 2)).append(")");
                }


                int gold = item.getInt(Item.FIELD_GOLD);
                itemInfoBuilder.append("\r\n금화: ").append(RokCalcUtils.quantityToString(gold));

                RealmList<Attribute> attrs = item.getAttrs();
                RealmList<RealmDouble> attrVals = item.getAttrVals();

                Object[] multiVals = null;
                if (attrs.size() == 1 && attrVals.size() > 1) {
                    multiVals = new Object[attrVals.size()];
                    for (int i = 0; i < multiVals.length; i++) {
                        RealmDouble valObj = attrVals.get(i);
                        if (valObj != null) {
                            multiVals[i] = valObj.getValue();
                        }
                    }
                }

                for (int i = 0, number = 1; i < attrs.size(); i++) {

                    Attribute attr = attrs.get(i);
                    if (attr != null) {
                        itemInfoBuilder.append("\r\n속성").append(attrs.size() == 1 ? "" : (number++)).append(": ");
                        if (multiVals == null) {
                            RealmDouble valObj = attrVals.get(i);
                            if (valObj != null) {
                                double val = valObj.getValue();
                                itemInfoBuilder.append(attr.getFormWithValue(val));
                                Double curValSum = attrSumMap.get(attr);
                                attrSumMap.put(attr, (curValSum == null ? 0d : curValSum) + val);
                                double rVal = Math.ceil(val * 0.6d) / 2.0d;
                                itemInfoBuilder.append(" (+").append(rVal).append(")");
                                Double rValSum = rAttrSumMap.get(attr);
                                rAttrSumMap.put(attr, (rValSum == null ? 0d : rValSum) + rVal);
                            } else {
                                itemInfoBuilder.append(attr.getString(Attribute.FIELD_NAME));
                            }
                        } else {
                            itemInfoBuilder.append(attr.getFormWithValue(multiVals));
                        }

                    }
                }

                String desc = item.getString(Item.FIELD_DESCRIPTION);
                if (desc != null) {
                    itemInfoBuilder.append("\r\n").append(desc);
                }

                if (items.size() < 3) {
                    l.add(itemInfoBuilder.toString());
                }
            } // for Items


            for (ItemSet itemSet : setMap.keySet()) {
                Integer count = setMap.get(itemSet);
                if (count != null && count > 0) {
                    RealmList<Attribute> attrs = itemSet.getAttrs();
                    RealmList<RealmDouble> vals = itemSet.getVals();
                    RealmList<RealmInteger> counts = itemSet.getCounts();
                    StringBuilder itemSetBuilder = new StringBuilder();
                    itemSetBuilder.append(itemSet.getString(Source.FIELD_NAME)).append("\r\n").append(itemSet.getString(Source.FIELD_NAME_ENG));
                    for (int i = 0; i < counts.size(); i++) {
                        RealmInteger setCount = counts.get(i);
                        Attribute attr = attrs.get(i);
                        RealmDouble val = vals.get(i);
                        if (setCount != null && attr != null && val != null) {
                            itemSetBuilder.append("\r\n").append(setCount.getValue()).append("개: ").append(attr.getFormWithValue(val.getValue()));

                            if (count >= setCount.getValue()) {
                                itemSetBuilder.append(" [o]");
                                Double curVal = attrSumMap.get(attr);
                                attrSumMap.put(attr, (curVal == null ? 0d : curVal) + val.getValue());
                            } else {
                                itemSetBuilder.append(" [x]");
                            }
                        }
                    }

                    l.add(itemSetBuilder.toString());
                }
            }

            if (!overlappedEquipment && items.size() > 1) {
                StringBuilder equipBuilder = new StringBuilder();
                equipBuilder.append("*아이템 착용");
                for (ItemCategory cate : equippedMap.keySet()) {
                    Item eqItem = equippedMap.get(cate);
                    if (eqItem != null) {
                        equipBuilder.append("\r\n - ").append(cate.getString(Source.FIELD_NAME)).append(": ")
                                .append(eqItem.getString(Source.FIELD_NAME));
                    }
                }
                if (attrSumMap.size() > 0) {
                    equipBuilder.append("\r\n-----------------\r\n*속성 총합").append(setMap.size() > 0 ? "(세트 포함)": "");
                    for (Attribute attr : attrSumMap.keySet()) {
                        Double val = attrSumMap.get(attr);
                        Double rVal = rAttrSumMap.get(attr);
                        String valRange = val == null ? null : (val + (rVal != null ? ("~" + (val + rVal)) : ""));
                        equipBuilder.append("\r\n - ").append(attr.getFormWithValue(valRange));
                    }
                }

                l.add(equipBuilder.toString());
            }


        };


        List<String> result = new ArrayList<>();

        switch (cmd) {
            case CMD_WIKI:
                searchWiki.search(result, req, false);
                break;
            case CMD_ITEM:
                findItem.play(result, req);
                break;
            case CMD_SPEC:
                result.add("사령관 특성: 정보 수집 중");
                break;
            case CMD_SKILL:
                searchSkill.search(result, req, false);
                break;
            case CMD_CIVIL:
                searchCivil.search(result, req, false);
                break;
            case CMD_COMMANDER:
                searchCommanders.search(result, req, false);
                break;
            case CMD_RESEARCH:
                research.search(result, req, true);
                break;
            case CMD_BUILD:
                result.add("건물 건설: 준비 중");
                break;
            case CMD_COMMIT:
                RealmString prfxReq = new RealmString(req);
                CMD_ENUM prfxCmd = findCMD(prfxReq);
                req = prfxReq.toString();
                switch (prfxCmd) {
                    case CMD_RESEARCH:
                        research.search(result, req, false);
                        break;
                    case CMD_BUILD:
                        result.add("건물 건설: 준비 중");
                        break;
                    default:
                        result.add("ex1)병영 25 건설 완료\r\nex2)고대로마방패 연구 완료");
                }
                break;
            case CMD_TECH:
                searchTech.search(result, req, false);
                break;
            case CMD_CALC:
                calc.play(result, req);
                break;
            default:
                commanderByName.play(result, req);
                civilByName.play(result, req);
                skillByName.play(result, req);
                techByName.play(result, req);
                findItem.play(result, req);
        }

        return result;

    }

    private interface Command {
        void search(List<String> list, String req, Boolean simulationMode);
    }

    private interface PlayWithCat {
        void play(List<String> list, String req);
    }


    private static RokUser findOrCreateUser(String sendCat, long forumId, Realm realm) {

        RokUser user = realm.where(RokUser.class).equalTo(RokUser.FIELD_FORUM_ID, forumId)
                .and().equalTo(RokUser.FIELD_SENDCAT, sendCat)
                .and().equalTo(RokUser.FIELD_NAME, sendCat).findFirst();

        if (user == null) {
            realm.beginTransaction();
            user = new RokUser(sendCat, sendCat, forumId);
            realm.copyToRealm(user);
            realm.commitTransaction();
        }

        return user;
    }

    private static TechContent findExactTechContent(String name, Realm realm) {
        RealmResults<TechContent> contents = realm.where(TechContent.class).equalTo(TechContent.FIELD_NAME, name).or()
                .contains(TechContent.FIELD_NAME_WITHOUT_BLANK, name).findAll();
        if (contents.size() > 1) {
            contents = contents.where().equalTo(TechContent.FIELD_NAME, name).or().equalTo(TechContent.FIELD_NAME_WITHOUT_BLANK, name).findAll();
        }

        if (contents.size() == 1) {
            return contents.first();
        }
        return null;
    }


}
