package com.ontotext.ehri.genders.classifier.model;

import types.Alphabet;
import types.ClassificationInstance;

import java.util.ArrayList;
import java.util.List;

public class PersonGenderFeatureExtractor {

    private static final String SPACE = " ";

    public static List<ClassificationInstance> getInstances(List<PersonClassificationInstance> parsedData) {
        Alphabet xA = new Alphabet(), yA = new Alphabet();
        List<ClassificationInstance> allData = new ArrayList<>();

        for (PersonClassificationInstance instance : parsedData)
            if (instance.normalizedName.contains(SPACE) && instance.normalizedName.length() > 5)
                getInstance(xA, yA, allData, instance);

        return allData;
    }

    private static void getInstance(Alphabet xA, Alphabet yA, List<ClassificationInstance> allData, PersonClassificationInstance p) {
        USHMMPersonGenderFeatureExtraction featureExtraction = new USHMMPersonGenderFeatureExtraction(p);
        allData.add(featureExtraction.getInstance(xA, yA));
    }
}
