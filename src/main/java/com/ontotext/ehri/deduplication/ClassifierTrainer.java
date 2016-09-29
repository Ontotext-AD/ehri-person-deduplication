package com.ontotext.ehri.deduplication;

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
    private static final int F1 = 0;
    private static final int PRECISION = 1;
    private static final int RECALL = 2;

    private static final int COUNT_EXPERIMENTS = 10;
    private static final double TRAIN_TO_ALL_RATIO = 0.9;

    private static final int DEFAULT_NUMBER_OF_ITERATIONS = 200;
    private static final int DEFAULT_NUMBER_OF_THREADS = 4;

    private String resultsFilename;
    private String modelPath;

    private Alphabet xA;
    private Alphabet yA;
    private List<ClassificationInstance> allData;

    private Map<Integer, double[]> perExperimentScores;
    private Map<String, double[][]> perLabelScores;

    public ClassifierTrainer(List<ClassificationInstance> allData, String resultsFilename, String modelPath) {

        this.allData = allData;
        this.resultsFilename = resultsFilename;
        this.modelPath = modelPath;

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

    public void trainAndSaveModel() throws IOException, URISyntaxException {
        LinearClassifier linearClassifier = trainModel();
        reportResultsAndSaveModel(linearClassifier);
    }

    private LinearClassifier trainModel() {
        LinearClassifier linearClassifier = null;
        for (int experiment = 0; experiment < COUNT_EXPERIMENTS; experiment++)
            linearClassifier = getLinearClassifierFromExperiment(experiment);
        return linearClassifier;
    }

    private LinearClassifier getLinearClassifierFromExperiment(int experiment) {
        Collections.shuffle(allData);
        int offsetSplit = (int) (TRAIN_TO_ALL_RATIO * allData.size());
        List<ClassificationInstance> trainData = allData.subList(0, offsetSplit);
        List<ClassificationInstance> testData = allData.subList(offsetSplit, allData.size());

        return trainClassifierAndStoreComputedScores(experiment, trainData, testData);
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
        labelScores[experiment][F1] = experimentScores[labelScoresOffset + F1];
        labelScores[experiment][PRECISION] = experimentScores[labelScoresOffset + PRECISION];
        labelScores[experiment][RECALL] = experimentScores[labelScoresOffset + RECALL];
    }

    private void reportResultsAndSaveModel(LinearClassifier linearClassifier)
            throws IOException, URISyntaxException {
        ReportRenderer.renderResultsSingleLabel(resultsFilename, COUNT_EXPERIMENTS, perLabelScores, perExperimentScores);
        IOUtils.saveModel(linearClassifier, new File(modelPath).toURI().toURL());
    }

}
