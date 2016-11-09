package com.ontotext.ehri.classifier;

import classification.algorithms.MultithreadedSigmoidPerceptron;
import classification.functions.CompleteFeatureFunction;
import types.Alphabet;
import types.ClassificationInstance;
import types.LinearClassifier;
import utils.ReportRenderer;
import utils.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public abstract class BaseClassifierTrainer {

    public static final int SCORES_COUNT = 3;
    public static final int F1_OFFSET = 0;
    public static final int PRECISION_OFFSET = 1;
    public static final int RECALL_OFFSET = 2;

    public static final int COUNT_EXPERIMENTS = 10;
    public static final int DEFAULT_NUMBER_OF_THREADS = 4;

    public static int numberOfIterations = 200;

    public Alphabet xA;
    public Alphabet yA;
    public List<ClassificationInstance> allData;

    public Map<Integer, double[]> perExperimentScores;
    public Map<String, double[][]> perLabelScores;

    public abstract LinearClassifier getLinearClassifierFromExperiment(int experiment);
    public abstract void computeAndStoreScores(int experiment, List<ClassificationInstance> testData, LinearClassifier linearClassifier);

    public void getAlphabetsAndStopGrowth() {
        xA = allData.get(0).getxAlphabet();
        xA.stopGrowth();

        yA = allData.get(0).getyAlphabet();
        yA.stopGrowth();
    }

    public void initializeScoresMaps() {
        perExperimentScores = new TreeMap<>();
        perLabelScores = new TreeMap<>();
        for (String label : getSortedLabels())
            perLabelScores.put(label, new double[COUNT_EXPERIMENTS][SCORES_COUNT]);
    }

    public Set<String> getSortedLabels() {
        Set<String> sortedSet = new TreeSet<>();
        for (int i = 0; i < yA.size(); i++)
            sortedSet.add(yA.lookupInt(i));
        return sortedSet;
    }

    public void trainAndSaveModel(String resultsFilename, String modelPath) throws IOException, URISyntaxException {
        LinearClassifier linearClassifier = trainModel();
        reportResultsAndSaveModel(linearClassifier, resultsFilename, modelPath);
    }

    public LinearClassifier trainModel() {
        LinearClassifier linearClassifier = null;
        for (int experiment = 0; experiment < COUNT_EXPERIMENTS; experiment++)
            linearClassifier = getLinearClassifierFromExperiment(experiment);
        return linearClassifier;
    }

    public LinearClassifier trainClassifierAndStoreComputedScores(int experiment,
                                                                   List<ClassificationInstance> trainData, List<ClassificationInstance> testData) {
        LinearClassifier linearClassifier = trainMultithreadedSigmoidPerceptron(
                numberOfIterations, DEFAULT_NUMBER_OF_THREADS, trainData
        );
        computeAndStoreScores(experiment, testData, linearClassifier);
        return linearClassifier;
    }

    public LinearClassifier trainMultithreadedSigmoidPerceptron(int numberOfIterations, int numberOfThreads,
                                                                 List<ClassificationInstance> trainData) {
        MultithreadedSigmoidPerceptron multithreadedSigmoidPerceptron = new MultithreadedSigmoidPerceptron(
                numberOfIterations, numberOfThreads, xA, yA, new CompleteFeatureFunction(xA, yA)
        );
        return multithreadedSigmoidPerceptron.batchTrain(trainData);
    }

    public void getLabelScores(int experiment, double[] experimentScores, int i) {
        double[][] labelScores = perLabelScores.get(yA.lookupInt(i));
        int labelScoresOffset = (SCORES_COUNT * 2) + (i * SCORES_COUNT);
        labelScores[experiment][F1_OFFSET] = experimentScores[labelScoresOffset + F1_OFFSET];
        labelScores[experiment][PRECISION_OFFSET] = experimentScores[labelScoresOffset + PRECISION_OFFSET];
        labelScores[experiment][RECALL_OFFSET] = experimentScores[labelScoresOffset + RECALL_OFFSET];
    }

    public void reportResultsAndSaveModel(LinearClassifier linearClassifier, String resultsFilename, String modelPath)
            throws IOException, URISyntaxException {
        ReportRenderer.renderResultsSingleLabel(resultsFilename, COUNT_EXPERIMENTS, perLabelScores, perExperimentScores);
        IOUtils.saveModel(linearClassifier, new File(modelPath).toURI().toURL());
    }

}
