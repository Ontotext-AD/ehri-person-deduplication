package com.ontotext.ehri.deduplication.model;

import types.Alphabet;
import types.ClassificationInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates list of classification instances from the parsed data.
 */

class USHMMPersonsFeatureExtractor {

    List<ClassificationInstance> getClassificationInstances(List<USHMMGoldStandardEntry> data, String personStatementsMapCache) {
        Alphabet xA = new Alphabet();
        Alphabet yA = new Alphabet();
        USHMMPersonStatementsMapHash statementsMap = new USHMMPersonStatementsMapHash(data, personStatementsMapCache);
        List<ClassificationInstance> classificationInstanceList = new ArrayList<>();
        classificationInstanceList.addAll(data.stream().map(entry -> getInstance(statementsMap, entry, xA, yA)).collect(Collectors.toList()));
        return classificationInstanceList;
    }

    private ClassificationInstance getInstance(USHMMPersonStatementsMapHash statementsMap, USHMMGoldStandardEntry entry, Alphabet xA, Alphabet yA) {
        USHMMPersonPair personPair = new USHMMPersonPair(entry, statementsMap);
        USHMMPerson person1 = personPair.getPerson1();
        USHMMPerson person2 = personPair.getPerson2();
        USHMMClassificationInstance instance = new USHMMClassificationInstance(xA, person1, person2);
        return new ClassificationInstance(xA, yA, instance.getSparseVector(), yA.lookupObject(entry.getLabel()));

    }

}
