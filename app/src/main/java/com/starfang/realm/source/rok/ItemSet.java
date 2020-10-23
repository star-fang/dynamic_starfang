package com.starfang.realm.source.rok;

import com.starfang.realm.primitive.RealmDouble;
import com.starfang.realm.primitive.RealmInteger;
import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class ItemSet extends RealmObject implements Source, SearchNameWithoutBlank {

    public static final String FIELD_ATTRS = "attrs";
    //{"id":"8001","name":"선봉의 세트","nameEng":"Vanguard Set","attrIds":"[1003]","counts":"[2]","vals":"[2]"},

    @PrimaryKey
    private int id;

    @Index
    private String name;
    private String nameWithoutBlank;
    private String nameEng;
    private RealmList<RealmInteger> attrIds;
    private RealmList<RealmInteger> counts;
    private RealmList<RealmDouble> vals;

    private RealmList<Attribute> attrs;

    public void setAttrs(List<Attribute> attrs) {
        this.attrs = new RealmList<>();
        this.attrs.addAll(attrs);
    }

    public RealmList<Attribute> getAttrs() {
        return attrs;
    }

    public RealmList<RealmInteger> getAttrIds() {
        return attrIds;
    }

    public RealmList<RealmInteger> getCounts() {
        return counts;
    }

    public RealmList<RealmDouble> getVals() {
        return vals;
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
        switch (field ) {
            case FIELD_NAME:
                return name;
            case FIELD_NAME_WITHOUT_BLANK:
                return nameWithoutBlank;
            case FIELD_NAME_ENG:
                return nameEng;
        }
        return null;
    }

    @Override
    public int getInt(String field) {
        return 0;
    }
}
