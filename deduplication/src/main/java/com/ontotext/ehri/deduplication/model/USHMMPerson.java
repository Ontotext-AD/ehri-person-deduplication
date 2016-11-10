package com.ontotext.ehri.deduplication.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class USHMMPerson {

    private static final String EMPTY_STRING = "";

    private String personId;
    private Map<String, Set<String>> predicateObjectMap;

    USHMMPerson(String personId, Map<String, Set<String>> predicateObjectMap) {
        this.personId = personId;
        this.predicateObjectMap = predicateObjectMap;
    }

    String getStringValue(String predicate) {
        Set<String> objectsSet = predicateObjectMap.get(predicate);
        if (objectsSet != null) {
            Iterator<String> it = objectsSet.iterator();
            if (it.hasNext())
                return it.next();
        }
        return EMPTY_STRING;
    }

    Set<String> getSetOfStringsValues(String predicate) {
        return predicateObjectMap.get(predicate);
    }

    @Override
    public String toString() {
        String personString = "personId : " + personId + " ";
        for (String predicate : predicateObjectMap.keySet()) {
            personString += (" " + predicate + " : ");
            for (String objectValue : predicateObjectMap.get(predicate))
                personString += objectValue;
        }
        return personString;
    }
}
