package com.ontotext.ehri.deduplication.model;

import types.Alphabet;
import types.ClassificationInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates list of classification instances from the parsed data.
 */

class USHMMPersonsFeatureExtractor {

    Map<ClassificationInstance, USHMMPersonPair> getClassificationInstanceUSHMMPersonPairMap(List<USHMMGoldStandardEntry> data, String personStatementsMapCache) {
        Alphabet xA = new Alphabet();
        Alphabet yA = new Alphabet();
        Map<ClassificationInstance, USHMMPersonPair> classificationInstanceUSHMMPersonPairMap = new HashMap<>();
        USHMMPersonStatementsMapHash statementsMap = new USHMMPersonStatementsMapHash(data, personStatementsMapCache);
        for (USHMMGoldStandardEntry entry : data)
            putInstance(classificationInstanceUSHMMPersonPairMap, statementsMap, entry, xA, yA);
        return classificationInstanceUSHMMPersonPairMap;
    }

    private void putInstance(Map<ClassificationInstance, USHMMPersonPair> classificationInstanceUSHMMPersonPairMap,
                             USHMMPersonStatementsMapHash statementsMap, USHMMGoldStandardEntry entry, Alphabet xA, Alphabet yA) {
        USHMMPersonPair personPair = new USHMMPersonPair(entry, statementsMap);
        USHMMClassificationInstance instance = new USHMMClassificationInstance(xA, personPair.getPerson1(), personPair.getPerson2());
        ClassificationInstance classificationInstance = new ClassificationInstance(xA, yA, instance.getSparseVector(), yA.lookupObject(entry.getLabel()));
        classificationInstanceUSHMMPersonPairMap.put(classificationInstance, personPair);
    }

}
