package com.ontotext.ehri.genders.classifier.model;

import com.ontotext.ehri.normalization.USHMMNationalityNormalization;
import types.Alphabet;
import types.SparseVector;

public class PersonGenderFeatureExtractor {

    private PersonClassificationInstance instance;

    public PersonGenderFeatureExtractor(PersonClassificationInstance instance)
    {
        this.instance = instance;
    }

    public SparseVector getSparseVector(Alphabet xA) {
        SparseVector sparseVector = new SparseVector();
        extractNamesSuffixesFeatures(sparseVector, xA);
        extractNamesFeatures(sparseVector, xA);
        extractNormalizedNamesFeatures(sparseVector, xA);
        extractNationalitiesFeatures(sparseVector, xA);
        return sparseVector;
    }

    private void extractNamesSuffixesFeatures(SparseVector sparseVector, Alphabet xA) {
        for (int i = 1; i <= 4; ++i) {
            sparseVector.add(xA.lookupObject("fns_" + getSuffix(instance.firstName, i)), 1.0d);
            sparseVector.add(xA.lookupObject("lns_" + getSuffix(instance.lastName, i)), 1.0d);
        }
    }

    private static String getSuffix(String stringName, int suffixLength) {
        return stringName.substring(Math.max(0, stringName.length() - suffixLength)).toLowerCase();
    }

    private void extractNamesFeatures(SparseVector sparseVector, Alphabet xA) {
        sparseVector.add(xA.lookupObject("fn_" + instance.firstName), 1.0d);
        sparseVector.add(xA.lookupObject("ln_" + instance.lastName), 1.0d);
        sparseVector.add(xA.lookupObject("n_" + instance.name), 1.0d);
    }

    private void extractNormalizedNamesFeatures(SparseVector sparseVector, Alphabet xA) {
        sparseVector.add(xA.lookupObject("nfn_" + instance.normalizedFirstName), 1.0d);
        sparseVector.add(xA.lookupObject("nln_" + instance.normalizedLastName), 1.0d);
        sparseVector.add(xA.lookupObject("nn_" + instance.normalizedName), 1.0d);
    }

    private void extractNationalitiesFeatures(SparseVector sparseVector, Alphabet xA) {
        for (String nationality : instance.nationalitiesSet)
            sparseVector.add(xA.lookupObject("na_" + USHMMNationalityNormalization.normalize(nationality)), 1.0d);
    }

}
