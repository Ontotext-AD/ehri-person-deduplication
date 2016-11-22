package com.ontotext.ehri.genders.classifier.model;

import types.Alphabet;
import types.ClassificationInstance;
import types.SparseVector;

import java.util.ArrayList;
import java.util.List;

public class PersonGenderClassificationInstanceFactory {

    private static final String SPACE = " ";

    public static List<ClassificationInstance> getInstances(List<PersonClassificationInstance> inputData) {
        Alphabet xA = new Alphabet(), yA = new Alphabet();
        List<ClassificationInstance> allData = new ArrayList<>();

        inputData.stream().filter(instance -> instance.normalizedName.contains(SPACE) && instance.normalizedName.length() > 5).forEach(
                instance -> {
                    PersonGenderFeatureExtractor featureExtractor = new PersonGenderFeatureExtractor(instance);
                    SparseVector sparseVector = featureExtractor.getSparseVector(xA);
                    allData.add(new ClassificationInstance(xA, yA, sparseVector, yA.lookupObject(instance.gender)));
                }
        );

        return allData;
    }

}
