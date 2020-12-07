package com.starfang.nlp;

import android.text.TextUtils;
import android.util.Log;

import com.starfang.realm.source.Source;
import com.starfang.realm.source.caocao.Banners;
import com.starfang.realm.source.caocao.Friendships;
import com.starfang.realm.source.caocao.PassiveList;
import com.starfang.realm.source.caocao.Passives;
import com.starfang.realm.source.caocao.Personality;
import com.starfang.realm.source.caocao.PrefectSkills;
import com.starfang.realm.source.caocao.UnitTypes;
import com.starfang.realm.source.caocao.Units;
import com.starfang.realm.source.caocao.WarlordSkills;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class SearchUnits {

    private static final String TAG = "FANG_SEARCH_UNIT";

    private static final String COST_ENG = "COST";
    private static final String COST_KOR = "코스트";
    private static final String COST_KOR_SIMPLE = "코";
    private static final String VAL = "수치";
    private static final String PRICE = "값";

    private static final String PASSIVE = "효과";
    private static final String CHARACTERISTIC = "특성";
    private static final String SURNAME = "씨";

    private interface StatKor {
        String STR = "무력";
        String INTEL = "지력";
        String CMD = "통솔";
        String DEX = "민첩";
        String LCK = "행운";
    }

    private interface StatKorSimple {
        String STR_SIMPLE = "무";
        String INTEL_SIMPLE = "지";
        String CMD_SIMPLE = "통";
        String DEX_SIMPLE = "민";
        String LCK_SIMPLE = "행";
    }

    private interface InequalityKor {
        String GREATER_OR_EQUAL = "이상";
        String GREATER_THAN = "초과";
        String LESS_OR_EQUAL = "이하";
        String LESS_THAN = "미만";
    }

    private interface InequalitySign {
        String SIGN_GREATER_OR_EQUAL = ">=";
        String SIGN_GREATER_THAN = ">";
        String SIGN_LESS_OR_EQUAL = "<=";
        String SIGN_LESS_THAN = "<";
    }

    private interface InequalityCode {
        int CODE_EQUAL = 0;
        int CODE_GREATER_OR_EQUAL = 1;
        int CODE_GREATER_THAN = 2;
        int CODE_LESS_OR_EQUAL = 3;
        int CODE_LESS_THAN = 4;
    }



    @NotNull
    public static JSONObject interpret(String text) throws JSONException {

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
                    vals[j] -= 10;
                    break;
                case StatKor.STR:
                case StatKorSimple.STR_SIMPLE:
                    unit_field = Units.FIELD_STR;
                    break;
                case StatKor.INTEL:
                case StatKorSimple.INTEL_SIMPLE:
                    unit_field = Units.FIELD_INTEL;
                    break;
                case StatKor.CMD:
                case StatKorSimple.CMD_SIMPLE:
                    unit_field = Units.FIELD_CMD;
                    break;
                case StatKor.DEX:
                case StatKorSimple.DEX_SIMPLE:
                    unit_field = Units.FIELD_DEX;
                    break;
                case StatKor.LCK:
                case StatKorSimple.LCK_SIMPLE:
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
                        case InequalityKor.GREATER_OR_EQUAL:
                        case InequalitySign.SIGN_GREATER_OR_EQUAL:
                            inequalityCode = InequalityCode.CODE_GREATER_OR_EQUAL;
                            break;
                        case InequalityKor.GREATER_THAN:
                        case InequalitySign.SIGN_GREATER_THAN:
                            inequalityCode = InequalityCode.CODE_GREATER_THAN;
                            break;
                        case InequalityKor.LESS_OR_EQUAL:
                        case InequalitySign.SIGN_LESS_OR_EQUAL:
                            inequalityCode = InequalityCode.CODE_LESS_OR_EQUAL;
                            break;
                        case InequalityKor.LESS_THAN:
                        case InequalitySign.SIGN_LESS_THAN:
                            inequalityCode = InequalityCode.CODE_LESS_THAN;
                            break;
                        default:
                            inequalityCode = InequalityCode.CODE_EQUAL;
                    }
                } else {
                    inequalityCode = InequalityCode.CODE_EQUAL;
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

        Log.d(TAG, "interpreted json: " + json);

        return json;
    }

    private static Banners searchBanner(@Nonnull String bannerName, Realm realm) {
        if (bannerName.length() < 4) {
            return null;
        }
        //Log.d(TAG, "banner search :" + bannerName);
        return realm.where(Banners.class).contains(Banners.FIELD_NAME_WITHOUT_BLANK, bannerName).findFirst();
    }

    private static UnitTypes searchType(@Nonnull String typeName, Realm realm) {
        if (typeName.length() < 2) {
            return null;
        }
        //Log.d(TAG, "type search :" + typeName);
        return realm.where(UnitTypes.class).contains(UnitTypes.FIELD_NAME, typeName).or().equalTo(UnitTypes.FIELD_NAME2, typeName).findFirst();
    }

    private static RealmResults<Units> searchUnitsByName(@Nonnull String unitName, Realm realm) {
        int length = unitName.length();
        if (length > 1 && unitName.substring(length - 1).equals(SURNAME)) {
            return realm.where(Units.class).like(Units.FIELD_NAME, unitName.substring(0, length - 1) + "*").findAll();
        } else if (length < 2) {
            return null;
        }
        return realm.where(Units.class).equalTo(Units.FIELD_NAME, unitName).or().contains(UnitTypes.FIELD_NAME2, unitName).findAll();
    }


    public static List<String> search(JSONObject json, Realm realm) throws Exception {
        /*
        공범확 30 특성 피범확 수치 8 이상 일필
        {"passives":{"일필":{},"피범확":{"passiveLists.val":{"val":8,"inequality":1},"passiveLists.unitLevel":{"val":30,"inequality":0}},"공범확":{}}}
        무 >= 30 지력 < 60 20코 미만 30특성 방보조 수치 10 이상 행 < 99
        {"fields":{"val":30,"inequality":1,"cost":{"val":20,"inequality":4},"lck":{"val":99,"inequality":4},"intel":{"val":60,"inequality":4}},"passives":{"방보조":{"passiveLists.val":{"val":10,"inequality":1},"passiveLists.unitLevel":{"val":30,"inequality":0}}}}
         */

        //List<OrderedRealmCollection<Units>> collectionList = new ArrayList<>();

        List<String> messages = new ArrayList<>();

        RealmQuery<Units> query = realm.where(Units.class);

        StringBuilder fieldCriteria = new StringBuilder();

        if (json.has("fields")) {
            JSONObject fieldObject = (JSONObject) json.get("fields");
            Iterator<String> fieldKeys = fieldObject.keys();
            while (fieldKeys.hasNext()) {
                String field = fieldKeys.next();
                //Log.d(TAG, "field: " + field);
                fieldCriteria.append(field);
                JSONObject fieldDetails = (JSONObject) fieldObject.get(field);
                if (fieldDetails.has("val")) {
                    int value = fieldDetails.getInt("val");
                    fieldCriteria.append(": ").append(value);
                    //Log.d(TAG, "value: " + value);
                    if (fieldDetails.has("inequality")) {
                        switchInequality((int) fieldDetails.get("inequality"), query, field, value, fieldCriteria);
                    } else {
                        query.equalTo(field, value);
                    }
                }
                fieldCriteria.append("\r\n");
            }
        }

        OrderedRealmCollection<Units> unitsByName = new RealmList<>();

        if (json.has("passives")) {
            JSONObject passiveObject = (JSONObject) json.get("passives");
            List<Integer> baseList = new ArrayList<>();
            List<Passives[]> passiveArraysList = new ArrayList<>();
            List<JSONObject> criteriaList = new ArrayList<>();
            Iterator<String> passiveKeys = passiveObject.keys();
            boolean queryIsValid = false;
            while (passiveKeys.hasNext()) {
                String passiveStr = passiveKeys.next();

                Banners banner = searchBanner(passiveStr, realm);
                if (banner != null) {
                    query.equalTo(Units.FIELD_BANNER_ID, banner.getId());
                    queryIsValid = true;
                    fieldCriteria.append(banner.getString(Banners.FIELD_NAME)).append("\r\n");
                    //Log.d(TAG, "banner name: " + banner.getString(Banners.FIELD_NAME));
                    //Log.d(TAG, "banner nwb: " + banner.getString(Banners.FIELD_NAME_WITHOUT_BLANK));
                } else {
                    UnitTypes type = searchType(passiveStr, realm);
                    if (type != null) {
                        query.equalTo(Units.FIELD_TYPE_ID, type.getId());
                        queryIsValid = true;
                        fieldCriteria.append(type.getString(UnitTypes.FIELD_NAME)).append("\r\n");
                        //Log.d(TAG, "UnitTypes: " + type.getString(UnitTypes.FIELD_NAME));
                    } else {
                        RealmResults<Units> units = searchUnitsByName(passiveStr, realm);
                        if (units != null && units.size() > 0) {
                            unitsByName.addAll(units);
                        } else {
                            RealmResults<Passives> passives = realm.where(Passives.class).contains(Passives.FIELD_NAME2, passiveStr).or().contains(Passives.FIELD_NAME_WITHOUT_BLANK, passiveStr).findAll();
                            int base = passives.size();
                            if (base > 0) {
                                Log.d(TAG, "passiveStr: " + passiveStr);
                                Log.d(TAG, "base: " + base);
                                baseList.add(base);
                                Passives[] passiveArray = new Passives[base];
                                for (int i = 0; i < passiveArray.length; i++) {
                                    Passives passive = passives.get(i);
                                    if (passive != null) {
                                        //int passiveId = passive.getId();
                                        //String passiveName = passive.getString(Passives.FIELD_NAME);
                                        passiveArray[i] = passive;
                                        //Log.d(TAG, "passiveId: " + passiveId);
                                        //Log.d(TAG, "passiveName: " + passiveName);
                                    }

                                }
                                passiveArraysList.add(passiveArray);
                                criteriaList.add((JSONObject) passiveObject.get(passiveStr));
                            }
                        }
                    }

                }

            }


            if (baseList.size() == 0) {
                if (queryIsValid) {
                    messages.add(toMessage(query.findAll().sort(Units.FIELD_COST).sort(Units.FIELD_BANNER_ID), fieldCriteria.toString(), false));
                }
            } else {
                RealmResults<Units> tentativeResult = query.findAll();
                MultiBaseNotation mbn = new MultiBaseNotation(baseList);
                for (int[] comb : mbn.getPositiveBaseDigitsCombination(99999999)) {
                    RealmQuery<Units> passiveQuery = tentativeResult.where();
                    StringBuilder passiveCriteria = new StringBuilder();
                    passiveCriteria.append(fieldCriteria);

                    for (int i = 0; i < comb.length; i++) {
                        Passives passive = (passiveArraysList.get(i))[comb[i]];
                        int passiveId = passive.getId();
                        String passiveName = passive.getString(Passives.FIELD_NAME);
                        passiveCriteria.append(passiveName);
                        JSONObject criteria = criteriaList.get(i);
                        Iterator<String> criteriaKeys = criteria.keys();
                        while (criteriaKeys.hasNext()) {
                            String field = criteriaKeys.next();
                            JSONObject criteriaDetails = (JSONObject) criteria.get(field);
                            if (criteriaDetails.has("val")) {
                                int value = criteria.getInt("val");
                                passiveCriteria.append(": ").append(value);
                                if (criteriaDetails.has("inequality")) {
                                    switchInequality((int) criteriaDetails.get("inequality"), passiveQuery, field, value, passiveCriteria);
                                } else {
                                    passiveQuery.equalTo(field, value);
                                }
                            }
                        }
                        passiveCriteria.append("\r\n");
                        passiveQuery.equalTo(TextUtils.join(".", new String[]{Units.FIELD_PASSIVE_LISTS, PassiveList.FIELD_PASSIVE, Source.FIELD_ID}), passiveId);
                    }
                    //query.endGroup();
                    if (passiveQuery.isValid()) {
                        messages.add(toMessage(passiveQuery.findAll().sort(Units.FIELD_COST).sort(Units.FIELD_BANNER_ID), passiveCriteria.toString(), false));
                    }
                }
            }
        } else {
            messages.add(toMessage(query.findAll().sort(Units.FIELD_COST).sort(Units.FIELD_BANNER_ID), fieldCriteria.toString(), false));
        }

        if (unitsByName.size() > 0) {
            messages.add(toMessage(unitsByName, null, true));
        }


        return messages;

    }

    private static String toMessage(@Nonnull OrderedRealmCollection<Units> units, String criteria, boolean byName) {
        StringBuilder builder = new StringBuilder();
        if (units.size() == 1) {
            Units unit = units.first();
            if (unit != null) {
                builder.append(unit.getType().getString(Source.FIELD_NAME)).append(" ")
                        .append(unit.getString(Source.FIELD_NAME)).append("\r\n")
                        .append(COST_ENG).append(": ")
                        .append(TextUtils.join("→", unit.getCosts())).append("\r\n")
                        .append("계보: ").append(unit.getBanner().getString(Source.FIELD_NAME)).append("\r\n");
                RealmList<Friendships> friendships = unit.getFriendshipList();
                if (friendships != null) {
                    if (friendships.size() > 1) {
                        for (int i = 0; i < friendships.size(); i++) {
                            Friendships friendship = friendships.get(i);
                            if (friendship != null)
                                builder.append("인연").append(i + 1).append(": ").append(friendship.getString(Source.FIELD_NAME)).append("\r\n");
                        }
                    } else if (friendships.size() == 1) {
                        Friendships friendship = friendships.first();
                        if (friendship != null)
                            builder.append("인연: ").append(friendship.getString(Source.FIELD_NAME)).append("\r\n");
                    }
                }
                builder.append(TextUtils.join(" "
                        , new String[]{
                                StatKorSimple.STR_SIMPLE + unit.getString(Units.FIELD_STR),
                                StatKorSimple.INTEL_SIMPLE + unit.getString(Units.FIELD_INTEL),
                                StatKorSimple.CMD_SIMPLE + unit.getString(Units.FIELD_CMD),
                                StatKorSimple.DEX_SIMPLE + unit.getString(Units.FIELD_DEX),
                                StatKorSimple.LCK_SIMPLE + unit.getString(Units.FIELD_LCK)
                        })).append(" (+").append(5 * (unit.getInt(Units.FIELD_COST) + 16)).append(")\r\n");

                OrderedRealmCollection<PassiveList> passiveLists = unit.getPassiveLists().sort(PassiveList.FIELD_UNIT_LEVEL, Sort.ASCENDING);
                if (passiveLists != null) {
                    for (int i = 0; i < passiveLists.size(); i++) {
                        PassiveList passiveList = passiveLists.get(i);
                        if (passiveList != null) {
                            Passives passive = passiveList.getPassive();
                            String passiveName = passive.getString(Source.FIELD_NAME);
                            int val = passiveList.getInt(PassiveList.FIELD_VAL);
                            String value;
                            if (val > 0) {
                                if (StringUtils.right(passiveName, 1).equals("%")) {
                                    passiveName = StringUtils.left(passiveName, passiveName.length() - 1);
                                    value = val + "%";
                                } else {
                                    value = "[" + val + "]";
                                }
                            } else {
                                value = "";
                            }

                            builder.append("Lv").append(passiveList.getInt(PassiveList.FIELD_UNIT_LEVEL))
                                    .append(": ").append(passiveName)
                                    .append(" ").append(value).append("\r\n");
                        }
                    }
                }
                PrefectSkills prefectSkill = unit.getPrefectSkill();
                if (prefectSkill != null)
                    builder.append("태수: ").append(prefectSkill.getString(Source.FIELD_NAME)).append("\r\n");
                WarlordSkills warlordSkill = unit.getWarlordSkill();
                if (warlordSkill != null)
                    builder.append("군주: ").append(warlordSkill.getString(Source.FIELD_NAME)).append("\r\n");
                Personality personality = unit.getPersonality();
                if (personality != null)
                    builder.append("성격: ").append(personality.getString(Source.FIELD_NAME));
            }
        } else if (units.size() > 1) {
            if (criteria != null) {
                builder.append(criteria).append("-----------------\r\n");
            }
            builder.append("병종　　 이름　　 코스트");

            Map<Integer, Friendships> friendshipMap = null;
            List<Integer> idList = null;
            int costSum = 0;
            if (byName) {
                friendshipMap = new HashMap<>();
                idList = new ArrayList<>();
            }
            int size = units.size();
            int digit = (int)(Math.log10(size)+1);
            for (int i = 0; i < size; i++) {
                Units unit = units.get(i);
                int cost = unit.getIntCostByGrade(5);
                costSum += cost;
                builder.append("\r\n");
                if( byName ) {
                    builder.append(StringUtils.leftPad(String.valueOf(i+1),digit,'0')).append(". ");
                }
                builder.append(StringUtils.rightPad(unit.getType().getString(UnitTypes.FIELD_NAME),4,'　'))
                        .append(" ").append(StringUtils.rightPad(unit.getString(Units.FIELD_NAME),4, '　'))
                        .append(" ").append(cost);
                if (byName) {
                    idList.add(unit.getId());
                    for( Friendships friendship : unit.getFriendshipList() ) {
                        friendshipMap.put(friendship.getId(), friendship);
                    }
                }
            }

            if( byName ) {
                builder.append("\r\n-------------\r\n").append("코스트 합: [").append(costSum).append("]");
                for (Integer friendshipId : friendshipMap.keySet()) {
                    Friendships friendship = friendshipMap.get(friendshipId);
                    if (friendship != null) {
                        List<PassiveList> activePassives = friendship.checkActive(idList);
                        if( activePassives != null && activePassives.size() > 0) {
                            builder.append("\r\n-------------\r\n")
                                    .append(friendship.getString(Friendships.FIELD_NAME)).append(" 효과");
                            for( PassiveList passiveList : activePassives ) {
                                String passiveName = passiveList.getPassive().getString(Passives.FIELD_NAME);
                                boolean percentageValue = false;
                                if( passiveName.substring(passiveName.length() - 1).equals("%")) {
                                    percentageValue = true;
                                    passiveName = passiveName.substring(0, passiveName.length()- 1);
                                }
                                builder.append("\r\n").append(passiveName);
                                int val = passiveList.getInt(PassiveList.FIELD_VAL);
                                if( val > 0 ) {
                                    String value = percentageValue ? val + "%" : "[" + val + "]";
                                    builder.append(" ").append(value);
                                }
                            }
                        }
                    }
                }
            }
        }


        return builder.toString();
    }


    private static void switchInequality(int inequalityCode, RealmQuery<Units> query, String field, int value, StringBuilder criteria) {
        Log.d(TAG, "inequalityCode: " + inequalityCode);
        switch (inequalityCode) {
            case InequalityCode.CODE_EQUAL:
                query.equalTo(field, value);
                break;
            case InequalityCode.CODE_GREATER_OR_EQUAL:
                query.greaterThanOrEqualTo(field, value);
                criteria.append(InequalityKor.GREATER_OR_EQUAL);
                break;
            case InequalityCode.CODE_GREATER_THAN:
                query.greaterThan(field, value);
                criteria.append(InequalityKor.GREATER_THAN);
                break;
            case InequalityCode.CODE_LESS_OR_EQUAL:
                query.lessThanOrEqualTo(field, value);
                criteria.append(InequalityKor.LESS_OR_EQUAL);
                break;
            case InequalityCode.CODE_LESS_THAN:
                query.lessThan(field, value);
                criteria.append(InequalityKor.LESS_THAN);
                break;
        }
    }
}
