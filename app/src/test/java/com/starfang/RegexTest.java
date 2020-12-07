package com.starfang;

import com.starfang.realm.source.caocao.PassiveList;
import com.starfang.realm.source.caocao.Units;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegexTest {

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

    @Test
    public void regex() {
        //interpret("30 특성 전화");

        try {
            interpret("공범확 30 특성 피범확 수치 8 이상 일필");
            interpret("통솔 10 무력 >= 30 지력 < 60 20코 미만 30특성 방보조 수치 10 이상 행 < 99");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*
        {
         "str":{
          "val": 30,
          "inequality": 0
         },
         "intel":{
          "val": 60,
          "inequality": 1
         },
         "cost":{
          "val": 20,
          "inequality": 4
         },
          "방보조" : {
                "lv":{
                 "val": 30,
                 "inequality": 0
                },
                "val":{
                 "val": 10,
                 "inequality": 1
                }

           }
        }
         */
    }

    private void interpret(String text) throws JSONException {

        final List<String> words = new ArrayList<>();
        final List<String> values = new ArrayList<>();

        System.out.println(text);

        //System.out.print("words: ");
        Pattern reg = Pattern.compile("[\\uAC00-\\uDCAFa-zA-Z>=<]+");
        Matcher matcher = reg.matcher(text);

        while (matcher.find()) {
            String word = matcher.group();
            if (!word.equals("몰우전")) {
                //System.out.print("[" + word + "] ");
                words.add(word);
            }
        }
        //System.out.println();
       // System.out.print("values: ");
        reg = Pattern.compile("\\d+|몰우전");
        matcher = reg.matcher(text);
        while (matcher.find()) {
            String value = matcher.group();
            //System.out.print("[" + value + "] ");
            values.add(value);
        }
        //System.out.println();


        final int[] vals = new int[values.size()];
        for (int i = 0; i < vals.length; i++)
            vals[i] = NumberUtils.toInt(values.get(i), 0);

        final int wordsSize = words.size();

        JSONObject json = new JSONObject();
        for (int i = 0, j = 0; i < wordsSize; i++) {
            String unit_field;
            String passive = null;
            switch (words.get(i).toUpperCase()) {
                case COST_ENG: case COST_KOR:
                case COST_KOR_SIMPLE:
                    unit_field = Units.FIELD_COST;
                    break;
                case STR: case STR_SIMPLE:
                    unit_field = Units.FIELD_STR;
                    break;
                case INTEL: case INTEL_SIMPLE:
                    unit_field = Units.FIELD_INTEL;
                    break;
                case CMD: case CMD_SIMPLE:
                    unit_field = Units.FIELD_CMD;
                    break;
                case DEX: case DEX_SIMPLE:
                    unit_field = Units.FIELD_DEX;
                    break;
                case LCK: case LCK_SIMPLE:
                    unit_field = Units.FIELD_LCK;
                    break;
                case PASSIVE:
                case CHARACTERISTIC:
                    if (i + 1 < wordsSize) {
                        passive = words.get(i + 1);
                        unit_field = Units.FIELD_PASSIVE_LISTS+"."+ PassiveList.FIELD_UNIT_LEVEL;
                    } else {
                        unit_field = null;
                    }
                    break;
                case VAL:
                case PRICE:
                    if (i > 0) {
                        passive = words.get(i - 1);
                        unit_field =  Units.FIELD_PASSIVE_LISTS+"."+PassiveList.FIELD_VAL;
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
                //fieldDetails.put(unit_field,fieldDetails );
                // System.out.print("field:" + unit_field + ", value:" + vals[j] + ", range:" +
                //        (containInequality ? words.get(i + 1) : "일치") );

                if (passive != null) {
                    //System.out.println( ", search: " + passive);
                    JSONObject passiveObject;
                    if( json.has( "passives")) {
                        passiveObject = (JSONObject)json.get("passives");
                    } else {
                        passiveObject = new JSONObject();
                        json.put("passives", passiveObject);
                    }
                    JSONObject passiveDetails;
                    if( passiveObject.has(passive )) {
                        passiveDetails  = (JSONObject) passiveObject.get(passive);
                    } else {
                        passiveDetails = new JSONObject();
                        passiveObject.put(passive, passiveDetails);
                    }
                    passiveDetails.put(unit_field, fieldDetails);

                } else {
                    JSONObject fieldObject;
                    if( json.has("fields")) {
                        fieldObject = (JSONObject)json.get("fields");
                    } else {
                        fieldObject = new JSONObject();
                        json.put("fields", fieldObject);
                    }
                    fieldObject.put(unit_field, fieldDetails);
                   // System.out.println();
                }

                if (inequalityCode > 0) {
                    ++i;
                }


                ++j;


            } else {
                passive = words.get(i);
                JSONObject passiveObject;
                if( json.has( "passives")) {
                    passiveObject = (JSONObject)json.get("passives");
                } else {
                    passiveObject = new JSONObject();
                    json.put("passives", passiveObject);
                }

                if (!passiveObject.has(passive)) {
                    passiveObject.put(passive, new JSONObject());
                }

                //json.put(passive,null);
                //System.out.println("search: " + passive);
            }

        } // for


        System.out.println(json.toString());
    }
}
