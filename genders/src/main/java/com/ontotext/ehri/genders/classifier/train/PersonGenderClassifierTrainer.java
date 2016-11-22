package com.ontotext.ehri.genders.classifier.train;

import com.ontotext.ehri.classifier.BaseLinearClassifierTrainer;
import types.ClassificationInstance;
import types.LinearClassifier;
import utils.MathUtils;

import java.util.Arrays;
import java.util.List;

class PersonGenderClassifierTrainer extends BaseLinearClassifierTrainer {

    PersonGenderClassifierTrainer(List<ClassificationInstance> allData) {
        this.allData = allData;
        getAlphabetsAndStopGrowth();
        initializeScoresMaps();
    }

    @Override
    public void computeAndStoreScores(int experiment, List<ClassificationInstance> testData, LinearClassifier linearClassifier) {
        double[] experimentScores = MathUtils.computeF1(linearClassifier, testData, true);
        for (int i = 0; i < yA.size(); i++)
            getLabelScores(experiment, experimentScores, i);
        perExperimentScores.put(experiment, Arrays.copyOfRange(experimentScores, 0, SCORES_COUNT * 2));
    }

}
