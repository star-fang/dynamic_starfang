package com.starfang.utilities;

import android.os.Build;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class VersionUtils {

    @Contract(pure = true)
    public static boolean isKitKat() {
        return Build.VERSION.SDK_INT >= 19;
    }

    @Contract(pure = true)
    public static boolean isJellyBean() {
        return Build.VERSION.SDK_INT >= 16;
    }

    @Contract(pure = true)
    public static boolean isJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= 17;
    }

    @Contract(pure = true)
    public static boolean isJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= 18;
    }

    @Contract(pure = true)
    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= 21;
    }

    @Contract(pure = true)
    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= 23;
    }

    @Contract(pure = true)
    public static boolean isNougat() { return Build.VERSION.SDK_INT >= 24;}

    @Contract(pure = true)
    public static boolean isOreo() { return Build.VERSION.SDK_INT >= 26;}

    @Contract(pure = true)
    public static boolean isPie() { return Build.VERSION.SDK_INT >= 28;}

    @NotNull
    public static String currentVersion() {
        double release=Double.parseDouble(Build.VERSION.RELEASE.replaceAll("(\\d+[.]\\d+)(.*)","$1"));
        String codeName="Unsupported";//below Jelly bean OR above Oreo
        if(release>=4.1 && release<4.4)codeName="Jelly Bean";
        else if(release<5)codeName="KitKat";
        else if(release<6)codeName="Lollipop";
        else if(release<7)codeName="Marshmallow";
        else if(release<8)codeName="Nougat";
        else if(release<9)codeName="Oreo";
        else if(release<10)codeName="Pie";
        return codeName+" v"+release+", API Level: "+Build.VERSION.SDK_INT;
    }

}
