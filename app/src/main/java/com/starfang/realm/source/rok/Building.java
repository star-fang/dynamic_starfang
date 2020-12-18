package com.starfang.realm.source.rok;

import android.text.TextUtils;

import com.starfang.realm.primitive.RealmString;
import com.starfang.realm.source.Source;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

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
    private RealmList<RealmString> unlocksList;

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

    public void setUnlocksList(RealmList<RealmString> unlocksList) {
        this.unlocksList = unlocksList;
    }

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

        for (RealmString co : cost) {
            String costStr = co.toString();
            String rssCategory = costStr.replaceAll("[0-9]{1,4}|[0-9]{1,3}.[0-9]{1,3}[a-zA-Z]", "").trim();
            String siUnit = costStr.substring(costStr.length() - 1);
            double quantity = NumberUtils.toDouble(costStr.replaceAll("[^0-9.]", ""), 0.0);
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
                case "arrow of resistance x":
                    arrowCost = (int) quantity;
                    break;
                case "book of covenant x":
                    bookCost = (int) quantity;
                    break;
                case "x master's blueprint":
                    blueprintCost = (int) quantity;
                    break;
                default:
                    if (costStr.toLowerCase().contains("blueprint")) {
                        blueprintCost = (int) quantity;
                    }
            }
        }


        for (RealmString rw : reward) {
            String rwStr = rw.toString();
            String rssCategory = rwStr.replaceAll("[0-9]{1,3}.[0-9]{1,3}[a-zA-Z]", "").trim();
            String siUnit = rwStr.substring(rwStr.length() - 1);
            double quantity = NumberUtils.toDouble(rwStr.replaceAll("[^0-9.]", ""), 0.0);
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
        this.levelVal = NumberUtils.toInt(level, 0);
        this.seconds = RokCalcUtils.stringToSeconds(this.time);
        this.timeKor = RokCalcUtils.secondsToString(this.seconds);
        if( power != null ) {
            this.powerVal = NumberUtils.toInt(power.replaceAll("[^0-9]", ""), 0);
        }
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
                if (content != null) {
                    return content.getString(Source.FIELD_NAME);
                } else {
                    return null;
                }
            case FIELD_FIGURES:
                if (figures != null) {
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

    public String getInfo(boolean detail) {
        if (content != null) {
            StringBuilder infoBuilder = new StringBuilder();

            if (!detail) {
                infoBuilder.append("\r\n - Lv.").append(level).append(": ");
                if (figures != null) {
                    for (RealmString figureObj : figures) {
                        if (figureObj != null) {
                            String figure = figureObj.toString().replace(",", "");
                            if (NumberUtils.isDigits(figure)) {
                                figure = RokCalcUtils.quantityToString(NumberUtils.toInt(figure));
                            }
                            infoBuilder.append(figure).append(", ");
                        }
                    } // foreach figures
                } // if figure != null
                infoBuilder.append(seconds > 0 ? RokCalcUtils.secondsToString(seconds) : "?");
            } // if !detail
            else {
                if (levelVal > 0) {
                    infoBuilder.append(" lv.").append(levelVal);
                }

                RealmList<RealmString> facts = content.getFacts();
                if (facts != null) {
                    for (int i = 0; i < facts.size(); i++) {
                        RealmString fact = facts.get(i);
                        RealmString figure = figures.get(i);
                        if (fact != null)
                            infoBuilder.append("\r\n * ")
                                    .append(fact.toString()).append(figure != null ? ": " + figure.toString() : "");
                    }
                } // if facts != null

                if( seconds > 0 ) {
                    infoBuilder.append("\r\n * 건설 시간: ")
                            .append(RokCalcUtils.secondsToString(seconds));
                }


                if(powerVal > 0 ) {
                    infoBuilder.append("\r\n - 전투력: ")
                            .append(RokCalcUtils.quantityToString(powerVal));
                }

                if (unlocksList != null && unlocksList.size() > 0) {
                    infoBuilder.append("\r\n - 잠금 해제: ");
                    for (RealmString unlocksObj : unlocksList) {
                        if (unlocksObj != null) {
                            if (unlocksList.size() > 1) {
                                infoBuilder.append("\r\n        ");
                            }
                            infoBuilder.append(unlocksObj.toString());
                        }
                    }
                }

                if (reqBuildings != null && reqBuildings.size() > 0) {
                    infoBuilder.append("\r\n - 요구 건물: ");

                    for (Building reqBuilding : reqBuildings) {
                        if( reqBuildings.size() > 1 ) {
                            infoBuilder.append("\r\n        ");
                        }
                        infoBuilder.append(reqBuilding.getString(Source.FIELD_NAME)).append(" lv.")
                                .append(reqBuilding.getInt(Building.FIELD_LEVEL));
                    }
                }

                List<String> rssList = makeRssList();
                if( rssList != null && rssList.size() > 0) {
                    infoBuilder.append("\r\n - 자원: ");
                    for( String rss : rssList ) {
                        if( rssList.size() > 1 ) {
                            infoBuilder.append("\r\n        ");
                        }
                        infoBuilder.append(rss);
                    }
                }

            } // if detail
            return infoBuilder.toString();
        }
        return null;
    }

    private List<String> makeRssList() {
        List<String> rssList = new ArrayList<>();
        if (foodCost > 0)
            rssList.add("식량 " + RokCalcUtils.quantityToString(foodCost));
        if (woodCost > 0)
            rssList.add("목재 " + RokCalcUtils.quantityToString(woodCost));
        if (stoneCost > 0)
            rssList.add("석재 " + RokCalcUtils.quantityToString(stoneCost));
        if (goldCost > 0)
            rssList.add("금화 " + RokCalcUtils.quantityToString(goldCost));
        if (blueprintCost > 0)
            rssList.add("청사진 " + blueprintCost + "장");
        if (bookCost > 0)
            rssList.add("계약의 서 " + bookCost + "권");
        if (arrowCost > 0)
            rssList.add("저항의 화살 " + arrowCost + "개");

        if( rssList.size() > 0 ) {
            return rssList;
        }

        return null;
    }
}