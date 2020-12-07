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

    public static final String FIELD_LEVEL_VAL = "levelVal";
    public static final String FIELD_LEVEL = "level";
    public static final String FIELD_REQ_BDS = "reqBuildings";
    public static final String FIELD_COST_FOOD = "foodCost";
    public static final String FIELD_COST_WOOD = "woodCost";
    public static final String FIELD_COST_STONE = "stoneCost";
    public static final String FIELD_COST_GOLD = "goldCost";
    public static final String FIELD_COST_BP = "blueprintCost";
    public static final String FIELD_COST_BOOK = "bookCost";
    public static final String FIELD_COST_ARROW = "arrowCost";
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
    private RealmList<Building> preBuildings;
    private BuildContent content;

    private int levelVal;
    private int foodCost;
    private int woodCost;
    private int stoneCost;
    private int goldCost;
    private int blueprintCost;
    private int bookCost; // 계약의 서
    private int arrowCost; // 저항의 화살

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

    public void updateIntValues() {

        for( RealmString co : cost) {
            String costStr = co.toString();
            String rssCategory = costStr.replaceAll("[0-9]{1,3}.[0-9]{1,3}[a-zA-Z]", "").trim();
            String siUnit = costStr.substring(costStr.length() - 1);
            double quantity = NumberUtils.toDouble(costStr.replaceAll("[^0-9.]", ""),0.0);
            switch (rssCategory.toLowerCase()) {
                case "food":
                    foodCost = RokCalcUtils.siValue(siUnit, quantity);
                    break;
                case "wood":
                    woodCost = RokCalcUtils.siValue(siUnit, quantity);
                    break;
                case "stone":
                    stoneCost = RokCalcUtils.siValue(siUnit, quantity);
                    break;
                case "gold":
                    goldCost = RokCalcUtils.siValue(siUnit, quantity);
                    break;
                case "Arrow of Resistance x":
                    arrowCost = (int)quantity;
                    break;
                case "Book of Covenant x":
                    bookCost = (int)quantity;
                    break;
                case "x Master's Blueprint":
                    blueprintCost = (int)quantity;
                    break;
                default:
                    if( costStr.toLowerCase().contains("blueprint")) {
                        blueprintCost = (int)quantity;
                    }
            }
        }


        for( RealmString rw : reward) {
            String rwStr = rw.toString();
            String rssCategory = rwStr.replaceAll("[0-9]{1,3}.[0-9]{1,3}[a-zA-Z]", "").trim();
            String siUnit = rwStr.substring(rwStr.length() - 1);
            double quantity = NumberUtils.toDouble(rwStr.replaceAll("[^0-9.]", ""),0.0);
            switch (rssCategory.toLowerCase()) {
                case "food":
                    foodReward = RokCalcUtils.siValue(siUnit, quantity);
                    break;
                case "wood":
                    woodReward = RokCalcUtils.siValue(siUnit, quantity);
                    break;
                case "stone":
                    stoneReward = RokCalcUtils.siValue(siUnit, quantity);
                    break;
                case "gold":
                    goldReward = RokCalcUtils.siValue(siUnit, quantity);
                    break;
                default:
            }
        }
        this.levelVal = NumberUtils.toInt(level,0);
        this.seconds = RokCalcUtils.stringToSeconds(this.time);
        this.timeKor = RokCalcUtils.secondsToString(this.seconds);
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
            case FIELD_NAME:
                if( content != null ) {
                    return content.getString(Source.FIELD_NAME);
                } else {
                    return  null;
                }
            case FIELD_FIGURES:
                if( figures != null ) {
                    return TextUtils.join(",", figures);
                } else {
                    return null;
                }
            case FIELD_TIME:
                return timeKor;
            case FIELD_LEVEL:
                return level;
        }
        return null;
    }

    public String getCostInfo() {

        StringBuilder costInfoBuilder = new StringBuilder();
        if (foodCost > 0)
            costInfoBuilder.append("\r\n - 식량: ").append(RokCalcUtils.quantityToString(foodCost));
        if (woodCost > 0)
            costInfoBuilder.append("\r\n - 목재: ").append(RokCalcUtils.quantityToString(woodCost));
        if (stoneCost > 0)
            costInfoBuilder.append("\r\n - 석제: ").append(RokCalcUtils.quantityToString(stoneCost));
        if (goldCost > 0)
            costInfoBuilder.append("\r\n - 금화: ").append(RokCalcUtils.quantityToString(goldCost));
        if (blueprintCost > 0)
            costInfoBuilder.append("\r\n - 청사진: ").append(blueprintCost).append("장");
        if (bookCost > 0)
            costInfoBuilder.append("\r\n - 계약의 서: ").append(bookCost).append("권");
        if (arrowCost > 0)
            costInfoBuilder.append("\r\n - 저항의 화살: ").append(arrowCost).append("개");

        return  costInfoBuilder.toString();
    }

    @Override
    public int getInt(String field) {
        switch (field) {
            case FIELD_ID:
                return id;
            case FIELD_CONTENT_ID:
                return contentId;
            case FIELD_LEVEL:
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
            case FIELD_COST_BP:
                return blueprintCost;
            case FIELD_COST_BOOK:
                return bookCost;
            case FIELD_COST_ARROW:
                return arrowCost;
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

    public RealmList<Building> getPreBuildings() {
        return preBuildings;
    }

    public void setPreBuildings(RealmList<Building> preBuildings) {
        this.preBuildings = preBuildings;
    }
}