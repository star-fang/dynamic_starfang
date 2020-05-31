package com.starfang.realm.primitive;

import java.text.MessageFormat;

import io.realm.RealmObject;

public class RealmIntegerPair extends RealmObject {

    public static final String FIELD_X = "x";
    public static final String FIELD_Y = "y";

    private int x;
    private int y;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return MessageFormat.format("({0}, {1})", x, y);
    }
}
