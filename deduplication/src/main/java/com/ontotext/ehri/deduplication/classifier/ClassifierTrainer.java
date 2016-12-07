package com.ontotext.ehri.deduplication.classifier;

import com.ontotext.ehri.classifier.BaseLinearClassifierTrainer;
import com.ontotext.ehri.deduplication.clustering.indices.Predicates;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import types.Alphabet;
import types.ClassificationInstance;
import types.LinearClassifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.*;

class ClassifierTrainer extends BaseLinearClassifierTrainer {

    private static final transient Logger LOG = Logger.getLogger(ClassifierTrainer.class);

    private static final int PRECISION_INDEX = 0;
    private static final int RECALL_INDEX = 1;
    private static final int F1_INDEX = 2;

    private static final int TP_INDEX = 0;
    private static final int FP_INDEX = 1;
    private static final int FN_INDEX = 2;

    private static Map<ClassificationInstance, Pair<List[], List[]>> classificationInstanceUSHMMPersonPairMap;

    ClassifierTrainer(Map<ClassificationInstance, Pair<List[], List[]>> classificationInstanceUSHMMPersonPairMap) {

        this.allData = new ArrayList<>(classificationInstanceUSHMMPersonPairMap.keySet());
        Collections.shuffle(allData);
        this.classificationInstanceUSHMMPersonPairMap = classificationInstanceUSHMMPersonPairMap;
        numberOfIterations = 10000;
        getAlphabetsAndStopGrowth();
        initializeScoresMaps();

    }

    @Override
    public void computeAndStoreScores(int experiment, List<ClassificationInstance> testData, LinearClassifier linearClassifier) {
        double[] experimentScores = computeScores(linearClassifier, testData);
        for (int i = 0; i < yA.size(); i++)
            getLabelScores(experiment, experimentScores, i);
        perExperimentScores.put(experiment, Arrays.copyOfRange(experimentScores, 0, SCORES_COUNT * 2));
    }

    private static double[] computeScores(LinearClassifier h, List<ClassificationInstance> data) {
        int classCount = h.getyAlphabet().size();
        int[] tpInClass = new int[classCount];
        int[] fpInClass = new int[classCount];
        int[] fnInClass = new int[classCount];
        int[] totalsInClass = new int[classCount];
        int[] confusionMatrix = new int[classCount * classCount];

        List<ClassificationInstance> wrongLabeledInstances = computeTpFpFn(h, data, tpInClass, fpInClass, fnInClass, totalsInClass, confusionMatrix);
        logInfoWrongLabeledInstances(h, wrongLabeledInstances);
        double[] micro = computeMicroMeasures(tpInClass, fpInClass, fnInClass);
        double[] accumulated = {0.0D, 0.0D, 0.0D};
        Map<Integer, double[]> perClassResults = computeResultsPerClass(h, tpInClass, fpInClass, fnInClass, accumulated);
        double[] macro = computeMacroMeasures(accumulated, classCount);

        printConfusionMatrixRows(confusionMatrix, h);

        return storeComputedMeasures(macro, micro, perClassResults, classCount);
    }

    private static List<ClassificationInstance> computeTpFpFn(LinearClassifier h, List<ClassificationInstance> data, int[] tpInClass, int[] fpInClass, int[] fnInClass,
                                                              int[] totalsInClass, int[] confusionMatrix) {
        List<ClassificationInstance> falseInstances = new ArrayList<>();
        for (ClassificationInstance inst : data) {
            int label = h.label(inst.x);
            ++totalsInClass[inst.y];
            if (label == inst.y) {
                ++tpInClass[inst.y];
            } else {
                falseInstances.add(inst);
                ++fpInClass[label];
                ++fnInClass[inst.y];
            }
            ++confusionMatrix[inst.y * h.getyAlphabet().size() + label];
        }
        return falseInstances;
    }

    private static void logInfoWrongLabeledInstances(LinearClassifier h, List<ClassificationInstance> wrongLabeledInstances) {
        for (ClassificationInstance inst : wrongLabeledInstances) {
            Alphabet yA = h.getyAlphabet();
            LOG.info("Wrong class : " + yA.lookupInt(h.label(inst.x)) + " Actual class : " + yA.lookupInt(inst.y));
            LOG.info(inst.x.toString());
            logInfoPerson(classificationInstanceUSHMMPersonPairMap.get(inst).getKey());
            logInfoPerson(classificationInstanceUSHMMPersonPairMap.get(inst).getValue());
        }
    }

