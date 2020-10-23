package com.starfang.realm.source.rok;

import android.text.TextUtils;

import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.Source;

import org.apache.commons.lang3.math.NumberUtils;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Technology extends RealmObject implements Source {

    public static final String FIELD_CONTENT_ID = "contentId";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_TIME = "time";
    public static final String FIELD_FIGURES = "figures";
    public static final String FIELD_FOOD = "food";
    public static final String FIELD_WOOD = "wood";
    public static final String FIELD_STONE = "stone";
    public static final String FIELD_GOLD = "gold";

    public static final String FIELD_LEVEL_VAL = "levelVal";
    public static final String FIELD_LEVEL = "level";
    public static final String FIELD_REQ_TECHS = "requiredTechList";
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
    private RealmList<RealmString> costs;
    private String time;
    private String power;

    private String food;
    private String wood;
    private String stone;
    private String gold;


    //runtime fields
    private RealmList<Technology> reqTechs;
    private RealmList<Technology> preTechs;
    private RealmList<Building> reqBuildings;
    private RealmList<Building> preBuildings;
    private TechContent content;

    private RealmList<RealmString> figures;

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

    public void setReqTechs(RealmList<Technology> reqTechs) {
        this.reqTechs = reqTechs;
    }

    public void setReqBuildings(RealmList<Building> reqBuildings) {
        this.reqBuildings = reqBuildings;
    }

    public RealmList<Building> getReqBuildings() {
        return reqBuildings;
    }

    public RealmList<Technology> getReqTechs() {
        return reqTechs;
    }

    public RealmList<RealmString> getRequirements() {
        return requirements;
    }

    public void updateIntValues() {
        for( RealmString cost : costs) {
            String costStr = cost.toString();
            String rssCategory = costStr.replaceAll("[0-9]{1,3}.[0-9]{1,3}[a-zA-Z]", "").trim();
            String siUnit = costStr.substring(costStr.length() - 1);
            double quantity = NumberUtils.toDouble(costStr.replaceAll("[^0-9.]", ""),0.0);
            switch (rssCategory.toLowerCase()) {
                case FIELD_FOOD:
                    foodCost = RokCalcUtils.siValue(siUnit, quantity);
                    break;
                case FIELD_WOOD:
                    woodCost = RokCalcUtils.siValue(siUnit, quantity);
                    break;
                case FIELD_STONE:
                    stoneCost = RokCalcUtils.siValue(siUnit, quantity);
                    break;
                case FIELD_GOLD:
                    goldCost = RokCalcUtils.siValue(siUnit, quantity);
                    break;
                default:
            }
        }
        this.levelVal = NumberUtils.toInt(level,0);
        this.foodReward = NumberUtils.toInt(food,0);
        this.woodReward = NumberUtils.toInt(wood,0);
        this.stoneReward = NumberUtils.toInt(stone,0);
        this.goldReward = NumberUtils.toInt(gold,0);
        this.seconds = RokCalcUtils.stringToSeconds(this.time);
        this.timeKor = RokCalcUtils.secondsToString(this.seconds);
        this.powerVal = NumberUtils.toInt(power,0);
    }

    @Override
    public int getId() {
        return id;
    }

    public TechContent getContent() {
        return content;
    }

    public void setContent(TechContent content) {
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
            case FIELD_LEVEL:
                return level;
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

    public RealmList<Technology> getPreTechs() {
        return preTechs;
    }

    public void setPreTechs(RealmList<Technology> preTechs) {
        this.preTechs = preTechs;
    }

    public RealmList<Building> getPreBuildings() {
        return preBuildings;
    }

    public void setPreBuildings(RealmList<Building> preBuildings) {
        this.preBuildings = preBuildings;
    }

    public boolean containsPreTech( Technology preTech ) {
        return preTechs != null && preTechs.contains(preTech);
    }

    public boolean containsPreBuilding( Building preBuilding ) {
        return preBuildings != null && preBuildings.contains(preBuilding);
    }

}
