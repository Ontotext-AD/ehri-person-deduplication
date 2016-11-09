package com.ontotext.ehri.genders.classifier.model;

import com.ontotext.ehri.normalization.USHMMNationalityNormalization;
import types.Alphabet;
import types.ClassificationInstance;
import types.SparseVector;

public class USHMMPersonGenderFeatureExtraction {

    private PersonClassificationInstance instance;

    public USHMMPersonGenderFeatureExtraction(PersonClassificationInstance instance)
    {
        this.instance = instance;
    }

    ClassificationInstance getInstance(Alphabet xA, Alphabet yA) {
        SparseVector sparseVector = getSparseVector(xA);
        return new ClassificationInstance(xA, yA, sparseVector, yA.lookupObject(instance.gender));
    }

    public SparseVector getSparseVector(Alphabet xA) {
        SparseVector sparseVector = new SparseVector();

        for (int i = 1; i <= 4; ++i) {
            sparseVector.add(xA.lookupObject("fns_" + getSuffix(instance.firstName, i)), 1.0d);
            sparseVector.add(xA.lookupObject("lns_" + getSuffix(instance.lastName, i)), 1.0d);
        }

        sparseVector.add(xA.lookupObject("fn_" + instance.firstName), 1.0d);
        sparseVector.add(xA.lookupObject("ln_" + instance.lastName), 1.0d);
        sparseVector.add(xA.lookupObject("n_" + instance.name), 1.0d);

        sparseVector.add(xA.lookupObject("nfn_" + instance.normalizedFirstName), 1.0d);
        sparseVector.add(xA.lookupObject("nln_" + instance.normalizedLastName), 1.0d);
        sparseVector.add(xA.lookupObject("nn_" + instance.normalizedName), 1.0d);

        sparseVector.add(xA.lookupObject("na_" + USHMMNationalityNormalization.normalize(instance.nationality)), 1.0d);

        return sparseVector;
    }

    private static String getSuffix(String stringName, int suffixLength) {
        return stringName.substring(Math.max(0, stringName.length() - suffixLength)).toLowerCase();
    }

}
