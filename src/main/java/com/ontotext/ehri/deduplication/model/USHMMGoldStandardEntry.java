package com.ontotext.ehri.deduplication.model;

class USHMMGoldStandardEntry {

    private static final String NEGATIVE_CLASS = "NO";
    private static final String POSITIVE_CLASS = "YES";
    private static final String NEUTRAL_CLASS = "UNCERTAIN";

    private String personId1;
    private String personId2;
    private String label;

    USHMMGoldStandardEntry(String personId1, String personId2, String label) {
        this.personId1 = personId1;
        this.personId2 = personId2;
        this.label = label;
    }

    String getPersonId1() {
        return personId1;
    }

    String getPersonId2() {
        return personId2;
    }

    String getLabel() {
        if ("-2".equals(label))
            return NEGATIVE_CLASS;
        else if ("2".equals(label))
            return POSITIVE_CLASS;
        else
            return NEUTRAL_CLASS;
    }

}
