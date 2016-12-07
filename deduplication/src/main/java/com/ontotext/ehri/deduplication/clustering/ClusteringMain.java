package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.indices.USHMMPersonIndex;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import types.LinearClassifier;
import utils.io.IOUtils;

import java.io.File;
import java.util.List;

public class ClusteringMain {

    private static final double maximumDistanceBetweenTwoPoints = 0.00000001d;
    private static final int minimalPointsInCluster = 2;
    private static final double levenshteinDistance = 0.20d;

    public static void main(String[] args) throws Exception {

        Namespace ns = getNamespace(args);
        if (ns != null) {
            String modelFilepath = ns.getString("modelFilepath");
            String indicesDirectory = ns.getString("indicesDirectory");
            String resultsDirectory = ns.getString("resultsDirectory");
            clusterData(modelFilepath, indicesDirectory, resultsDirectory);
        }
    }

    private static Namespace getNamespace(String[] args) {
        ArgumentParser parser = getArgumentParser();
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
        return ns;
    }

    private static ArgumentParser getArgumentParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("cluster-persons");
        parser.addArgument("modelFilepath").help("Model File");
        parser.addArgument("indicesDirectory").help("Indices Directory");
        parser.addArgument("resultsDirectory").help("Results Directory");
        return parser;
    }

    private static void clusterData(String modelFilepath, String indicesDirectory, String resultsDirectory) throws Exception {
        LinearClassifier linearClassifier = (LinearClassifier) IOUtils.loadModel(new File(modelFilepath).toURI().toURL());
        USHMMPersonIndex personIndex = new USHMMPersonIndex(
                indicesDirectory + "personIdFSA.bin",
                indicesDirectory + "index.bin"
        );
        DBSCANClustering dbScan = new DBSCANClustering(linearClassifier, maximumDistanceBetweenTwoPoints, minimalPointsInCluster, levenshteinDistance, indicesDirectory, personIndex);
        List<Cluster> clusters = dbScan.cluster();
        ClusteringResultsWriter resultsWriter = new ClusteringResultsWriter();
        resultsWriter.printResults(resultsDirectory, clusters, dbScan);
    }

}
