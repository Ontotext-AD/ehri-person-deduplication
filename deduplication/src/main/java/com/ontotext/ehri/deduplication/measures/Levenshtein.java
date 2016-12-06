package com.ontotext.ehri.deduplication.measures;

/**
 * The Levenshtein distance is a string metric for measuring the difference between two sequences.
 * Informally, the Levenshtein distance between two words is the minimum number of single-character edits
 * (i.e. insertions, deletions or substitutions) required to change one word into the other.
 * <p>
 * This implementation computes the Levenshtein distance iteratively with two matrix rows.
 * The algorithm is described in <a href="https://en.wikipedia.org/wiki/Levenshtein_distance">
 * https://en.wikipedia.org/wiki/Levenshtein_distance</a>.
 */

public class Levenshtein {

    private static final int DELETION_COST = 1;
    private static final int INSERTION_COST = 1;
    private static final int SUBSTITUTION_COST = 1;

    public static double similarity(String s, String t) {
        if (s.length() == 0 && t.length() == 0)
            return 1;
        else
            return 1 - (distance(s, t) / (double) Math.max(s.length(), t.length()));
    }

    public static int distance(String s, String t) {
        if (s.length() == 0 || t.length() == 0)
            return Math.max(s.length(), t.length());

        int[] previousRowDistances = new int[t.length() + 1];
        int[] currentRowDistances = new int[t.length() + 1];

        for (int i = 0; i < previousRowDistances.length; ++i)
            previousRowDistances[i] = DELETION_COST * i;
        for (int i = 0; i < s.length(); ++i) {
            currentRowDistances[0] = DELETION_COST * (i + 1);
            for (int j = 0; j < t.length(); ++j)
                currentRowDistances[j + 1] = Math.min(Math.min(
                        currentRowDistances[j] + INSERTION_COST,
                        previousRowDistances[j + 1] + DELETION_COST),
                        previousRowDistances[j] + (s.charAt(i) == t.charAt(j) ? 0 : SUBSTITUTION_COST));
            System.arraycopy(currentRowDistances, 0, previousRowDistances, 0, previousRowDistances.length);
        }

        return currentRowDistances[t.length()];
    }

}
