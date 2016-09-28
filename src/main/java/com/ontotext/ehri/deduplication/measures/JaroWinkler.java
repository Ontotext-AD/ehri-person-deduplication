package com.ontotext.ehri.deduplication.measures;

public class JaroWinkler {

    private static final double DEFAULT_SCALING_FACTOR = 0.1;
    private static final int DEFAULT_DENOMINATOR = 3;

    public static double distance(String s1, String s2) {
        return distance(s1, s2, DEFAULT_SCALING_FACTOR);
    }

    public static double distance(String s1, String s2, double scalingFactor) {
        int windowSize = getWindowSize(s1, s2);
        String matches1 = getMatchingCharactersWithin(s2, s1, windowSize);
        String matches2 = getMatchingCharactersWithin(s1, s2, windowSize);

        if (matches1.length() == 0)
            return 0.0;

        return calculateJaroWinklerDistance(s1, s2, scalingFactor, matches1, matches2);
    }

    private static int getWindowSize(String s1, String s2) {
        String longer = s1.length() >= s2.length() ? s1 : s2;
        return Math.max(0, (longer.length() / 2) - 1);
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
