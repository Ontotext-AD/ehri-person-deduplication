package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.model.USHMMClassificationInstance;
import com.ontotext.ehri.deduplication.model.USHMMPerson;
import types.Alphabet;
import types.LinearClassifier;
import types.SparseVector;

import java.util.Map;
import java.util.Set;

class DistanceMeasure<T> {

    private LinearClassifier model;

    DistanceMeasure(LinearClassifier model)
    {
        this.model = model;
    }

    double compute(T var1, T var2) {
        Alphabet xA = model.getxAlphabet();
        USHMMPerson p1 = new USHMMPerson((String)((Set)((Map) var1).get("personId")).iterator().next(), (Map<String, Set<String>>) var1);
        USHMMPerson p2 = new USHMMPerson((String)((Set)((Map) var2).get("personId")).iterator().next(), (Map<String, Set<String>>) var2);
        SparseVector sparseVector = new USHMMClassificationInstance(xA, p1, p2).getSparseVector();
        Map<String, Double> scores = model.labelScoreNormalized(sparseVector);
        return 1 - scores.get("YES");
    }

}
