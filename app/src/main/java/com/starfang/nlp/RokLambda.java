package com.starfang.nlp;

import android.text.TextUtils;
import android.util.Log;

import com.starfang.realm.source.Source;
import com.starfang.realm.source.rok.Civilizations;
import com.starfang.realm.source.rok.Commanders;
import com.starfang.realm.source.rok.Skills;

import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;


class RokLambda {

    private static final String TAG = "FANG_MOD_CAT";

    private enum CMD_CERTAIN_ENUM {CMD_DESC, CMD_COMMANDER, CMD_SKILL, CMD_SPEC, CMD_CIVIL, CMD_ITEM, CMD_CALC, CMD_WIKI, CMD_DEFAULT}

    private static final String[] CMD_CERTAIN = {"설명", "사령관", "스킬", "특성", "문명", "아이템", "계산", "위키", "냥"};

    private static final String ROK_WIKI = "http://rok.wiki/";
    private static final String ROK_WIKI_HERO_DIR = "bbs/board.php?bo_table=hero&wr_id=";

    private static final String REGEX_SPACE = "\\s+";
    private static final String REGEX_EXCEPT_DIGITS = "[^0-9]";
    private static final String REGEX_DIGITS = "[0-9]";

    public static List<String> processReq(String req, Realm realm) {

        req = req.trim();

        CMD_CERTAIN_ENUM certainCMD = CMD_CERTAIN_ENUM.CMD_DEFAULT;
        for (CMD_CERTAIN_ENUM certain : CMD_CERTAIN_ENUM.values()) {
            try {
                int certainIndex = certain.ordinal();
                if (certainIndex < CMD_CERTAIN.length) {
                    String probKey = CMD_CERTAIN[certainIndex];
                    int reqLength = req.length();
                    int keyLength = probKey.length();

                    if (reqLength >= keyLength) {

                        try {
                            if (req.substring(reqLength - keyLength).equals(probKey)) {
                                certainCMD = certain;
                                req = (reqLength == keyLength) ? null : req.substring(0, reqLength - keyLength);
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

        SearchWithCMD searchSkill = ((l, q, w) -> {
            if (q == null) {
                l.add("\"사령관 이름\"   \"스킬 번호\"   냥 << 이렇게 입력하라옹");
            } else {
                q = q.trim();
                String number = q.replaceAll(REGEX_EXCEPT_DIGITS, "");
                String name = q.replaceAll(REGEX_DIGITS, "");

                RealmResults<Commanders> commanders = realm.where(Commanders.class)
                        .contains(Commanders.FIELD_NAME_WITHOUT_BLANK, name.replaceAll(REGEX_SPACE, ""))
                        .or().equalTo(Commanders.FIELD_NAME, name).findAll();

                int numberVal = -1;
                if (!TextUtils.isEmpty(number)) {
                    numberVal = Integer.parseInt(number);
                    if (numberVal > 0 && numberVal < 6) {
                        numberVal -= 1;
                    } else {
                        numberVal = -1;
                    }
                }


                for (Commanders commander : commanders) {
                    RealmList<Skills> skills = commander.getSkills();
                    if (skills != null) {
                        for (int i = 0; i < skills.size(); i++) {
                            Skills skill = skills.get(i);
                            if (skill != null && (numberVal == -1 || numberVal == i)) {
                                String skillBuilder = commander.getString(Source.FIELD_NAME) + " > " + (i + 1) + "스킬\r\n" +
                                        skill.getString(Source.FIELD_NAME) + " (" + skill.getString(Skills.FIELD_PROPERTY) + ")\r\n" +
                                        skill.getString(Skills.FIELD_DESCRIPTION).replace("<br>", "\r\n");
                                l.add(skillBuilder);
                            }
                        }
                    }
                }


            }
        });

        SearchWithCMD searchCivil = ((l, q, w) -> {
            RealmResults<Civilizations> civilizations;
            String header = "";
            if (q == null) {
                civilizations = realm.where(Civilizations.class).findAll().sort(Source.FIELD_NAME);
                header = "총 " + civilizations.size() + "개 문명\r\n-----------------";
            } else {
                q = q.trim();
                civilizations = realm.where(Civilizations.class).contains(Civilizations.FIELD_BONUS1, q)
                        .or().contains(Civilizations.FIELD_BONUS2, q)
                        .or().contains(Civilizations.FIELD_BONUS3, q)
                        .findAll().sort(Source.FIELD_NAME);
                if (civilizations.size() > 0) {
                    header = q + " 특성 보유 문명: " + civilizations.size() + "개\r\n-----------------";
                }
            }
            if (civilizations.size() > 0) {
                StringBuilder lambdaResult = new StringBuilder();
                lambdaResult.append(header);
                for (Civilizations civilization : civilizations) {
                    lambdaResult.append("\r\n").append(civilization.getString(Source.FIELD_NAME));
                }
                l.add(lambdaResult.toString());
            }
        });

        SearchWithCMD searchWiki = ((l, q, w) -> {
            if (q == null) {
                l.add(ROK_WIKI);
            } else {
                q = q.trim();
                RealmResults<Commanders> commanders = realm.where(Commanders.class)
                        .contains(Commanders.FIELD_NAME_WITHOUT_BLANK, q.replaceAll(REGEX_SPACE, ""))
                        .or().equalTo(Commanders.FIELD_NAME, q).findAll();

                if (commanders.size() == 0) {
                    return;
                }

                for( Commanders commander : commanders ) {
                    String url = ROK_WIKI + ROK_WIKI_HERO_DIR + commander.getId();
                    l.add(commander.getString(Source.FIELD_NAME) + ": " + url);
                }

            }
        });

        SearchWithCMD searchCommanders = ((l, q, w) -> {
            String header = "";
            RealmResults<Commanders> commanders;
            if (q == null) {
                commanders = realm.where(Commanders.class).findAll().sort(Commanders.FIELD_RARITY);
            } else {
                q = q.trim();
                commanders = realm.where(Commanders.class).equalTo(Commanders.FIELD_RARITY, q).findAll();
                if (commanders.size() > 0) {
                    header = "희귀도 \"" + q + "\"";
                } else {
                    commanders = realm.where(Commanders.class).contains(Commanders.FIELD_SPECS + "." + Source.FIELD_NAME, q).findAll();
                    if (commanders.size() > 0) {
                        header = "\"" + q + "\" 특성 보유";
                    }
                }
            }

            if (commanders.size() > 0) {
                header += " 사령관: " + commanders.size() + "명\r\n-----------------";
                StringBuilder lambdaResult = new StringBuilder();
                lambdaResult.append(header);
                for (Commanders commander : commanders) {
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

            RealmResults<Civilizations> civilizations = realm.where(Civilizations.class)
                    .equalTo(Civilizations.FIELD_NAME, q).findAll();

            for (Civilizations civilization : civilizations) {
                StringBuilder civil_info = new StringBuilder();

                civil_info
                        .append(civilization.getString(Commanders.FIELD_NAME)).append("\r\n")
                        .append("\"").append(civilization.getString(Civilizations.FIELD_COMMENT)).append("\"").append("\r\n\r\n");

                String[] bonuses = {
                        civilization.getString(Civilizations.FIELD_BONUS1),
                        civilization.getString(Civilizations.FIELD_BONUS2),
                        civilization.getString(Civilizations.FIELD_BONUS3)
                };

                Commanders commander = realm.where(Commanders.class).equalTo(Source.FIELD_ID, civilization.getInt(Civilizations.FIELD_COMMANDER_ID)).findFirst();
                if (commander != null) {
                    civil_info.append("초기 사령관: ").append(commander.getString(Source.FIELD_NAME)).append("\r\n");
                }

                for (int i = 0, number = 1; i < bonuses.length; i++) {
                    if (bonuses[i] != null) {
                        civil_info.append("보너스").append(number).append(": ")
                                .append(bonuses[i]).append("\r\n");
                        number++;
                    }
                }

                civil_info.append("특수 유닛: ").append(civilization.getString(Civilizations.FIELD_SPECIAL_UNIT));
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


            RealmResults<Commanders> commanders = realm.where(Commanders.class)
                    .contains(Commanders.FIELD_NAME_WITHOUT_BLANK, q.replaceAll(REGEX_SPACE, ""))
                    .or().equalTo(Commanders.FIELD_NAME, q).findAll();

            if (commanders.size() == 0) {
                return;
            }


            for (Commanders commander : commanders) {
                StringBuilder commander_info = new StringBuilder();

                commander_info
                        .append("[").append(commander.getString(Commanders.FIELD_RARITY)).append("] ")
                        .append(commander.getString(Commanders.FIELD_NAME)).append("\r\n");
                commander_info.append("별명: ").append(commander.getString(Commanders.FIELD_NICKNAME)).append("\r\n");

                String civil = commander.getString(Commanders.FIELD_CIVIL);
                if (civil != null) {
                    commander_info.append("문명: ").append(civil).append("\r\n");
                }

                commander_info
                        .append(commander.getString(Commanders.FIELD_SPECS))
                        .append(commander.getString(Commanders.FIELD_SKILLS));

                String[] gain_split = commander.getString(Commanders.FIELD_GAIN).split(",");

                for (int i = 0; i < gain_split.length; i++) {
                    gain_split[i] = gain_split[i].trim();
                }
                commander_info.append("획득 : ").append(TextUtils.join(", ", gain_split));
                int gainDays = commander.getInt(Commanders.FIELD_GAIN_DAYS);
                if (gainDays > 0) {
                    commander_info.append("(").append(gainDays).append("일)");
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

            RealmResults<Skills> skills = (l.size() == 0) ?
                    realm.where(Skills.class).equalTo(Source.FIELD_NAME, q)
                            .or().contains(Skills.FIELD_NAME_WITHOUT_BLANK, q.replaceAll(REGEX_SPACE, "")).findAll()
                    : realm.where(Skills.class).equalTo(Source.FIELD_NAME, q).findAll();

            if (skills.size() > 0) {
                for (Skills skill : skills) {

                    StringBuilder skill_info = new StringBuilder();
                    Commanders commander = realm.where(Commanders.class).equalTo(Commanders.FIELD_SKILLS + "." + Source.FIELD_ID, skill.getId()).findFirst();
                    if (commander != null) {
                        skill_info.append(commander.getString(Source.FIELD_NAME)).append(" 스킬\r\n");
                    }
                    skill_info.append(skill.getString(Source.FIELD_NAME))
                            .append(" (").append(skill.getString(Skills.FIELD_PROPERTY)).append(")\r\n");
                    String desc = skill.getString(Skills.FIELD_DESCRIPTION);
                    skill_info.append(desc.replace("<br>", "\r\n"));
                    l.add(skill_info.toString());
                }
            }
        });


        List<String> result = new ArrayList<>();

        switch (certainCMD) {
            case CMD_WIKI:
                searchWiki.search(result, req, true);
                break;
            case CMD_ITEM:
                result.add("아이템: 정보 수집 중");
                break;
            case CMD_SPEC:
                result.add("사령관 특성: 정보 수집 중");
                break;
            case CMD_SKILL:
                searchSkill.search(result, req, true);
                break;
            case CMD_CIVIL:
                searchCivil.search(result, req, true);
                break;
            case CMD_COMMANDER:
                searchCommanders.search(result, req, true);
                break;
            case CMD_CALC:
                calc.play(result, req);
                break;
            default:
                commanderByName.play(result, req);
                civilByName.play(result, req);
                skillByName.play(result, req);

        }

        return result;

    }

    private interface SearchWithCMD {
        void search(List<String> list, String req, Boolean withCMD);
    }

    private interface PlayWithCat {
        void play(List<String> list, String req);
    }


}
