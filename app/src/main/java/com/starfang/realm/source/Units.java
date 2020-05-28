package com.starfang.realm.source;

import com.starfang.realm.primitive.RealmInteger;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Units extends RealmObject {

    private static final int[] plusCost = {0,3,5,8,10};
    public static final String FIELD_NAME = "name";
    public static final String FIELD_TYPE_ID = "unitTypeId";
    public static final String FIELD_BANNER_ID = "bannerId";
    public static final String FIELD_COST = "cost";
    public static final String FIELD_STR = "str";
    public static final String FIELD_INTEL = "intel";
    public static final String FIELD_CMD = "cmd";
    public static final String FIELD_DEX = "dex";
    public static final String FIELD_LCK = "lck";
    public static final String FIELD_PASSIVE_LISTS = "passiveLists";
    public static final String FIELD_FRIENDSHIP_LIST = "friendshipList";
    public static final String FIELD_BANNER = "banner";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_STAT_SUM = "statSum";

    /*
    Primitive fields
     */
    @PrimaryKey
    private int id;
    @Index
    private String name;
    @Index
    private int unitTypeId;
    private String face;
    private String gender;
    @Index
    private int bannerId;
    @Index
    private int cost;
    private int str;
    private int intel;
    private int cmd;
    private int dex;
    private int lck;
    private RealmList<RealmInteger> passiveListIds;
    private int prefectId;
    private int warlordId;
    @Index
    private String name2;
    private int hp;
    private int mp;
    private int ep;
    private int gold;

    private int personalityId;

    /*
    Fields which are created after download primitive fields
     */
    private int statSum;
    private UnitTypes type;
    private Banners banner;
    private Personality personality;
    private PrefectSkills prefectSkill;
    private WarlordSkills warlordSkill;
    private RealmList<PassiveList> passiveLists;
    private RealmList<Friendships> friendshipList;

    /*
    methods
     */
    public String getName() {
        return name;
    }

    public int getUnitTypeId() {
        return unitTypeId;
    }

    public String getFace() {
        return face;
    }

    public String getGender() {
        return gender;
    }

    public int getBannerId() {
        return bannerId;
    }

    public int getCost() {
        return cost;
    }

    public int getStr() {
        return str;
    }

    public int getIntel() {
        return intel;
    }

    public int getCmd() {
        return cmd;
    }

    public int getDex() {
        return dex;
    }

    public int getLck() {
        return lck;
    }

    public RealmList<RealmInteger> getPassiveListIds() {
        return passiveListIds;
    }

    public int getPrefectId() {
        return prefectId;
    }

    public int getWarlordId() {
        return warlordId;
    }

    public String getName2() {
        return name2;
    }

    public int getHp() {
        return hp;
    }

    public int getMp() {
        return mp;
    }

    public int getEp() {
        return ep;
    }

    public int getGold() {
        return gold;
    }

    public int getPersonalityId() {
        return personalityId;
    }

    /*
    grade: 1 ~ 5
     */
    public int getIntCostByGrade( int grade) throws IndexOutOfBoundsException {
        int gradeIndex = grade - 1;
        if( gradeIndex < 0 || gradeIndex >= plusCost.length )
            throw new IndexOutOfBoundsException("grade index error");
        return cost + plusCost[gradeIndex];
    }

    public String[] getCosts() {
        String[] costs = new String[plusCost.length];
        for( int i = 0; i < plusCost.length; i++ ) {
            costs[i] = String.valueOf(cost + plusCost[i]);
        }
        return costs;
    }

    /*
    runtime field's methods
     */
    public int getStatSum() {
        return statSum;
    }

    public void calcStatSum() {
        this.statSum = str + intel + cmd + dex + lck;
    }

    public RealmList<Friendships> getFriendshipList() {
        return friendshipList;
    }

    public void setFriendshipList(RealmList<Friendships> friendshipList) {
        this.friendshipList = friendshipList;
    }

    public RealmList<PassiveList> getPassiveLists() {
        return passiveLists;
    }

    public void setPassiveLists(RealmList<PassiveList> passiveLists) {
        this.passiveLists = passiveLists;
    }

    public WarlordSkills getWarlordSkill() {
        return warlordSkill;
    }

    public void setWarlordSkill(WarlordSkills warlordSkill) {
        this.warlordSkill = warlordSkill;
    }

    public PrefectSkills getPrefectSkill() {
        return prefectSkill;
    }

    public void setPrefectSkill(PrefectSkills prefectSkill) {
        this.prefectSkill = prefectSkill;
    }

    public Personality getPersonality() {
        return personality;
    }

    public void setPersonality(Personality personality) {
        this.personality = personality;
    }

    public Banners getBanner() {
        return banner;
    }

    public void setBanner(Banners banner) {
        this.banner = banner;
    }

    public UnitTypes getType() {
        return type;
    }

    public void setType(UnitTypes type) {
        this.type = type;
    }
}
