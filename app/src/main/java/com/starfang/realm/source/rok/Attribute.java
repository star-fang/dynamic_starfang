package com.starfang.realm.source.rok;

import com.starfang.realm.source.SearchNameWithoutBlank;
import com.starfang.realm.source.Source;

import java.text.MessageFormat;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Attribute extends RealmObject implements Source, SearchNameWithoutBlank, Comparable<Attribute> {

    private static final String FIELD_FORM = "form";

    @PrimaryKey
    private int id;

    @Index
    private String name;
    private String nameWithoutBlank;
    private String form;

    @Override
    public int getId() {
        return id;
    }

    public String getFormWithValue( Object... vals) {
        if( form != null ) {
            return MessageFormat.format(form, vals);
        }
        return null;
    }

    @Override
    public String getString(String field) {
        switch (field) {
            case FIELD_NAME:
                return name;
            case FIELD_FORM:
                return form;
            case FIELD_NAME_WITHOUT_BLANK:
                return nameWithoutBlank;
        }
        return null;
    }

    @Override
    public int getInt(String field) {
        return 0;
    }

    @Override
    public void setNameWithoutBlank() {
        this.nameWithoutBlank = name.replaceAll("\\s+", "").trim();
    }

    @Override
    public int compareTo(Attribute attribute) {
        return this.name.compareTo(attribute.name);
    }
}
