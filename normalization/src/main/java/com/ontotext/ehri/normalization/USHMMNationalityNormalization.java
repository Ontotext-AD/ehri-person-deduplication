package com.ontotext.ehri.normalization;

public class USHMMNationalityNormalization {
    public static String normalize(String nationality) {
        String normalizedNationality = nationality;
        int index = normalizedNationality.indexOf("(\"");
        if (index != -1)
            normalizedNationality = normalizedNationality.substring(0, index).trim();
        return normalizedNationality;
    }
}
