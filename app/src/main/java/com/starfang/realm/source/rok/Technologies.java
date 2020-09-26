package com.starfang.realm.source.rok;

import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import org.apache.commons.lang3.math.NumberUtils;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Technologies extends RealmObject implements Source, SearchNameWithoutBlank {

    public static final String FIELD_CONTENT_ID = "contentId";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_FACT = "fact";
    public static final String FIELD_FIGURE = "figure";
    public static final String FIELD_FIGURE_VAL = "figureVal";
    public static final String FIELD_LEVEL_VAL = "levelVal";
    public static final String FIELD_REQ_TECHS = "ee";
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
    public static final String FIELD_FOOD = "food";
    public static final String FIELD_WOOD = "wood";
    public static final String FIELD_STONE = "stone";
    public static final String FIELD_GOLD = "gold";


    @PrimaryKey
    private int id;
    private int contentId;
    private String name;

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
    private String nameWithoutBlank;
    private TechContent content;

    private String fact;
    private String figure;
    private int figureVal;

    private int levelVal;
    private RealmList<Technologies> requiredTechList;
    private int foodCost;
    private int woodCost;
    private int stoneCost;
    private int goldCost;

    private int seconds;
    private int powerVal;

    private int foodReward;
    private int woodReward;
    private int stoneReward;
    private int goldReward;

    private int siValue( String character, double quantity ) {
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

    private int stringToSeconds( String time ) {
        String[] timeWords = time.split("\\s+");
        int sum = 0;
        for( String timeWord : timeWords ) {
            int value = NumberUtils.toInt(timeWord.replaceAll("^[0-9]","").trim(),0);
            switch(timeWord.replaceAll("[0-9]","").trim().toLowerCase()) {
                case "d":
                    sum += value * 24 * 60 * 60;
                    break;
                case "h":
                    sum += value * 60 * 60;
                    break;
                case "m":
                    sum += value * 60;
                    break;
                case "s":
                    sum += value;
                    break;
                default:
            }
        }
        return sum;
    }

    public void updateIntValues() {
        for( RealmString cost : costs) {
            String costStr = cost.toString();
            String rssCategory = costStr.replaceAll("[0-9]{1,3}.[0-9]{1,3}[a-zA-Z]", "").trim();
            String siUnit = costStr.substring(costStr.length() - 1);
            double quantity = NumberUtils.toDouble(costStr.replaceAll( "^[0-9]|.", ""),0.0);
            switch (rssCategory) {
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
        this.levelVal = NumberUtils.toInt(level,0);
        this.foodReward = NumberUtils.toInt(food,0);
        this.woodReward = NumberUtils.toInt(wood,0);
        this.stoneReward = NumberUtils.toInt(stone,0);
        this.goldReward = NumberUtils.toInt(gold,0);
        this.seconds = stringToSeconds(this.time);
        this.powerVal = NumberUtils.toInt(power,0);
    }


    public void setMinutes() {

    }

    @Override
    public void setNameWithoutBlank() {
        this.nameWithoutBlank = name.replaceAll("\\s+","").trim();
    }

    @Override
    public int getId() {
        return id;
    }

    public TechContent getContent() {
        return content;
    }

    @Override
    public String getString(String field) {
        switch (field) {
            case FIELD_NAME:
                return name;
            case FIELD_NAME_WITHOUT_BLANK:
                return nameWithoutBlank;
            case FIELD_FACT:
                return fact;
            case FIELD_FIGURE:
                return figure;

        }
        return null;
    }

    @Override
    public int getInt(String field) {
        return 0;
    }
}
