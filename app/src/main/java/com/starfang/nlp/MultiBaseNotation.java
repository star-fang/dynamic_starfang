package com.starfang.nlp;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MultiBaseNotation {

    /*
    A23.035 = 10(12) 2(3) 3(4) . 0(2) 3(7) 5(8)
    = 10*(3*4*1) + 2*(4*1) + 3*(1) + 0/2 + 3/(2*7) + 5/(2*7*8)
    -> bases = {4,3,12};
    -> digits = {3,2,10}
    -> negative-bases = { 2,7,8 }
    -> decimal point digits = { 0,3,5 }
     */

    private List<Integer> baseList;
    private List<Integer> negativeBaseList;

    public MultiBaseNotation(@NotNull int[] bases) {
        baseList = new LinkedList<>();
        for (int base : bases) {
            baseList.add(base);
        }
    }

    public MultiBaseNotation( List<Integer> baseLIst ) {
        this.baseList = baseLIst;
    }

    public MultiBaseNotation(@NotNull int[] bases, int[] negativeBases) {
        baseList = new LinkedList<>();
        for (int base : bases) {
            baseList.add(base);
        }

        negativeBaseList = new LinkedList<>();
        for (int negativeBase : negativeBases) {
            negativeBaseList.add(negativeBase);
        }
    }

    /*
    params digits : 12325 => {5,2,3,2,1}
     */
    public int getIntegerValue(int[] digits) throws NullPointerException, IndexOutOfBoundsException {
        if (baseList == null)
            throw new NullPointerException();
        if (digits.length > baseList.size())
            throw new IndexOutOfBoundsException();
        int sumOfProduct = 0;
        for (int i = 0; i < digits.length; i++) {
            sumOfProduct += digits[i] * productOfBases(0, i);
        }
        return sumOfProduct;
    }

    /*
    params digits : 12325 => {5,2,3,2,1}
    decimalPointDigits : .1352 => {1,3,5,2} -1 -2 -3 -4
     */
    public double getValue(int[] digits, int[] decimalPointDigits) throws NullPointerException, IndexOutOfBoundsException {
        int integerValue = getIntegerValue(digits);

        if (negativeBaseList == null)
            throw new NullPointerException();
        if (decimalPointDigits.length > negativeBaseList.size())
            throw new IndexOutOfBoundsException();
        double negativeSumOfProduct = 0;
        int negativeStartIndex = (-1) * decimalPointDigits.length;
        for (int ni = negativeStartIndex; ni < 0; ni++) { // ni : -4 -3 -2 -1
            negativeSumOfProduct += (double) decimalPointDigits[(-1) * ni - 1] / (double) productOfBases(ni, 0);
        }

        return (double) integerValue + negativeSumOfProduct;
    }

    // 2, 3, 4
    // pod 0 = 1
    // pod 1 = 2
    // pod 2 = 2 * 3
    // pod 3 = 2 * 3 * 4
    private int productOfBases(int beginIndex, int endIndex) throws IndexOutOfBoundsException, NullPointerException {
        if (beginIndex > endIndex)
            throw new IndexOutOfBoundsException("beginIndex must equals or be smaller than endIndex");
        if (beginIndex < 0 && negativeBaseList == null)
            throw new IndexOutOfBoundsException("negativeMultiBaseSystem is not set, beginIndex(" + beginIndex + ") must be greater than or equals 0");
        int figures = baseList.size();
        if (endIndex > figures)
            throw new IndexOutOfBoundsException("endIndex(" + endIndex + ") must be smaller than " + figures + 1);

        int product = 1;
        if (negativeBaseList != null && beginIndex < 0) {
            int negativeBeginIndex = (-1) * beginIndex - 1;
            int negativeFigures = negativeBaseList.size();
            if (negativeBeginIndex >= negativeFigures)
                throw new IndexOutOfBoundsException("beginIndex(" + beginIndex + ") must be bigger than " + (-1) * (negativeFigures + 1));
            for (int i = negativeBeginIndex; i >= 0; i--) {
                product *= negativeBaseList.get(i);
            }
            for (int i = 0; i < endIndex; i++) {
                product *= baseList.get(i);
            }
        } // if negativeMultiBaseSystem != null && beginIndex < 0
        else {
            for (int i = beginIndex; i < endIndex; i++) {
                product *= baseList.get(i);
            }
        } // else : calculate only positive section

        return product;
    }


    @NotNull
    private int[] getPositiveBaseDigitsInCase(int caseIndex) throws NullPointerException, IndexOutOfBoundsException {
        int[] digits = new int[baseList.size()];
        for (int i = 0; i < digits.length; i++) {
            digits[i] = caseIndex % productOfBases(0, i + 1) / productOfBases(0, i);
        }
        /*
        n = 0 * pod(3) + d[2] * pod(2) + d[1] * pod(1) + d[0] * pod(0);
        d[2] = n % pos(3) / pod(2)
        d[1] = n % pod(2) / pod(1)
        d[0] = n % pos(1) / pos(0)

        d[i] = n % pod(i+1) /  pod(i)
         */

        return digits;
    }


    public List<int[]> getPositiveBaseDigitsCombination(int nocBoundary) throws NullPointerException, IndexOutOfBoundsException {
        if (baseList == null)
            throw new NullPointerException("please set multi-base system");
        int noc = productOfBases(0, baseList.size());
        if (noc > nocBoundary)
            return null;
        List<int[]> combinationList = new ArrayList<>();
        for (int caseIndex = 0; caseIndex < noc; caseIndex++) {
            combinationList.add(getPositiveBaseDigitsInCase(caseIndex));
        }
        return combinationList;
    }

}
