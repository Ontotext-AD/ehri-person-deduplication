package com.ontotext.ehri.deduplication.training;

import classification.algorithms.MultithreadedSigmoidPerceptron;
import classification.functions.CompleteFeatureFunction;
import types.Alphabet;
import types.ClassificationInstance;
import types.LinearClassifier;
import utils.MathUtils;
import utils.ReportRenderer;
import utils.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class ClassifierTrainer {

    private static final int SCORES_COUNT = 3;
    private static final int F1_OFFSET = 0;
    private static final int PRECISION_OFFSET = 1;
    private static final int RECALL_OFFSET = 2;

    private static final int COUNT_EXPERIMENTS = 10;

    private static final int DEFAULT_NUMBER_OF_ITERATIONS = 200;
    private static final int DEFAULT_NUMBER_OF_THREADS = 4;

    private Alphabet xA;
    private Alphabet yA;
    private List<ClassificationInstance> allData;

    private Map<Integer, double[]> perExperimentScores;
    private Map<String, double[][]> perLabelScores;

    public ClassifierTrainer(List<ClassificationInstance> allData) {

        this.allData = allData;

        getAlphabetsAndStopGrowth();
        initializeScoresMaps();

    }

    private void getAlphabetsAndStopGrowth() {
        xA = allData.get(0).getxAlphabet();
        xA.stopGrowth();

        yA = allData.get(0).getyAlphabet();
        yA.stopGrowth();
    }

    private void initializeScoresMaps() {
        perExperimentScores = new TreeMap<>();
        perLabelScores = new TreeMap<>();
        for (String label : getSortedLabels())
            perLabelScores.put(label, new double[COUNT_EXPERIMENTS][SCORES_COUNT]);
    }

    private Set<String> getSortedLabels() {
        Set<String> sortedSet = new TreeSet<>();
        for (int i = 0; i < yA.size(); i++)
            sortedSet.add(yA.lookupInt(i));
        return sortedSet;
    }

    public void trainAndSaveModel(String resultsFilename, String modelPath) throws IOException, URISyntaxException {
        LinearClassifier linearClassifier = trainModel();
        reportResultsAndSaveModel(linearClassifier, resultsFilename, modelPath);
    }

    private LinearClassifier trainModel() {
        LinearClassifier linearClassifier = null;
        for (int experiment = 0; experiment < COUNT_EXPERIMENTS; experiment++)
            linearClassifier = getLinearClassifierFromExperiment(experiment);
        return linearClassifier;
    }

    private LinearClassifier getLinearClassifierFromExperiment(int experiment) {
        int splitSize = allData.size() / COUNT_EXPERIMENTS ;
        int offsetSplitLeft = (experiment) * (splitSize);
        int offsetSplitRight = (experiment + 1) * (splitSize);

        List<ClassificationInstance> trainData = getTrainData(offsetSplitLeft, offsetSplitRight);
        List<ClassificationInstance> testData = allData.subList(offsetSplitLeft, offsetSplitRight);

        return trainClassifierAndStoreComputedScores(experiment, trainData, testData);
    }

    private List<ClassificationInstance> getTrainData(int offsetSplitLeft, int offsetSplitRight) {
        List<ClassificationInstance> trainData = new ArrayList<>(allData.subList(0, offsetSplitLeft));
        List<ClassificationInstance> trainRightSide = new ArrayList<>(allData.subList(offsetSplitRight, allData.size()));
        trainData.addAll(trainRightSide);
        return trainData;
    }

    private LinearClassifier trainClassifierAndStoreComputedScores(int experiment,
                                                                   List<ClassificationInstance> trainData, List<ClassificationInstance> testData) {
        LinearClassifier linearClassifier = trainMultithreadedSigmoidPerceptron(
                DEFAULT_NUMBER_OF_ITERATIONS, DEFAULT_NUMBER_OF_THREADS, trainData
        );
        computeAndStoreScores(experiment, testData, linearClassifier);
        return linearClassifier;
    }

    private LinearClassifier trainMultithreadedSigmoidPerceptron(int numberOfIterations, int numberOfThreads,
                                                                 List<ClassificationInstance> trainData) {
        MultithreadedSigmoidPerceptron multithreadedSigmoidPerceptron = new MultithreadedSigmoidPerceptron(
                numberOfIterations, numberOfThreads, xA, yA, new CompleteFeatureFunction(xA, yA)
        );
        return multithreadedSigmoidPerceptron.batchTrain(trainData);
    }

    private void computeAndStoreScores(int experiment, List<ClassificationInstance> testData, LinearClassifier linearClassifier) {
        double[] experimentScores = MathUtils.computeF1(linearClassifier, testData, true);
        for (int i = 0; i < yA.size(); i++)
            getLabelScores(experiment, experimentScores, i);
        perExperimentScores.put(experiment, Arrays.copyOfRange(experimentScores, 0, SCORES_COUNT * 2));
    }

    private void getLabelScores(int experiment, double[] experimentScores, int i) {
        double[][] labelScores = perLabelScores.get(yA.lookupInt(i));
        int labelScoresOffset = (SCORES_COUNT * 2) + (i * SCORES_COUNT);
        labelScores[experiment][F1_OFFSET] = experimentScores[labelScoresOffset + F1_OFFSET];
        labelScores[experiment][PRECISION_OFFSET] = experimentScores[labelScoresOffset + PRECISION_OFFSET];
        labelScores[experiment][RECALL_OFFSET] = experimentScores[labelScoresOffset + RECALL_OFFSET];
    }

    private void reportResultsAndSaveModel(LinearClassifier linearClassifier, String resultsFilename, String modelPath)
            throws IOException, URISyntaxException {
        ReportRenderer.renderResultsSingleLabel(resultsFilename, COUNT_EXPERIMENTS, perLabelScores, perExperimentScores);
        IOUtils.saveModel(linearClassifier, new File(modelPath).toURI().toURL());
    }

}
