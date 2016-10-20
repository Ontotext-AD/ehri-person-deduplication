package com.ontotext.ehri.deduplication.model;

class USHMMGoldStandardEntry {

    String personId1;
    String personId2;
    String label;

    USHMMGoldStandardEntry(String personId1, String personId2, String label) {
        this.personId1 = personId1;
        this.personId2 = personId2;
        this.label = label;
    }

}
