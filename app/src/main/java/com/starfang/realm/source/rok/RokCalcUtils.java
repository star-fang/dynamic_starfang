package com.starfang.realm.source.rok;

import org.apache.commons.lang3.math.NumberUtils;

public class RokCalcUtils {

    public static int siValue(String character, double quantity ) {
        double unit;
        switch (character.toUpperCase()) {
            case "K":
                unit = 1000.0;
                break;
            case "M":
                unit = 1000000.0;
                break;
            case "B":
                unit = 1000000000.0;
                break;
            default:
                unit = 1.0;
        }
        return (int)(quantity * unit);
    }

    public static String quantityToString( int quantity ) {
        StringBuilder quantityBuilder = new StringBuilder();
        if( quantity >= 100000000 ) {
            quantityBuilder.append( quantity / 100000000 ).append("억 ");
            quantity %= 100000000;
        }

        if( quantity >= 10000 ) {
            quantityBuilder.append( quantity / 10000 ).append("만 ");
            quantity %= 10000;
        }

        if( quantity > 0 ) {
            quantityBuilder.append( quantity );
        }

        return quantityBuilder.toString().trim();
    }

    public static String secondsToString( int seconds ) {
        StringBuilder timeBuilder = new StringBuilder();
        if( seconds >= 24 * 60 * 60 ) {
            timeBuilder.append( seconds / (24 * 60 * 60) ).append("일 ");
            seconds %= 24 * 60 * 60;
        }

        if( seconds >= 60 * 60 ) {
            timeBuilder.append( seconds / (60 * 60) ).append("시간 ");
            seconds %= 60 * 60;
        }

        if( seconds >= 60 ) {
            timeBuilder.append( seconds / 60 ).append("분 ");
            seconds %= 60;
        }

        if( seconds > 0 ) {
            timeBuilder.append( seconds ).append("초 ");
        }

        return  timeBuilder.toString().trim();
    }

    public static int stringToSeconds( String time ) {
        if( time == null ) {
            return 0;
        }
        time = time.replaceAll("\\s+", "").trim();
        String units = time.replaceAll( "[0-9]{1,3}", " ").trim();
        String quantities = time.replaceAll( "[^0-9]", " ").trim();


        String[] quantity_list = quantities.split("\\s+");
        String[] unit_list = units.split("\\s+");
        int sum = 0;
        for( int i = 0; i < quantity_list.length; i++ ) {
            int value = NumberUtils.toInt(quantity_list[i],0);
            switch(unit_list[i].toLowerCase()) {
                case "d":
                    sum += value * 24 * 60 * 60;
                    break;
                case "h":
                    sum += value * 60 * 60;
                    break;
                case "m":
                    sum += value * 60;
                    break;
                case "s":
                    sum += value;
                    break;
                default:
            } // switch
        } // for
        return sum;
    }
}
