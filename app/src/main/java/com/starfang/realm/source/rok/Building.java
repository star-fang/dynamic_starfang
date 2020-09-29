package com.starfang.realm.source.rok;

import android.text.TextUtils;

import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.Source;

import org.apache.commons.lang3.math.NumberUtils;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Building extends RealmObject implements Source {

    public static final String FIELD_CONTENT_ID = "contentId";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_FIGURES = "figures";
    public static final String FIELD_FOOD = "food";
    public static final String FIELD_WOOD = "wood";
    public static final String FIELD_STONE = "stone";
    public static final String FIELD_GOLD = "gold";

    public static final String FIELD_LEVEL_VAL = "levelVal";
    public static final String FIELD_REQ_BDS = "reqBuildings";
    public static final String FIELD_COST_FOOD = "foodCost";
    public static final String FIELD_COST_WOOD = "woodCost";
    public static final String FIELD_COST_STONE = "stoneCost";
    public static final String FIELD_COST_GOLD = "goldCost";
    public static final String FIELD_SECONDS = "seconds";
    public static final String FIELD_POWER_VAL = "powerVal";
    public static final String FIELD_RW_FOOD = "foodReward";
    public static final String FIELD_RW_WOOD = "woodReward";
    public static final String FIELD_RW_STONE = "stoneReward";
    public static final String FIELD_RW_GOLD = "goldReward";



    @PrimaryKey
    private int id;
    private int contentId;

    private String level;
    private RealmList<RealmString> requirements;
    private RealmList<RealmString> cost;
    private String time;
    private String power;

    private RealmList<RealmString> reward;
    private RealmList<RealmString> figures;

    //runtime fields
    private RealmList<Building> reqBuildings;
    private BuildContent content;

    private int levelVal;
    private int foodCost;
    private int woodCost;
    private int stoneCost;
    private int goldCost;

    private int seconds;
    private String timeKor;
    private int powerVal;

    private int foodReward;
    private int woodReward;
    private int stoneReward;
    private int goldReward;

    public RealmList<RealmString> getFigures() {
        return figures;
    }

    public void setReqBuildings(RealmList<Building> reqBuildings) {
        this.reqBuildings = reqBuildings;
    }

    public RealmList<Building> getReqBuildings() {
        return reqBuildings;
    }

    public RealmList<RealmString> getRequirements() {
        return requirements;
    }

    private int siValue(String character, double quantity ) {
        int unit;
        switch (character.toUpperCase()) {
            case "K":
                unit = 1000;
                break;
            case "M":
                unit = 1000*1000;
                break;
            case "B":
                unit = 1000*1000*1000;
                break;
            default:
                unit = 1;
        }
        return (int)(quantity * (double)unit);
    }

    public static String quantityToString( int quantity ) {
        StringBuilder quantityBuilder = new StringBuilder();
        if( quantity >= 100000000 ) {
            quantityBuilder.append( quantity / 100000000 ).append("억 ");
            quantity %= 100000000;
        }

        if( quantity >= 10000 ) {
            quantityBuilder.append( quantity / 10000 ).append("만 ");
            quantity %= 10000;
        }

        if( quantity > 0 ) {
            quantityBuilder.append( quantity );
        }

        return quantityBuilder.toString().trim();
    }

    public static String secondsToString( int seconds ) {
        StringBuilder timeBuilder = new StringBuilder();
        if( seconds >= 24 * 60 * 60 ) {
            timeBuilder.append( seconds / (24 * 60 * 60) ).append("일 ");
            seconds %= 24 * 60 * 60;
        }

        if( seconds >= 60 * 60 ) {
            timeBuilder.append( seconds / (60 * 60) ).append("시간 ");
            seconds %= 60 * 60;
        }

        if( seconds >= 60 ) {
            timeBuilder.append( seconds / 60 ).append("분 ");
            seconds %= 60;
        }

        if( seconds > 0 ) {
            timeBuilder.append( seconds ).append("초 ");
        }

        return  timeBuilder.toString().trim();
    }

    private int stringToSeconds( String time ) {
        String[] timeWords = time.split("\\s+");
        StringBuilder timeKorBuilder = new StringBuilder();
        int sum = 0;
        for( String timeWord : timeWords ) {
            int value = NumberUtils.toInt(timeWord.replaceAll("[^0-9]","").trim(),0);
            String timeUnitKor;
            switch(timeWord.replaceAll("[0-9]","").trim().toLowerCase()) {
                case "d":
                    sum += value * 24 * 60 * 60;
                    timeUnitKor = "일";
                    break;
                case "h":
                    sum += value * 60 * 60;
                    timeUnitKor = "시간";
                    break;
                case "m":
                    sum += value * 60;
                    timeUnitKor = "분";
                    break;
                case "s":
                    sum += value;
                    timeUnitKor = "초";
                    break;
                default:
                    timeUnitKor = null;
            } // switch
            if( value > 0 && timeUnitKor != null ) {
                timeKorBuilder.append(value).append(timeUnitKor).append(" ");
            }
        } // for
        timeKor = timeKorBuilder.toString();
        return sum;
    }

    public void updateIntValues() {
        for( RealmString co : cost) {
            String costStr = co.toString();
            String rssCategory = costStr.replaceAll("[0-9]{1,3}.[0-9]{1,3}[a-zA-Z]", "").trim();
            String siUnit = costStr.substring(costStr.length() - 1);
            double quantity = NumberUtils.toDouble(costStr.replaceAll("[^0-9.]", ""),0.0);
            switch (rssCategory.toLowerCase()) {
                case FIELD_FOOD:
                    foodCost = siValue(siUnit, quantity);
                    break;
                case FIELD_WOOD:
                    woodCost = siValue(siUnit, quantity);
                    break;
                case FIELD_STONE:
                    stoneCost = siValue(siUnit, quantity);
                    break;
                case FIELD_GOLD:
                    goldCost = siValue(siUnit, quantity);
                    break;
                default:
            }
        }


        for( RealmString rw : reward) {
            String rwStr = rw.toString();
            String rssCategory = rwStr.replaceAll("[0-9]{1,3}.[0-9]{1,3}[a-zA-Z]", "").trim();
            String siUnit = rwStr.substring(rwStr.length() - 1);
            double quantity = NumberUtils.toDouble(rwStr.replaceAll("[^0-9.]", ""),0.0);
            switch (rssCategory.toLowerCase()) {
                case FIELD_FOOD:
                    foodReward = siValue(siUnit, quantity);
                    break;
                case FIELD_WOOD:
                    woodReward = siValue(siUnit, quantity);
                    break;
                case FIELD_STONE:
                    stoneReward = siValue(siUnit, quantity);
                    break;
                case FIELD_GOLD:
                    goldReward = siValue(siUnit, quantity);
                    break;
                default:
            }
        }
        this.levelVal = NumberUtils.toInt(level,0);
        this.seconds = stringToSeconds(this.time);
        this.powerVal = NumberUtils.toInt(power,0);
    }

    @Override
    public int getId() {
        return id;
    }

    public BuildContent getContent() {
        return content;
    }

    public void setContent(BuildContent content) {
        this.content = content;
    }

    @Override
    public String getString(String field) {
        switch (field) {
            case FIELD_FIGURES:
                if( figures != null ) {
                    return TextUtils.join(",", figures);
                } else {
                    return null;
                }
            case FIELD_TIME:
                return timeKor;
        }
        return null;
    }

    @Override
    public int getInt(String field) {
        switch (field) {
            case FIELD_ID:
                return id;
            case FIELD_CONTENT_ID:
                return contentId;
            case FIELD_LEVEL_VAL:
                return levelVal;
            case FIELD_COST_FOOD:
                return foodCost;
            case FIELD_COST_WOOD:
                return woodCost;
            case FIELD_COST_STONE:
                return stoneCost;
            case FIELD_COST_GOLD:
                return goldCost;
            case FIELD_RW_FOOD:
                return foodReward;
            case FIELD_RW_WOOD:
                return woodReward;
            case FIELD_RW_STONE:
                return stoneReward;
            case FIELD_RW_GOLD:
                return goldReward;
            case FIELD_POWER_VAL:
                return powerVal;
            case FIELD_SECONDS:
                return seconds;
            default:
                return 0;
        }
    }
}