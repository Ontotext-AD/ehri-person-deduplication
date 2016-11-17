package com.ontotext.ehri.deduplication.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class USHMMPerson implements Serializable {

    private static final String EMPTY_STRING = "";

    private String personId;
    private Map<String, Set<String>> predicateObjectMap;

    public USHMMPerson(String personId, Map<String, Set<String>> predicateObjectMap) {
        this.personId = personId;
        this.predicateObjectMap = predicateObjectMap;
    }

    public String getStringValue(String predicate) {
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

    public boolean equals(Object other) {
        return !((other == null) || (getClass() != other.getClass())) && personId.equals(((USHMMPerson) other).personId);
    }
}
