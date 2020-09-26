package com.starfang.realm.source.rok;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;

public class Commanders extends RealmObject implements Source, SearchNameWithoutBlank {

    public static final String FIELD_NICKNAME = "nickname";
    public static final String FIELD_GAIN = "gain";
    public static final String FIELD_GAIN_DAYS = "gainDays";
    public static final String FIELD_CIVIL = "civilization";
    public static final String FIELD_CIVIL_ID = "civilId";
    public static final String FIELD_SPECS = "specifications";
    public static final String FIELD_SKILLS = "skills";
    public static final String FIELD_RARITY = "rarity";

    @PrimaryKey
    private int id;

    private String name;
    private String rarity;
    private String shortName;
    private String nickname;
    private String gain;
    private int gainDays;
    private int civilId;
    private RealmList<RealmInteger> specIds;
    private RealmList<RealmInteger> skillIds;

    // runtime field
    private Civilizations civilization;
    private RealmList<Specifications> specifications;
    private RealmList<Skills> skills;

    private String nameWithoutBlank;

    public void setCivilization(Civilizations civilization) {
        this.civilization = civilization;
    }

    public void setSpecifications(List<Specifications> specifications) {
        this.specifications = new RealmList<>();
        this.specifications.addAll(specifications);
    }

    public void setSkills(List<Skills> skills) {
        this.skills = new RealmList<>();
        this.skills.addAll(skills);
    }

    public RealmList<Skills> getSkills() {
        return skills;
    }

    public RealmList<RealmInteger> getSpecIds() {
        return specIds;
    }

    public RealmList<RealmInteger> getSkillIds() {
        return skillIds;
    }

    @Override
    public void setNameWithoutBlank() {
        this.nameWithoutBlank = name.replaceAll("\\s+","").trim();
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
            case FIELD_RARITY:
                return rarity;
            case FIELD_NAME:
                return name;
            case FIELD_NAME_WITHOUT_BLANK:
                return nameWithoutBlank;
            case FIELD_CIVIL:
                if( civilization == null ) {
                    return null;
                }
                return civilization.getString(Civilizations.FIELD_NAME);
            case FIELD_SPECS:
                StringBuilder specBuilder = new StringBuilder();
                for( Specifications spec : specifications.sort(Specifications.FIELD_POSITION, Sort.ASCENDING) ) {
                    specBuilder.append("특성").append(spec.getInt(Specifications.FIELD_POSITION) + 1)
                            .append(": ").append(spec.getString(Specifications.FIELD_NAME)).append("\r\n");
                }
                return specBuilder.toString();
            case FIELD_SKILLS:
                StringBuilder skillBuilder = new StringBuilder();
                for( Skills skill : skills ) {
                    skillBuilder.append("스킬").append( (skills.indexOf(skill) + 1) )
                            .append(": ").append(skill.getString(Skills.FIELD_NAME)).append("\r\n");
                }
                return skillBuilder.toString();
            case FIELD_GAIN:
                return gain;
            case FIELD_GAIN_DAYS:
                if( gainDays > 0 ) {
                    return String.valueOf(gainDays);
                }
            case FIELD_NICKNAME:
                return nickname;
            default:
                return null;
        }
    }

    @Override
    public int getInt(String field) {
        switch ( field ) {
            case FIELD_ID:
                return id;
            case FIELD_CIVIL:
            case FIELD_CIVIL_ID:
                return civilId;
            case FIELD_GAIN_DAYS:
                return gainDays;
        }
        return 0;
    }
}
