package com.starfang.realm.source.rok;

import com.starfang.realm.source.Source;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ItemMaterial extends RealmObject implements Source {
    public static final String FIELD_RARITY_ID = "rarityId";
    public static final String FIELD_SECONDS = "seconds";
    public static final String FIELD_RARITY = "rarity";
    //{"id":"770011","name":"가죽 (전설)","rarityId":"994","seconds":"276480"},
    @PrimaryKey
    private int id;
    private int rarityId;
    private String name;
    private int seconds;

    private Rarity rarity;

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }

    public Rarity getRarity() {
        return rarity;
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
            case FIELD_RARITY:
                return rarity == null ? null : rarity.getString(Source.FIELD_NAME);
        }
        return null;
    }

    @Override
    public int getInt(String field) {
        switch (field) {
            case FIELD_ID:
                return id;
            case FIELD_RARITY:
            case FIELD_RARITY_ID:
                return rarityId;
            case FIELD_SECONDS:
                return seconds;
        }
        return 0;
    }
}
