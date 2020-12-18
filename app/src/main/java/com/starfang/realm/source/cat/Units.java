package com.starfang.realm.source.cat;

import android.text.TextUtils;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.Source;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Units extends RealmObject implements Source {

    private static final String[] STAT_NAMES = {"무력", "지력", "통솔", "민첩", "행운"};
    private static final int[] PASSIVE_LEVELS = {30, 50, 70, 90};
    private static final int[] PLUS_COSTS = {0, 3, 5, 8, 10};
    public static final String FIELD_TYPE_ID = "unitTypeId";
    public static final String FIELD_FACE = "face";
    public static final String FIELD_GENDER = "gender";
    public static final String FIELD_BANNER_ID = "bannerId";
    public static final String FIELD_COST = "cost";
    public static final String FIELD_STR = "str";
    public static final String FIELD_INTEL = "intel";
    public static final String FIELD_CMD = "cmd";
    public static final String FIELD_DEX = "dex";
    public static final String FIELD_LCK = "lck";
    public static final String FIELD_PASV_LIST_IDS = "passiveListIds";
    public static final String FIELD_PREFECT_ID = "prefectId";
    public static final String FIELD_WARLORD_ID = "warlordId";
    public static final String FIELD_HP = "hp";
    public static final String FIELD_MP = "mp";
    public static final String FIELD_EP = "ep";
    public static final String FIELD_GOLD = "gold";
    public static final String FIELD_PERSONALITY_ID = "personalityId";

    public static final String FIELD_PASSIVE_LISTS = "passiveLists";
    public static final String FIELD_FRIENDSHIP_LIST = "friendshipList";
    public static final String FIELD_BANNER = "banner";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_STAT_SUM = "statSum";
    public static final String FIELD_COUNT_NAMESAKE = "namesakeCount";
    public static final String FIELD_TYPE_AND_NAME = "typeAndName";

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
    private int namesakeCount;
    @Index
    private String typeAndName;
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

    public void setNamesakeCount(int count) {
        this.namesakeCount = count;
    }

    public void updateTypeAndName() {
        if( this.type != null ) {
            String typeName = type.getString(Source.FIELD_NAME);
            if( typeName != null && this.name != null ) {
                this.typeAndName = typeName + this.name;
            }
        }
    }

    public int getCost() {
        return cost;
    }

    public int getUnitTypeId() {
        return unitTypeId;
    }

    public int getBannerId() {
        return bannerId;
    }

    public int getPersonalityId() {
        return personalityId;
    }

    public int getPrefectId() {
        return prefectId;
    }

    public int getWarlordId() {
        return warlordId;
    }

    public RealmList<RealmInteger> getPassiveListIds() {
        return passiveListIds;
    }

    public void addFriendship( Friendships friendship ) {
        if( friendshipList == null ) {
            friendshipList = new RealmList<>();
        }
        friendshipList.add(friendship);
    }

    /*
    grade: 1 ~ 5
     */
    public int getIntCostByGrade(int grade) throws IndexOutOfBoundsException {
        int gradeIndex = grade - 1;
        if (gradeIndex < 0) {
            throw new IndexOutOfBoundsException("grade index error");
        }
        if (gradeIndex >= PLUS_COSTS.length) {
            gradeIndex = PLUS_COSTS.length - 1;
        }
        return cost + PLUS_COSTS[gradeIndex];
    }

    public String[] getCosts() {
        String[] costs = new String[PLUS_COSTS.length];
        for (int i = 0; i < PLUS_COSTS.length; i++) {
            costs[i] = String.valueOf(cost + PLUS_COSTS[i]);
        }
        return costs;
    }

    /*
    runtime field's methods
     */

    public int getPassiveLevel(int passiveListId) {
        if (passiveListIds != null) {
            for (int i = 0; i < passiveListIds.size(); i++) {
                RealmInteger realmInteger = passiveListIds.get(i);
                if (realmInteger != null
                        && realmInteger.getValue() == passiveListId
                        && i < PASSIVE_LEVELS.length) {
                    return PASSIVE_LEVELS[i];
                }
            }
        }
        return 0;
    }

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

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getString(String field) {
        switch (field) {
            case FIELD_ID:
                return String.valueOf(id);
            case FIELD_NAME:
                return name;
            case FIELD_NAME2:
                return name2;
            case FIELD_TYPE_AND_NAME:
                return typeAndName;
            case FIELD_TYPE_ID:
                return String.valueOf(unitTypeId);
            case FIELD_FACE:
                return face;
            case FIELD_GENDER:
                return gender;
            case FIELD_BANNER_ID:
                return String.valueOf(bannerId);
            case FIELD_COST:
                return String.valueOf(cost);
            case FIELD_STR:
                return String.valueOf(str);
            case FIELD_INTEL:
                return String.valueOf(intel);
            case FIELD_CMD:
                return String.valueOf(cmd);
            case FIELD_DEX:
                return String.valueOf(dex);
            case FIELD_LCK:
                return String.valueOf(lck);
            case FIELD_PASV_LIST_IDS:
                return passiveListIds == null ? null : TextUtils.join(", ", passiveListIds);
            case FIELD_PREFECT_ID:
                return String.valueOf(prefectId);
            case FIELD_WARLORD_ID:
                return String.valueOf(warlordId);
            case FIELD_HP:
                return String.valueOf(hp);
            case FIELD_MP:
                return String.valueOf(mp);
            case FIELD_EP:
                return String.valueOf(ep);
            case FIELD_GOLD:
                return String.valueOf(gold);
            case FIELD_PERSONALITY_ID:
                return String.valueOf(personalityId);
            case FIELD_TYPE:
                if (type != null) {
                    return type.getString(UnitTypes.FIELD_NAME);
                } else {
                    return null;
                }
            default:
                return null;

        }
    }

    @Override
    public int getInt(String field) {
        switch (field) {
            case FIELD_ID:
                return id;
            case FIELD_TYPE_ID:
                return unitTypeId;
            case FIELD_BANNER_ID:
                return bannerId;
            case FIELD_COST:
                return cost;
            case FIELD_STR:
                return str;
            case FIELD_INTEL:
                return intel;
            case FIELD_CMD:
                return cmd;
            case FIELD_DEX:
                return dex;
            case FIELD_LCK:
                return lck;
            case FIELD_PREFECT_ID:
                return prefectId;
            case FIELD_WARLORD_ID:
                return warlordId;
            case FIELD_HP:
                return hp;
            case FIELD_MP:
                return mp;
            case FIELD_EP:
                return ep;
            case FIELD_GOLD:
                return gold;
            case FIELD_PERSONALITY_ID:
                return personalityId;
            case FIELD_COUNT_NAMESAKE:
                return namesakeCount;
            default:
                return -1;
        }
    }

    private int[] getStats() {
        return new int[]{str, intel, cmd, dex, lck};
    }

    public static class UnitAccumulation {
        private int count;
        private int cost;
        private final Map<Friendships, Integer> friendshipCount;
        private final List<Friendships> activatedFriendships;

        public UnitAccumulation() {
            count = 0;
            cost = 0;
            friendshipCount = new HashMap<>();
            activatedFriendships = new ArrayList<>();
        }

        public void addCost(int cost) {
            this.cost += cost;
        }

        public int countUp() {
            return ++count;
        }

        public void addFriendship(Friendships friendship) {
            Integer currentCount = friendshipCount.get(friendship);
            if (currentCount == null) {
                currentCount = 1;
            } else {
                currentCount++;
            }
            friendshipCount.put(friendship, currentCount);

            if (!activatedFriendships.contains(friendship) && friendship.activated( currentCount)) {
                activatedFriendships.add( friendship );
            }
        }

        public int getCost() {
            return cost;
        }

        public int getCount() {
            return count;
        }

        public int getFriendshipUnitCount( Friendships friendship ) {
            Integer count = friendshipCount.get(friendship);
            return count == null ? 0 : count;
        }

        public List<Friendships> getActivatedFriendships() {
            return activatedFriendships;
        }
    }

    public String getInfo(boolean detail, UnitAccumulation unitAccu ) {
        StringBuilder infoBuilder = new StringBuilder();
        String typeName = type.getString(Source.FIELD_NAME);

        if( !detail ) {
            infoBuilder.append("\r\n");
        }

        if( unitAccu != null ) {
            unitAccu.addCost( (cost + 10 ) );
            int number = unitAccu.countUp();
            if( !detail ) {
                infoBuilder.append(number).append(". ");
            }
            if( friendshipList != null ) {
                for( Friendships friendship : friendshipList ) {
                    unitAccu.addFriendship( friendship );
                }
            }
        }
        infoBuilder.append(detail ? typeName : StringUtils.rightPad(typeName, 4, '　'))
                .append(" ").append(detail ? name : StringUtils.rightPad(name, 4, '　'))
        .append(detail? "" : (cost + 10) );



        if( detail ) {
            //if (!StringUtils.isEmpty(name2)) {infoBuilder.append(" a.k.a. ").append(name2); }
            infoBuilder.append("\r\n계보: ").append(banner.getString(Source.FIELD_NAME));
            if (friendshipList != null && friendshipList.size() > 0) {
                for (int i = 0; i < friendshipList.size(); i++) {
                    infoBuilder.append("\r\n인연").append(friendshipList.size() > 1 ? (i + 1) : "").append(": ")
                            .append(friendshipList.get(i).getString(Source.FIELD_NAME));
                }
            }
            int[] stats = getStats();
            infoBuilder.append("\r\n");
            for (int i = 0; i < stats.length; i++) {
                if (i < STAT_NAMES.length) {
                    String statNameInitial = STAT_NAMES[i].substring(0, 1);
                    infoBuilder.append(statNameInitial).append(stats[i]).append(i > 0 ? " " : "");
                }
            }

            if (passiveLists != null) {
                for (int i = 0; i < PASSIVE_LEVELS.length; i++) {
                    if (i < passiveLists.size()) {
                        PassiveList passiveList = passiveLists.get(i);
                        if (passiveList != null) {
                            String passiveAndVal = passiveList.getPassiveAndVal();
                            if( passiveAndVal != null ) {
                                infoBuilder.append("\r\nLv").append(PASSIVE_LEVELS[i]).append(": ")
                                        .append(passiveAndVal);
                            }
                        }
                    }
                }
            }

            if (prefectSkill != null) {
                infoBuilder.append("\r\n태수: ").append(prefectSkill.getString(Source.FIELD_NAME));
            }

            if (warlordSkill != null) {
                infoBuilder.append("\r\n군주: ").append(warlordSkill.getString(Source.FIELD_NAME));
            }

            if (personality != null) {
                infoBuilder.append("\r\n성격: ").append(personality.getString(Source.FIELD_NAME));
            }
        }
        return infoBuilder.toString();
    }
}
