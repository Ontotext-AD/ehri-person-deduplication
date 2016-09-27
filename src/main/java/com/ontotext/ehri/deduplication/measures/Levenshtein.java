package com.ontotext.ehri.deduplication.measures;

public class Levenshtein {

    private static final int DELETION_COST = 1;
    private static final int INSERTION_COST = 1;
    private static final int SUBSTITUTION_COST = 1;

    public static int distance(String s, String t) {
        if (isEmptyString(s))
            return t.length();
        if (isEmptyString(t))
            return s.length();
        return calculateDistance(s, t);
    }

    private static boolean isEmptyString(String s) {
        return s.length() == 0;
    }

    private static int calculateDistance(String s, String t) {
        int[] previousRowDistances = new int[t.length() + 1];
        int[] currentRowDistances = new int[t.length() + 1];

        initialize(previousRowDistances);
        calculateIterativeTwoRows(s, t, previousRowDistances, currentRowDistances);

        return currentRowDistances[t.length()];
    }

    private static void initialize(int[] previousRowDistances) {
        for (int i = 0; i < previousRowDistances.length; i++)
            previousRowDistances[i] = DELETION_COST * i;
    }

    private static void calculateIterativeTwoRows(String s, String t, int[] previousRowDistances, int[] currentRowDistances) {
        for (int i = 0; i < s.length(); i++) {
            calculateCurrentRowDistances(s, t, previousRowDistances, currentRowDistances, i);
            copy(currentRowDistances, previousRowDistances);
        }
    }

    private static void calculateCurrentRowDistances(String s, String t, int[] previousRowDistances, int[] currentRowDistances, int i) {
        currentRowDistances[0] = DELETION_COST * (i + 1);
        for (int j = 0; j < t.length(); j++)
            currentRowDistances[j + 1] = Math.min(Math.min(
                    currentRowDistances[j] + INSERTION_COST,
                    previousRowDistances[j + 1] + DELETION_COST),
                    previousRowDistances[j] + (s.charAt(i) == t.charAt(j) ? 0 : SUBSTITUTION_COST));
    }

    private static void copy(int[] currentRowDistances, int[] previousRowDistances) {
        for (int j = 0; j < previousRowDistances.length; j++)
            previousRowDistances[j] = currentRowDistances[j];
    }

}
