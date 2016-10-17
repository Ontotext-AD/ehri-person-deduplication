package com.ontotext.ehri.deduplication.model;

import com.ontotext.ehri.deduplication.measures.JaroWinkler;
import com.ontotext.ehri.deduplication.measures.Levenshtein;
import com.ontotext.ehri.deduplication.measures.USHMMDate;
import types.Alphabet;
import types.ClassificationInstance;
import types.SparseVector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class USHMMClassificationInstanceFactory {

    private static final String NEGATIVE_CLASS = "NO";
    private static final String POSITIVE_CLASS = "YES";
    private static final String NEUTRAL_CLASS = "UNCERTAIN";

    private USHMMPersonStatementsMapHash statementsMap;

    public List<ClassificationInstance> getClassificationInstances(List<USHMMGoldStandardEntry> data, String personStatementsMapCache) {

        Alphabet xA = new Alphabet();
        Alphabet yA = new Alphabet();
        statementsMap = new USHMMPersonStatementsMapHash(data, personStatementsMapCache);
        List<ClassificationInstance> classificationInstanceList = new ArrayList<>();

        classificationInstanceList.addAll(data.stream().map(dataEntry -> getInstance(
                dataEntry.personId1, dataEntry.personId2, dataEntry.label, xA, yA
        )).collect(Collectors.toList()));

        return classificationInstanceList;
    }

    private ClassificationInstance getInstance(String personId1, String personId2, String match, Alphabet xA, Alphabet yA) {
        String label = getLabel(match);
        SparseVector sparseVector = getSparseVector(xA, personId1, personId2);
        return new ClassificationInstance(xA, yA, sparseVector, yA.lookupObject(label));
    }

    private String getLabel(String match) {
        if ("-2".equals(match))
            return NEGATIVE_CLASS;
        else if ("2".equals(match))
            return POSITIVE_CLASS;
        else
            return NEUTRAL_CLASS;
    }

    private SparseVector getSparseVector(Alphabet xA, String personId1, String personId2) {
        SparseVector sparseVector = new SparseVector();

        sparseVector.add(xA.lookupObject("jwf"), JaroWinkler.distance(
                statementsMap.get(personId1, "firstName"),
                statementsMap.get(personId2, "firstName")));

        sparseVector.add(xA.lookupObject("jwl"), JaroWinkler.distance(
                statementsMap.get(personId1, "lastName"),
                statementsMap.get(personId2, "lastName")));

        sparseVector.add(xA.lookupObject("jw"), JaroWinkler.distance(
                statementsMap.get(personId1, "firstName") + " " + statementsMap.get(personId1, "lastName"),
                statementsMap.get(personId2, "firstName") + " " + statementsMap.get(personId2, "lastName")));

        sparseVector.add(xA.lookupObject("jwnf"), JaroWinkler.distance(
                statementsMap.get(personId1, "normalizedFirstName"),
                statementsMap.get(personId2, "normalizedFirstName")));

        sparseVector.add(xA.lookupObject("jwnl"), JaroWinkler.distance(
                statementsMap.get(personId1, "normalizedLastName"),
                statementsMap.get(personId2, "normalizedLastName")));

        sparseVector.add(xA.lookupObject("jwn"), JaroWinkler.distance(
                statementsMap.get(personId1, "normalizedName"),
                statementsMap.get(personId2, "normalizedName")));

        sparseVector.add(xA.lookupObject("db"), USHMMDate.similarity(
                statementsMap.get(personId1, "dateBirth"),
                statementsMap.get(personId2, "dateBirth")));

        sparseVector.add(xA.lookupObject("ldb"), Levenshtein.similarity(
                statementsMap.get(personId1, "dateBirth"),
                statementsMap.get(personId2, "dateBirth")));

        sparseVector.add(xA.lookupObject("lpb"), Levenshtein.similarity(
                statementsMap.get(personId1, "placeBirth"),
                statementsMap.get(personId2, "placeBirth")));

        return sparseVector;

    }
}
