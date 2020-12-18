package com.starfang.realm.source.rok;

import com.starfang.realm.primitive.RealmDouble;
import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Item extends RealmObject implements Source, SearchNameWithoutBlank {
    public static final String FIELD_RARITY_ID = "rarityId";
    public static final String FIELD_RARITY = "rarity";
    public static final String FIELD_CATEGORY_ID = "categoryId";
    public static final String FIELD_LEVEL = "level";
    public static final String FIELD_ATTR_IDS = "attrIds";
    public static final String FIELD_ATTR_VALS = "attrVals";
    public static final String FIELD_MATERIAL_IDS = "materialIds";
    public static final String FIELD_MATERIAL_COUNTS = "materialCounts";
    public static final String FIELD_GOLD = "gold";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_SET_ID = "setId";
    public static final String FIELD_ITEM_SET = "itemSet";
    public static final String FIELD_ATTRS = "attrs";
    public static final String FIELD_MATERIALS = "materials";
    public static final String FIELD_CATEGORY = "category";

    @PrimaryKey
    private int id;
    private String name;
    private String nameEng;
    private int rarityId;
    private int categoryId;
    private int level;
    private RealmList<RealmInteger> attrIds;
    private RealmList<RealmDouble> attrVals;
    private RealmList<RealmInteger> materialIds;
    private RealmList<RealmInteger> materialCounts;
    private int gold;
    private String description;
    private int setId;


    //runtime fields;
    private String nameWithoutBlank;
    private RealmList<Attribute> attrs;
    private RealmList<ItemMaterial> materials;
    private ItemSet itemSet;
    private ItemCategory category;
    private Rarity rarity;

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public RealmList<RealmInteger> getAttrIds() {
        return attrIds;
    }

    public RealmList<RealmDouble> getAttrVals() {
        return attrVals;
    }

    public RealmList<RealmInteger> getMaterialIds() {
        return materialIds;
    }

    public RealmList<RealmInteger> getMaterialCounts() {
        return materialCounts;
    }

    @Override
    public void setNameWithoutBlank() {
        this.nameWithoutBlank = name.replaceAll("\\s+", "").trim();
    }

    public void setAttrs(RealmList<Attribute> attrs) {
        this.attrs = attrs;
    }

    public RealmList<Attribute> getAttrs() {
        return attrs;
    }

    public void setMaterials(RealmList<ItemMaterial> materials) {
        this.materials = materials;
    }

    public RealmList<ItemMaterial> getMaterials() {
        return materials;
    }

    public void setItemSet(ItemSet itemSet) {
        this.itemSet = itemSet;
    }

    public ItemSet getItemSet() {
        return itemSet;
    }

    public String[] getValsStrOfAttr( Integer attrId ) {

        if( attrIds.size() == 1 && attrIds.size() < attrVals.size() ) {
            RealmInteger oneId = attrIds.first();
            if( oneId != null && oneId.getValue() == attrId ) {
                String[] vals = new String[attrVals.size()];
                for( int i = 0; i < vals.length; i++ ) {
                    RealmDouble valObj = attrVals.get(i);
                    if( valObj != null ) {
                        vals[i] = String.valueOf(valObj.getValue()).replace(".0","");
                    }
                }
                return vals;
            }
        }


        for( int i = 0; i < attrIds.size(); i++ ) {
            RealmInteger idObj = attrIds.get(i);
            if( idObj != null && idObj.getValue() == attrId ) {
                RealmDouble valObj = attrVals.get(i);
                if( valObj != null ) {
                    String[] vals = new String[1];
                    vals[0] = String.valueOf(valObj.getValue()).replace(".0","");
                    return vals;
                }

            }
        }
       return null;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getString(String field) {
        switch (field) {
            case FIELD_NAME:
                return name;
            case FIELD_NAME_WITHOUT_BLANK:
                return nameWithoutBlank;
            case FIELD_NAME_ENG:
                return nameEng;
            case FIELD_DESCRIPTION:
                return description;
            case FIELD_CATEGORY:
                return category == null ? null : category.getString(Source.FIELD_NAME);
            case FIELD_RARITY:
                return rarity == null ? null : rarity.getString(Source.FIELD_NAME);
            case FIELD_ITEM_SET:
                return itemSet == null ? null : itemSet.getString(Source.FIELD_NAME);
        }
        return null;
    }

    @Override
    public int getInt(String field) {
        switch (field) {
            case FIELD_LEVEL:
                return level;
            case FIELD_GOLD:
                return gold;
            case FIELD_CATEGORY_ID:
                return categoryId;
            case FIELD_RARITY_ID:
                return rarityId;
            case FIELD_SET_ID:
                return setId;
        }
        return 0;
    }
}
