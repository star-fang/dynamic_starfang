package com.starfang.realm.source.cat;

import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.Source;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ArtifactsCate extends RealmObject implements Source {

    public static final String FIELD_CATE_SUB = "subCate";
    public static final String FIELD_CATE_MAIN = "mainCate";
    public static final String FIELD_TYPE_ATK = "atkType";
    public static final String FIELD_TYPE_WIS = "wisType";
    public static final String FIELD_TYPE_DEF = "defType";
    public static final String FIELD_TYPE_AGI = "agiType";
    public static final String FIELD_TYPE_MRL = "mrlType";

    @PrimaryKey
    private int id;
    private String subCate;
    private String mainCate;
    private String atkType;
    private String wisType;
    private String defType;
    private String agiType;
    private String mrlType;
    private RealmList<RealmInteger> unitTypeIds;

    private RealmList<UnitTypes> unitTypes;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getString(String field) {
        switch ( field ) {
            case FIELD_CATE_SUB:
                return subCate;
            case FIELD_CATE_MAIN:
                return mainCate;
            case FIELD_TYPE_ATK:
                return atkType;
            case FIELD_TYPE_WIS:
                return wisType;
            case FIELD_TYPE_DEF:
                return defType;
            case FIELD_TYPE_AGI:
                return agiType;
            case FIELD_TYPE_MRL:
                return mrlType;
            default:
                return null;
        }
    }

    public String[] getStatTypeArr() {
        return new String[] {atkType, wisType, defType, agiType, mrlType };
    }

    @Override
    public int getInt(String field) {
        return 0;
    }

    public void setUnitTypes(RealmList<UnitTypes> unitTypes) {
        this.unitTypes = unitTypes;
    }

    public RealmList<RealmInteger> getUnitTypeIds() {
        return unitTypeIds;
    }

    public RealmList<UnitTypes> getUnitTypes() {
        return unitTypes;
    }
}