    private static void logInfoPerson(List[] person) {
        Predicates predicates = new Predicates();
        String personInfo  = "";
        for (int predicate = 0 ; predicate < person.length; ++predicate)
        {
            List<String> predicateValues = person[predicate];
            if (predicateValues.size() > 0) {
                personInfo += predicates.intToString(predicate) + " ";
                for (String value : predicateValues)
                    personInfo += (value + " ");
            }

        }
        LOG.info(personInfo);
    }

    private static double[] computeMicroMeasures(int[] tpInClass, int[] fpInClass, int[] fnInClass) {
        int[] total = computeTotals(tpInClass, fpInClass, fnInClass);
        double[] micro = {0.0D, 0.0D, 0.0D};
        computePrecision(micro, total[TP_INDEX], total[FP_INDEX]);
        computeRecall(micro, total[TP_INDEX], total[FN_INDEX]);
        computeF1(micro, micro[PRECISION_INDEX], micro[RECALL_INDEX]);
        return micro;
    }

    private static int[] computeTotals(int[] tpInClass, int[] fpInClass, int[] fnInClass) {
        int[] total = {0, 0, 0};

        for (int c = 0; c < tpInClass.length; ++c) {
            total[TP_INDEX] += tpInClass[c];
            total[FP_INDEX] += fpInClass[c];
            total[FN_INDEX] += fnInClass[c];
        }

        return total;
    }

    private static void computePrecision(double[] measures, int tp, int fp) {
        if ((double) (tp + fp) != 0.0D)
            measures[PRECISION_INDEX] = tp / (double) (tp + fp);
    }

    private static void computeRecall(double[] measures, int tp, int fn) {
        if ((double) (tp + fn) != 0.0D)
            measures[RECALL_INDEX] = tp / (double) (tp + fn);
    }

    private static void computeF1(double[] measures, double precision, double recall) {
        if ((precision + recall) != 0.0D)
            measures[F1_INDEX] = 2.0D * precision * recall / (precision + recall);
    }

    private static Map<Integer, double[]> computeResultsPerClass(LinearClassifier h, int[] tpInClass, int[] fpInClass, int[] fnInClass, double[] cumul) {
        Map<Integer, double[]> perClassResults = new HashMap<>();
        for (int classIndex = 0; classIndex < tpInClass.length; ++classIndex)
            computeClassResults(h.getyAlphabet().lookupInt(classIndex), tpInClass[classIndex], fpInClass[classIndex], fnInClass[classIndex], cumul, perClassResults, classIndex);
        return perClassResults;
    }

    private static void computeClassResults(String classLabel, int tpInClass, int fpInClass, int fnInClass, double[] accumulated, Map<Integer, double[]> perClassResults, int classIndex) {
        double[] resultsPerClass = {0.0D, 0.0D, 0.0D};
        computePrecision(resultsPerClass, tpInClass, fpInClass);
        computeRecall(resultsPerClass, tpInClass, fnInClass);
        computeF1(resultsPerClass, resultsPerClass[PRECISION_INDEX], resultsPerClass[RECALL_INDEX]);
        accumulateMeasures(accumulated, resultsPerClass);
        perClassResults.put(classIndex, new double[]{resultsPerClass[F1_INDEX], resultsPerClass[PRECISION_INDEX], resultsPerClass[RECALL_INDEX]});
        LOG.info(classLabel + " F1:" + resultsPerClass[F1_INDEX] + " Precision:" + resultsPerClass[PRECISION_INDEX] + " Recall: " + resultsPerClass[RECALL_INDEX]);
    }

    private static void accumulateMeasures(double[] accumulated, double[] resultsPerClass) {
        accumulated[PRECISION_INDEX] += resultsPerClass[PRECISION_INDEX];
        accumulated[RECALL_INDEX] += resultsPerClass[RECALL_INDEX];
        accumulated[F1_INDEX] += resultsPerClass[F1_INDEX];
    }

    private static double[] computeMacroMeasures(double[] cumulative, int classCount) {
        double[] macro = {0.0D, 0.0D, 0.0D};
        macro[PRECISION_INDEX] = cumulative[PRECISION_INDEX] / (double) classCount;
        macro[RECALL_INDEX] = cumulative[RECALL_INDEX] / (double) classCount;
        macro[F1_INDEX] = cumulative[F1_INDEX] / (double) classCount;
        return macro;
    }

