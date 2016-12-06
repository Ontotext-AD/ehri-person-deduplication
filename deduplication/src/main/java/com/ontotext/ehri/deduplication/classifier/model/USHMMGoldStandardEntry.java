package com.ontotext.ehri.deduplication.classifier.model;

public class USHMMGoldStandardEntry {

    public static final String NEGATIVE_CLASS = "NO";
    public static final String POSITIVE_CLASS = "YES";
    public static final String NEUTRAL_CLASS = "UNCERTAIN";

    public String personId1;
    public String personId2;
    public String label;

    USHMMGoldStandardEntry(String personId1, String personId2, String actualLabel) {
        this.personId1 = personId1;
        this.personId2 = personId2;
        this.label = getLabel(actualLabel);
    }

    private String getLabel(String actualLabel) {
        if ("-2".equals(actualLabel))
            return NEGATIVE_CLASS;
        else if ("2".equals(actualLabel))
            return POSITIVE_CLASS;
        else
            return NEUTRAL_CLASS;
    }

}
