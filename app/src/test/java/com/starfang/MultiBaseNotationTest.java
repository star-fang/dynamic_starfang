package com.starfang;

import com.starfang.nlp.lambda.MultiBaseNotation;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class MultiBaseNotationTest {

    private void checkCombinationTime(int[] bases) {
        long start = System.currentTimeMillis();
        long product = 1;
        new MultiBaseNotation(bases).getPositiveBaseDigitsCombination(99999999);
        long end = System.currentTimeMillis();
        for (int base : bases) {
            System.out.print(base + " ");
            product *= base;
        }
        System.out.print("(x" + product + "): ");
        long time = end - start;
        if (time > 999L) {
            System.out.print(time / 1000 + "s ");
        }
        System.out.println(time % 1000 + " ms");
    }

    @Test
    public void combinationTest() {

        /*
        checkCombinationTime(new int[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2});
        checkCombinationTime(new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4});
        checkCombinationTime(new int[]{8, 8, 8, 8, 8, 8, 4});
        checkCombinationTime(new int[]{64, 64, 64, 4});
        System.out.println("---------------------");
        checkCombinationTime(new int[]{64, 64, 64, 4});
        checkCombinationTime(new int[]{8, 8, 8, 8, 8, 8, 4});
        checkCombinationTime(new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4});
        checkCombinationTime(new int[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2});
        System.out.println("---------------------");
        checkCombinationTime(new int[]{4, 4, 4, 4, 4, 4, 4, 4, 4, 4});
        checkCombinationTime(new int[]{8, 8, 8, 8, 8, 8, 4});
        checkCombinationTime(new int[]{64, 64, 64, 4});
        checkCombinationTime(new int[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2});
         */



        for( int[] comb : new MultiBaseNotation(new int[]{9,8}).getPositiveBaseDigitsCombination(400)) {
            for( int a : comb ) {
                System.out.print(a + " ");
            }
            System.out.println("");
        }


    }

}