    private static void printConfusionMatrixRows(int[] confusionMatrix, LinearClassifier h) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        printConfusionMatrixToStream(outputStream, printStream, confusionMatrix, h);
        closeStreams(outputStream, printStream);
    }

    private static void printConfusionMatrixToStream(ByteArrayOutputStream outputStream, PrintStream printStream,
                                                     int[] confusionMatrix, LinearClassifier h) {
        int[] columnsWidth = computeColumnsWidth(h);
        printHeader(printStream, columnsWidth, h);
        printConfusionMatrixRows(printStream, confusionMatrix, columnsWidth, h);
        LOG.info(new String(outputStream.toByteArray(), Charset.forName("UTF-8")));
    }

    private static int[] computeColumnsWidth(LinearClassifier h) {
        int[] columnsWidth = new int[h.getyAlphabet().size() + 1];
        Arrays.fill(columnsWidth, 6);
        for (int classIndex = 0; classIndex < h.getyAlphabet().size(); ++classIndex)
            computeColumnsWidthForClass(columnsWidth, classIndex, h);
        return columnsWidth;
    }

    private static void computeColumnsWidthForClass(int[] columnsWidth, int classIndex, LinearClassifier h) {
        String classLabel = h.getyAlphabet().lookupInt(classIndex);
        if (classLabel.length() > columnsWidth[0])
            columnsWidth[0] = classLabel.length();
        if (classLabel.length() > columnsWidth[classIndex + 1])
            columnsWidth[classIndex + 1] = classLabel.length();
    }

    private static void printHeader(PrintStream printStream, int[] columnsWidth, LinearClassifier h) {
        printStream.format("%" + columnsWidth[0] + "s ", "");
        printStream.flush();
        printClassLabels(printStream, columnsWidth, h);
        printStream.println(" <-- Predicted");
        printStream.flush();
    }

    private static void printClassLabels(PrintStream printStream, int[] columnsWidth, LinearClassifier h) {
        for (int classIndex = 0; classIndex < h.getyAlphabet().size(); ++classIndex) {
            printStream.format("%" + columnsWidth[classIndex + 1] + "s ", h.getyAlphabet().lookupInt(classIndex));
            printStream.flush();
        }
    }

    private static void printConfusionMatrixRows(PrintStream printStream, int[] confusionMatrix, int[] columnsWidth, LinearClassifier h) {
        for (int classIndex = 0; classIndex < h.getyAlphabet().size(); ++classIndex)
            printClassRow(printStream, confusionMatrix, columnsWidth, classIndex, h);
    }

    private static void printClassRow(PrintStream printStream, int[] confusionMatrix, int[] columnsWidth, int classIndex, LinearClassifier h) {
        printClassLabel(printStream, h.getyAlphabet().lookupInt(classIndex), columnsWidth[0]);
        printClassResults(printStream, confusionMatrix, columnsWidth, classIndex, h);
        printStream.println();
        printStream.flush();
    }

    private static void printClassLabel(PrintStream printStream, String classLabel, int width) {
        printStream.format("%" + width + "s ", classLabel);
        printStream.flush();
    }

    private static void printClassResults(PrintStream printStream, int[] confusionMatrix, int[] columnsWidth, int classIndex, LinearClassifier h) {
        for (int classColumn = 0; classColumn < h.getyAlphabet().size(); ++classColumn) {
            printStream.format("%" + columnsWidth[classColumn + 1] + "d ", confusionMatrix[classIndex * h.getyAlphabet().size() + classColumn]);
            printStream.flush();
        }
    }

    private static void closeStreams(ByteArrayOutputStream outputStream, PrintStream printStream) {
        try {
            printStream.close();
            outputStream.close();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    private static double[] storeComputedMeasures(double[] macro, double[] micro, Map<Integer, double[]> perClassResults, int classCount) {
        double[] measures = new double[(SCORES_COUNT * 2) + (classCount * SCORES_COUNT)];
        storeMacroAndMicroMeasures(macro, micro, measures);
        for (Integer classIndex : perClassResults.keySet())
            storeMeasuresPerClass(measures, perClassResults, classIndex);
        return measures;
    }

    private static void storeMacroAndMicroMeasures(double[] macro, double[] micro, double[] measures) {
        measures[0] = macro[F1_INDEX];
        measures[2] = macro[PRECISION_INDEX];
        measures[4] = macro[RECALL_INDEX];

        measures[1] = micro[F1_INDEX];
        measures[3] = micro[PRECISION_INDEX];
        measures[5] = micro[RECALL_INDEX];
    }

    private static void storeMeasuresPerClass(double[] measures, Map<Integer, double[]> perClassResults, Integer classIndex) {
        double[] resultsPerClass = perClassResults.get(classIndex);
        measures[(SCORES_COUNT * 2) + classIndex * SCORES_COUNT + F1_OFFSET] = resultsPerClass[F1_OFFSET];
        measures[(SCORES_COUNT * 2) + classIndex * SCORES_COUNT + PRECISION_OFFSET] = resultsPerClass[PRECISION_OFFSET];
        measures[(SCORES_COUNT * 2) + classIndex * SCORES_COUNT + RECALL_OFFSET] = resultsPerClass[RECALL_OFFSET];
    }

}
