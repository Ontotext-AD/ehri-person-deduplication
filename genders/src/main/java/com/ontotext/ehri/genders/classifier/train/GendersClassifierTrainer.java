package com.ontotext.ehri.genders.classifier.train;

import com.ontotext.ehri.classifier.BaseClassifierTrainer;
import types.ClassificationInstance;
import types.LinearClassifier;
import utils.MathUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class GendersClassifierTrainer extends BaseClassifierTrainer {

    private static final double TRAIN_TO_ALL_RATIO = 0.9;

    GendersClassifierTrainer(List<ClassificationInstance> allData) {
        this.allData = allData;
        getAlphabetsAndStopGrowth();
        initializeScoresMaps();
    }

    @Override
    public LinearClassifier getLinearClassifierFromExperiment(int experiment) {
        Collections.shuffle(allData);
        int offsetSplit = (int) (TRAIN_TO_ALL_RATIO * allData.size());
        List<ClassificationInstance> trainData = allData.subList(0, offsetSplit);
        List<ClassificationInstance> testData = allData.subList(offsetSplit, allData.size());
        return trainClassifierAndStoreComputedScores(experiment, trainData, testData);
    }

    @Override
    public void computeAndStoreScores(int experiment, List<ClassificationInstance> testData, LinearClassifier linearClassifier) {
        double[] experimentScores = MathUtils.computeF1(linearClassifier, testData, true);
        for (int i = 0; i < yA.size(); i++)
            getLabelScores(experiment, experimentScores, i);
        perExperimentScores.put(experiment, Arrays.copyOfRange(experimentScores, 0, SCORES_COUNT * 2));
    }

}
