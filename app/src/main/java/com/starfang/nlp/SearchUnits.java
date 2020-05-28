package com.starfang.nlp;

import android.text.TextUtils;

import com.starfang.realm.source.Banners;
import com.starfang.realm.source.Friendships;
import com.starfang.realm.source.PassiveList;
import com.starfang.realm.source.Passives;
import com.starfang.realm.source.Personality;
import com.starfang.realm.source.PrefectSkills;
import com.starfang.realm.source.UnitTypes;
import com.starfang.realm.source.Units;
import com.starfang.realm.source.WarlordSkills;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class SearchUnits {

    private static final String COST_ENG = "COST";
    private static final String COST_KOR = "코스트";
    private static final String COST_KOR_SIMPLE = "코";
    private static final String VAL = "수치";
    private static final String PRICE = "값";
    private static final String STR = "무력";
    private static final String STR_SIMPLE = "무";
    private static final String INTEL = "지력";
    private static final String INTEL_SIMPLE = "지";
    private static final String CMD = "통솔";
    private static final String CMD_SIMPLE = "통";
    private static final String DEX = "민첩";
    private static final String DEX_SIMPLE = "민";
    private static final String LCK = "행운";
    private static final String LCK_SIMPLE = "행";
    private static final String PASSIVE = "효과";
    private static final String CHARACTERISTIC = "특성";
    private static final int CODE_EQUAL = 0;
    //private static final String SIGN_EQUAL = "=";
    private static final String GREATER_OR_EQUAL = "이상";
    private static final String SIGN_GREATER_OR_EQUAL = ">=";
    private static final int CODE_GREATER_OR_EQUAL = 1;
    private static final String GREATER_THAN = "초과";
    private static final String SIGN_GREATER_THAN = ">";
    private static final int CODE_GREATER_THAN = 2;
    private static final String LESS_OR_EQUAL = "이하";
    private static final String SIGN_LESS_OR_EQUAL = "<=";
    private static final int CODE_LESS_OR_EQUAL = 3;
    private static final String LESS_THAN = "미만";
    private static final String SIGN_LESS_THAN = "<";
    private static final int CODE_LESS_THAN = 4;

    private static final String COMMON_FIELD_ID = "id";

    public JSONObject interpret(String text) throws JSONException {

        final List<String> words = new ArrayList<>();
        final List<String> values = new ArrayList<>();

        Pattern reg = Pattern.compile("[\\uAC00-\\uDCAFa-zA-Z>=<]+");
        Matcher matcher = reg.matcher(text);

        while (matcher.find()) {
            String word = matcher.group();
            if (!word.equals("몰우전")) {
                words.add(word);
            }
        }
        reg = Pattern.compile("\\d+|몰우전");
        matcher = reg.matcher(text);
        while (matcher.find()) {
            String value = matcher.group();
            values.add(value);
        }

        final int[] vals = new int[values.size()];
        for (int i = 0; i < vals.length; i++)
            vals[i] = NumberUtils.toInt(values.get(i), 0);

        final int wordsSize = words.size();

        JSONObject json = new JSONObject();
        for (int i = 0, j = 0; i < wordsSize; i++) {
            String unit_field;
            String passive = null;
            switch (words.get(i).toUpperCase()) {
                case COST_ENG:
                case COST_KOR:
                case COST_KOR_SIMPLE:
                    unit_field = Units.FIELD_COST;
                    break;
                case STR:
                case STR_SIMPLE:
                    unit_field = Units.FIELD_STR;
                    break;
                case INTEL:
                case INTEL_SIMPLE:
                    unit_field = Units.FIELD_INTEL;
                    break;
                case CMD:
                case CMD_SIMPLE:
                    unit_field = Units.FIELD_CMD;
                    break;
                case DEX:
                case DEX_SIMPLE:
                    unit_field = Units.FIELD_DEX;
                    break;
                case LCK:
                case LCK_SIMPLE:
                    unit_field = Units.FIELD_LCK;
                    break;
                case PASSIVE:
                case CHARACTERISTIC:
                    if (i + 1 < wordsSize) {
                        passive = words.get(i + 1);
                        unit_field = Units.FIELD_PASSIVE_LISTS + "." + PassiveList.FIELD_UNIT_LEVEL;
                    } else {
                        unit_field = null;
                    }
                    break;
                case VAL:
                case PRICE:
                    if (i > 0) {
                        passive = words.get(i - 1);
                        unit_field = Units.FIELD_PASSIVE_LISTS + "." + PassiveList.FIELD_VAL;
                    } else {
                        unit_field = null;
                    }
                    break;
                default:
                    unit_field = null;
            }


            if (unit_field != null) {
                int inequalityCode;
                if (i + 1 < wordsSize) {
                    switch (words.get(i + 1)) {
                        case GREATER_OR_EQUAL:
                        case SIGN_GREATER_OR_EQUAL:
                            inequalityCode = CODE_GREATER_OR_EQUAL;
                            break;
                        case GREATER_THAN:
                        case SIGN_GREATER_THAN:
                            inequalityCode = CODE_GREATER_THAN;
                            break;
                        case LESS_OR_EQUAL:
                        case SIGN_LESS_OR_EQUAL:
                            inequalityCode = CODE_LESS_OR_EQUAL;
                            break;
                        case LESS_THAN:
                        case SIGN_LESS_THAN:
                            inequalityCode = CODE_LESS_THAN;
                            break;
                        default:
                            inequalityCode = CODE_EQUAL;
                    }
                } else {
                    inequalityCode = CODE_EQUAL;
                }

                JSONObject fieldDetails = new JSONObject();
                fieldDetails.put("inequality", inequalityCode);
                fieldDetails.put("val", vals[j]);

                if (passive != null) {
                    JSONObject passiveObject;
                    if (json.has("passives")) {
                        passiveObject = (JSONObject) json.get("passives");
                    } else {
                        passiveObject = new JSONObject();
                        json.put("passives", passiveObject);
                    }
                    JSONObject passiveDetails;
                    if (passiveObject.has(passive)) {
                        passiveDetails = (JSONObject) passiveObject.get(passive);
                    } else {
                        passiveDetails = new JSONObject();
                        passiveObject.put(passive, passiveDetails);
                    }
                    passiveDetails.put(unit_field, fieldDetails);

                } else {
                    JSONObject fieldObject;
                    if (json.has("fields")) {
                        fieldObject = (JSONObject) json.get("fields");
                    } else {
                        fieldObject = new JSONObject();
                        json.put("fields", fieldObject);
                    }
                    fieldObject.put(unit_field, fieldDetails);
                }

                if (inequalityCode > 0) {
                    ++i;
                }


                ++j;


            } else {
                passive = words.get(i);
                JSONObject passiveObject;
                if (json.has("passives")) {
                    passiveObject = (JSONObject) json.get("passives");
                } else {
                    passiveObject = new JSONObject();
                    json.put("passives", passiveObject);
                }

                if (!passiveObject.has(passive)) {
                    passiveObject.put(passive, new JSONObject());
                }

            }

        } // for

        return json;
    }


    public OrderedRealmCollection<Units> search(JSONObject json, Realm realm) throws JSONException {
        /*
        공범확 30 특성 피범확 수치 8 이상 일필
        {"passives":{"일필":{},"피범확":{"passiveLists.val":{"val":8,"inequality":1},"passiveLists.unitLevel":{"val":30,"inequality":0}},"공범확":{}}}
        무 >= 30 지력 < 60 20코 미만 30특성 방보조 수치 10 이상 행 < 99
        {"fields":{"val":30,"inequality":1,"cost":{"val":20,"inequality":4},"lck":{"val":99,"inequality":4},"intel":{"val":60,"inequality":4}},"passives":{"방보조":{"passiveLists.val":{"val":10,"inequality":1},"passiveLists.unitLevel":{"val":30,"inequality":0}}}}
         */
        RealmQuery<Units> query = realm.where(Units.class);

        if (json.has("fields")) {
            JSONObject fieldObject = (JSONObject) json.get("fields");
            Iterator<String> fieldKeys = fieldObject.keys();
            while (fieldKeys.hasNext()) {
                String field = fieldKeys.next();
                JSONObject fieldDetails = (JSONObject) fieldObject.get(field);
                if (fieldDetails.has("val")) {
                    int value = fieldDetails.getInt("val");
                    if (fieldDetails.has("inequality")) {
                        switchInequality((int) fieldDetails.get("inequality"), query, field, value);
                    } else {
                        query.equalTo(field, value);
                    }
                }
            }
        }

        if (json.has("passives")) {
            JSONObject passiveObject = (JSONObject) json.get("passives");
            List<Integer> baseList = new ArrayList<>();
            List<int[]> passiveList = new ArrayList<>();
            List<JSONObject> criteriaList = new ArrayList<>();
            Iterator<String> passiveKeys = passiveObject.keys();
            while (passiveKeys.hasNext()) {
                String passiveStr = passiveKeys.next();
                Banners banner;
                UnitTypes type;
                if (passiveStr.length() > 3 && (banner = realm.where(Banners.class).contains(Banners.FIELD_NAME_WITHOUT_BLANK, passiveStr).findFirst()) != null) {
                    query.equalTo(Units.FIELD_BANNER_ID, banner.getId());
                } else if (passiveStr.length() > 1 && (type = realm.where(UnitTypes.class).equalTo(UnitTypes.FIELD_NAME, passiveStr).or().equalTo(UnitTypes.FIELD_NAME2, passiveStr).findFirst()) != null) {
                    query.equalTo(Units.FIELD_TYPE_ID, type.getId());
                } else {
                    RealmResults<Passives> passives = realm.where(Passives.class).contains(Passives.FIELD_NAME2, passiveStr).or().contains(Passives.FIELD_NAME_WITHOUT_BLANK, passiveStr).findAll();
                    if (passives.size() > 0) {
                        baseList.add(passives.size());
                        int[] ids = new int[passives.size()];
                        for (int i = 0; i < ids.length; i++) {
                            Passives passive = passives.get(i);
                            ids[i] = passive == null ? 0 : passive.getId();
                        }
                        passiveList.add(ids);
                        criteriaList.add((JSONObject) passiveObject.get(passiveStr));
                    }
                    //RealmResults<WarlordSkills> warlordSkills = realm.where(WarlordSkills.class).contains()
                }
            }
            MultiBaseNotation mbn = new MultiBaseNotation(baseList);
            for (int[] comb : mbn.getPositiveBaseDigitsCombination(99999999)) {
                query.or().beginGroup();
                for (int i = 0; i < comb.length; i++) {
                    int id = (passiveList.get(i))[comb[i]];
                    JSONObject criteria = criteriaList.get(i);
                    Iterator<String> criteriaKeys = criteria.keys();
                    while (criteriaKeys.hasNext()) {
                        String field = criteriaKeys.next();
                        JSONObject criteriaDetails = (JSONObject) criteria.get(field);
                        if (criteriaDetails.has("val")) {
                            int value = criteria.getInt("val");
                            if (criteriaDetails.has("inequality")) {
                                switchInequality((int) criteriaDetails.get("inequality"), query, field, value);
                            } else {
                                query.equalTo(field, value);
                            }
                        }
                    }
                    query.equalTo(TextUtils.join(".", new String[]{Units.FIELD_PASSIVE_LISTS, PassiveList.FIELD_PASSIVE, COMMON_FIELD_ID}), id);

                }
                query.endGroup();
            }
        }


        return query.findAll().sort(Units.FIELD_COST).sort(Units.FIELD_BANNER_ID);

    }

    public List<String> toMessage(OrderedRealmCollection<Units> units, String... fields) {
        List<String> message = new LinkedList<>();
        if (units.size() == 0) {
            message.add("없다옹");
        } else if (units.size() == 1) {
            Units unit = units.first();
            if (unit != null) {
                StringBuilder builder = new StringBuilder();

                builder.append(unit.getBanner().getName()).append(" ")
                        .append(unit.getName()).append("\r\n")
                        .append(COST_ENG).append(": ")
                        .append(TextUtils.join("→", unit.getCosts())).append("\r\n")
                        .append("계보: ").append(unit.getBanner().getName()).append("\r\n");
                RealmList<Friendships> friendships = unit.getFriendshipList();
                if (friendships != null) {
                    if (friendships.size() > 1) {
                        for (int i = 0; i < friendships.size(); i++) {
                            Friendships friendship = friendships.get(i);
                            if (friendship != null)
                                builder.append("인연").append(i + 1).append(friendship.getName()).append("\r\n");
                        }
                    } else {
                        Friendships friendship = friendships.first();
                        if (friendship != null)
                            builder.append("인연: ").append(friendship.getName()).append("\r\n");
                    }
                }
                builder.append(TextUtils.join(" "
                        , new String[]{
                                STR_SIMPLE + unit.getStr(),
                                INTEL_SIMPLE + unit.getIntel(),
                                CMD_SIMPLE + unit.getCmd(),
                                DEX_SIMPLE + unit.getDex(),
                                LCK_SIMPLE + unit.getLck()
                        })).append(" (+").append(5 * (unit.getCost() + 16)).append(")\r\n");

                RealmList<PassiveList> passiveLists = unit.getPassiveLists();
                if (passiveLists != null) {
                    for (int i = 0; i < passiveLists.size(); i++) {
                        PassiveList passiveList = passiveLists.get(i);
                        if (passiveList != null) {
                            Passives passive = passiveList.getPassive();
                            String passiveName = passive.getName();
                            int val = passiveList.getVal();
                            String value;
                            if (val > 0) {
                                if (StringUtils.right(passiveName, 1).equals("%")) {
                                    passiveName = StringUtils.left(passiveName, passiveName.length() - 1);
                                    value = val + "%";
                                } else {
                                    value = String.valueOf(val);
                                }
                            }

                            builder.append("Lv").append(passiveList.getUnitLevel())
                                    .append(": ").append(passiveName)
                                    .append(" ").append(val).append("\r\n");
                        }
                    }
                }
                PrefectSkills prefectSkill = unit.getPrefectSkill();
                if (prefectSkill != null)
                    builder.append("태수: ").append(prefectSkill.getName()).append("\r\n");
                WarlordSkills warlordSkill = unit.getWarlordSkill();
                if( warlordSkill != null)
                    builder.append("군주: ").append(warlordSkill.getName()).append("\r\n");
                Personality personality = unit.getPersonality();
                if( personality != null )
                    builder.append("성격: ").append(personality.getName());
                message.add(builder.toString());
            }
        } else {

            for(Units unit: units) {

            }
        }

        return message;
    }


    private void switchInequality(int inequalityCode, RealmQuery<Units> query, String field, int value) {
        switch (inequalityCode) {
            case CODE_EQUAL:
                query.equalTo(field, value);
                break;
            case CODE_GREATER_OR_EQUAL:
                query.greaterThanOrEqualTo(field, value);
                break;
            case CODE_GREATER_THAN:
                query.greaterThan(field, value);
                break;
            case CODE_LESS_OR_EQUAL:
                query.lessThanOrEqualTo(field, value);
                break;
            case CODE_LESS_THAN:
                query.lessThan(field, value);
                break;
        }
    }
}
