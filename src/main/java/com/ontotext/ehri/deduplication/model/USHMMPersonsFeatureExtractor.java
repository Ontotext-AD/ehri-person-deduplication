package com.ontotext.ehri.deduplication.model;

import javafx.util.Pair;
import types.Alphabet;
import types.ClassificationInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates list of classification instances from the parsed data.
 */

class USHMMPersonsFeatureExtractor {

    Map<ClassificationInstance, Pair<USHMMPerson, USHMMPerson>> getClassificationInstanceUSHMMPersonPairMap(List<USHMMGoldStandardEntry> data, String personStatementsMapCache) {
        Alphabet xA = new Alphabet(), yA = new Alphabet();
        Map<ClassificationInstance, Pair<USHMMPerson, USHMMPerson>> classificationInstanceUSHMMPersonPairMap = new HashMap<>();
        USHMMPersonStatementsMapHash statementsMap = new USHMMPersonStatementsMapHash(data, personStatementsMapCache);
        for (USHMMGoldStandardEntry entry : data)
            putInstance(classificationInstanceUSHMMPersonPairMap, statementsMap, entry, xA, yA);
        return classificationInstanceUSHMMPersonPairMap;
    }

    private void putInstance(Map<ClassificationInstance, Pair<USHMMPerson, USHMMPerson>> classificationInstanceUSHMMPersonPairMap, USHMMPersonStatementsMapHash statementsMap, USHMMGoldStandardEntry entry, Alphabet xA, Alphabet yA) {
        Pair<USHMMPerson, USHMMPerson> personPair = new Pair<>(statementsMap.getPerson(entry.personId1), statementsMap.getPerson(entry.personId2));
        USHMMClassificationInstance instance = new USHMMClassificationInstance(xA, personPair.getKey(), personPair.getValue());
        ClassificationInstance classificationInstance = new ClassificationInstance(xA, yA, instance.getSparseVector(), yA.lookupObject(entry.getLabel()));
        classificationInstanceUSHMMPersonPairMap.put(classificationInstance, personPair);
    }

}
