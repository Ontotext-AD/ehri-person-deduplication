package com.ontotext.ehri.deduplication.measures;

/**
 * This class computes the similarity score between two dates.
 * The implementation is specific for the dates coming from
 * the USHMM (United States Holocaust Memorial Museum)
 * HSV (Holocaust Survivors and Victims) Database.
 * <p>
 * Each date is a string in the form of "yyyymmdd".
 * The date can be invalid.
 * Any part can be omitted. In which case it is replaced with zeros (leading zeros are dropped).
 * <p>
 * The score is in the closed interval [0, 1].
 * 1 indicates exact match of the dates with no omitted parts.
 */

public class USHMMDate {

    private static final String DATE_FORMAT = "yyyymmdd";

    private static final int ZERO_SIMILARITY = 0;
    private static final int NORMALIZATION_FACTOR = 10;

    private static final int DELTA = 1;
    private static final int CLOSE_CALENDAR_YEARS_WEIGHT = 5;

    private static final int OMITTED_YEAR_WEIGHT = 6;

    private static final int SAME_YEARS_WEIGHT = 7;
    private static final int SAME_MONTHS_WEIGHT = 2;
    private static final int SAME_DAYS_WEIGHT = 1;

    public static double similarity(String date1, String date2) {
        if (date1.isEmpty() || date2.isEmpty())
            return 0.0d;
        date1 = addLeadingZeros(date1);
        date2 = addLeadingZeros(date2);
        return calculateNormalizedSimilarityScore(date1, date2);
    }

    private static String addLeadingZeros(String date) {
        return new String(new char[DATE_FORMAT.length() - date.length()]).replace("\0", "0") + date;
    }

    private static double calculateNormalizedSimilarityScore(String date1, String date2) {
        int similarityScore = ZERO_SIMILARITY;
        similarityScore += getYearSimilarityScore(date1, date2);
        similarityScore += getMonthSimilarityScore(date1, date2);
        similarityScore += getDaySimilarityScore(date1, date2);
        return (double) similarityScore / NORMALIZATION_FACTOR;
    }

    private static int getYearSimilarityScore(String date1, String date2) {
        int year1 = getYear(date1), year2 = getYear(date2);

        if (isOmitted(year1) || isOmitted(year2))
            return OMITTED_YEAR_WEIGHT;
        else if (yearsMatch(year1, year2))
            return SAME_YEARS_WEIGHT;
        else if (closeCalendarYears(year1, year2))
            return CLOSE_CALENDAR_YEARS_WEIGHT;

        return ZERO_SIMILARITY;
    }

    private static int getYear(String date) {
        return Integer.parseInt(date.substring(0, 4));
    }

    private static boolean isOmitted(int year) {
        return (year == 0);
    }

    private static boolean yearsMatch(int year1, int year2) {
        return (year1 == year2);
    }

    private static boolean closeCalendarYears(int year1, int year2) {
        return (Math.abs(year2 - year1) <= DELTA);
    }

    private static int getMonthSimilarityScore(String date1, String date2) {
        int month1 = getMonth(date1), month2 = getMonth(date2);
        if (monthsMatch(month1, month2))
            return SAME_MONTHS_WEIGHT;
        else
            return ZERO_SIMILARITY;
    }

    private static int getMonth(String date) {
        return Integer.parseInt(date.substring(4, 6));
    }

    private static boolean monthsMatch(int month1, int month2) {
        return (month1 != 0 && month1 == month2);
    }

    private static int getDaySimilarityScore(String date1, String date2) {
        int day1 = getDay(date1), day2 = getDay(date2);
        if (daysMatch(day1, day2))
            return SAME_DAYS_WEIGHT;
        else
            return ZERO_SIMILARITY;
    }

    private static int getDay(String date) {
        return Integer.parseInt(date.substring(6));
    }

    private static boolean daysMatch(int day1, int day2) {
        return (day1 != 0 && day1 == day2);
    }

}
