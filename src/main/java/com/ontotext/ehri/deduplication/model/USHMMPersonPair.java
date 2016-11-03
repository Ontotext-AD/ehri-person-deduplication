package com.ontotext.ehri.deduplication.model;

import java.util.ArrayList;
import java.util.List;

public class USHMMPersonPair {

    private String personId1;
    private String personId2;
    private USHMMPersonStatementsMapHash statementsMap;

    USHMMPersonPair(USHMMGoldStandardEntry entry, USHMMPersonStatementsMapHash statementsMap) {
        this.personId1 = entry.personId1;
        this.personId2 = entry.personId2;
        this.statementsMap = statementsMap;
    }

    USHMMPerson getPerson1() {
        return getPerson(personId1);
    }

    USHMMPerson getPerson2() {
        return getPerson(personId2);
    }

    private USHMMPerson getPerson(String personId) {
        List<String> args  = new ArrayList<>();
        for (String predicate : USHMMPersonStatementsMapHash.PREDICATE_NAMES_ARRAY)
            args.add(statementsMap.get(personId, predicate));
        return new USHMMPerson(args.toArray(new String[0]));
    }

    @Override
    public String toString() {
        String result = toStringPerson(personId1);
        result += toStringPerson(personId2);
        return result;
    }

    private String toStringPerson(String personId) {
        String result = "personId : " + personId + " ";
        for (String predicate : USHMMPersonStatementsMapHash.PREDICATE_NAMES_ARRAY)
            result +=  predicate + " : " + statementsMap.get(personId, predicate) + " ";
        return result;
    }
}
