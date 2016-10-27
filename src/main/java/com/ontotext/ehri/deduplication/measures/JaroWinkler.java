package com.ontotext.ehri.deduplication.measures;

/**
 * The Jaro–Winkler (Winkler, 1990) distance is a measure of similarity between two strings.
 * It is a variant of the Jaro distance metric (Jaro, 1989, 1995), a type of string edit distance.
 * The Jaro–Winkler distance metric is designed and best suited for short strings such as person names.
 * The score is normalized such that 0 equates to no similarity and 1 is an exact match.
 * <p>
 * This implementation is based on the Jaro Winkler similarity algorithm
 * from <a href="https://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance">
 * https://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance</a>.
 */

public class JaroWinkler {

    private static final double DEFAULT_SCALING_FACTOR = 0.1;
    private static final int DEFAULT_DENOMINATOR = 3;

    public static double distance(String s1, String s2) {
        return distance(s1, s2, DEFAULT_SCALING_FACTOR);
    }

    private static double distance(String s1, String s2, double scalingFactor) {

        if (s1 == null || s2 == null)
            return 0.0d;

        String longer, shorter;
        if (s1.length() > s2.length()) {
            longer = s1.toLowerCase();
            shorter = s2.toLowerCase();
        } else {
            longer = s2.toLowerCase();
            shorter = s1.toLowerCase();
        }

        int windowSize = getWindowSize(longer);
        String matches1 = getMatchingCharactersWithin(shorter, longer, windowSize);
        String matches2 = getMatchingCharactersWithin(longer, shorter, windowSize);

        if (matches1.length() == 0 || matches2.length() == 0 || matches1.length() != matches2.length())
            return 0.0;

        return calculateJaroWinklerDistance(s1, s2, scalingFactor, matches1, matches2);
    }

    private static int getWindowSize(String s) {
        return Math.max(0, (s.length() / 2) - 1);
    }

    private static String getMatchingCharactersWithin(String s1, String s2, int windowSize) {
        StringBuilder common = new StringBuilder();
        StringBuilder copy = new StringBuilder(s2);

        for (int i = 0; i < s1.length(); i++)
            appendCharacterIfMatch(s1, s2, windowSize, common, copy, i);

        return common.toString();
    }

    private static void appendCharacterIfMatch(String s1, String s2, int windowSize, StringBuilder common, StringBuilder copy, int i) {
        char ch = s1.charAt(i);
        boolean isMatch = false;

        for (int j = Math.max(0, i - windowSize); !isMatch && j < Math.min(i + windowSize, s2.length()); j++)
            if (copy.charAt(j) == ch) {
                isMatch = true;
                appendCharacter(common, copy, ch, j);
            }
    }

    private static void appendCharacter(StringBuilder common, StringBuilder copy, char ch, int j) {
        common.append(ch);
        copy.setCharAt(j, '*');
    }

    private static double calculateJaroWinklerDistance(String s1, String s2, double scalingFactor, String matches1, String matches2) {
        double jaroDistance = calculateJaroDistance(s1, s2, matches1, matches2);
        int commonPrefixLength = getCommonPrefixLength(s1, s2);
        return jaroDistance + (commonPrefixLength * scalingFactor * (1 - jaroDistance));
    }

    private static double calculateJaroDistance(String s1, String s2, String matches1, String matches2) {
        int halfNumberOfTranspositions = getHalfNumberOfTranspositions(matches1, matches2);
        return ((matches1.length() / (double) s1.length()) + (matches1.length() / (double) s2.length()) + ((
                matches1.length() - halfNumberOfTranspositions) / (double) matches1.length())) / DEFAULT_DENOMINATOR;
    }

    private static int getHalfNumberOfTranspositions(String matches1, String matches2) {
        int numberOfTranspositions = 0;

        for (int i = 0; i < matches1.length(); ++i)
            if (matches1.charAt(i) != matches2.charAt(i))
                ++numberOfTranspositions;

        return numberOfTranspositions / 2;
    }

    private static int getCommonPrefixLength(String s1, String s2) {
        int commonPrefixLength = 0;

        for (int i = 0; i < Math.min(4, Math.min(s1.length(), s2.length())) && s1.charAt(i) == s2.charAt(i); ++i)
            ++commonPrefixLength;

        return commonPrefixLength;
    }
}
