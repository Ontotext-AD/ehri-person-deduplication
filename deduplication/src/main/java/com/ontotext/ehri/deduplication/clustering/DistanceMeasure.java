package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.model.USHMMClassificationInstance;
import com.ontotext.ehri.deduplication.model.USHMMGoldStandardEntry;
import com.ontotext.ehri.deduplication.model.USHMMPerson;
import types.Alphabet;
import types.LinearClassifier;
import types.SparseVector;

import java.util.Map;

class DistanceMeasure {

    private LinearClassifier model;

    DistanceMeasure(LinearClassifier model)
    {
        this.model = model;
    }

    double compute(USHMMPerson var1, USHMMPerson var2) {
        Alphabet xA = model.getxAlphabet();
        SparseVector sparseVector = new USHMMClassificationInstance(xA, var1, var2).getSparseVector();
        Map<String, Double> scores = model.labelScoreNormalized(sparseVector);
        return 1 - scores.get(USHMMGoldStandardEntry.POSITIVE_CLASS);
    }

}
