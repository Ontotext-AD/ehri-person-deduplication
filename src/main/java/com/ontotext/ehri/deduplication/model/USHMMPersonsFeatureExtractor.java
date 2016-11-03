package com.ontotext.ehri.deduplication.model;

import types.Alphabet;
import types.ClassificationInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Creates list of classification instances from the parsed data.
 */

class USHMMPersonsFeatureExtractor {

    private Map<ClassificationInstance, USHMMPersonPair> classificationInstanceUSHMMPersonPairMap;

    List<ClassificationInstance> getClassificationInstances(List<USHMMGoldStandardEntry> data, String personStatementsMapCache) {
        Alphabet xA = new Alphabet();
        Alphabet yA = new Alphabet();
        classificationInstanceUSHMMPersonPairMap = new HashMap<>();
        USHMMPersonStatementsMapHash statementsMap = new USHMMPersonStatementsMapHash(data, personStatementsMapCache);
        List<ClassificationInstance> classificationInstanceList = new ArrayList<>();
        classificationInstanceList.addAll(data.stream().map(entry -> getInstance(statementsMap, entry, xA, yA)).collect(Collectors.toList()));
        return classificationInstanceList;
    }

    private ClassificationInstance getInstance(USHMMPersonStatementsMapHash statementsMap, USHMMGoldStandardEntry entry, Alphabet xA, Alphabet yA) {
        USHMMPersonPair personPair = new USHMMPersonPair(entry, statementsMap);
        USHMMClassificationInstance instance = new USHMMClassificationInstance(xA, personPair.getPerson1(), personPair.getPerson2());
        ClassificationInstance classificationInstance = new ClassificationInstance(xA, yA, instance.getSparseVector(), yA.lookupObject(entry.getLabel()));
        classificationInstanceUSHMMPersonPairMap.put(classificationInstance, personPair);
        return classificationInstance;
    }

    Map<ClassificationInstance, USHMMPersonPair> getClassificationInstanceUSHMMPersonPairMap() {
        return classificationInstanceUSHMMPersonPairMap;
    }
}
