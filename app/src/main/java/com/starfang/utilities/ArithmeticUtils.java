package com.starfang.utilities;

public class ArithmeticUtils {
    public static long longSum(long a, long b) throws ArithmeticException{
        long sum;
        if (a > b) {
            // use symmetry to reduce boundary cases
            sum = longSum(b, a);
        } else {
            // assert a <= b
            if (a < 0) {
                if (b < 0) {
                    // check for negative overflow
                    if (Long.MIN_VALUE - b <= a) {
                        sum = a + b;
                    } else {
                        throw new ArithmeticException("sum is less than min value");
                    }
                } else {
                    // opposite sign addition is always safe
                    sum = a + b;
                }
            } else {
                // assert a >= 0
                // assert b >= 0

                // check for positive overflow
                if (a <= Long.MAX_VALUE - b) {
                    sum = a + b;
                } else {
                    throw new ArithmeticException("sum is greater than max value");
                }
            }
        }
        return sum;
    }
}
