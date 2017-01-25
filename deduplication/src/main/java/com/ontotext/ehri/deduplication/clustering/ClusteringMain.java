package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.classifier.model.USHHMGoldStandardParser;
import com.ontotext.ehri.deduplication.classifier.model.USHMMGoldStandardEntry;
import com.ontotext.ehri.deduplication.indices.USHMMPersonIndex;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import types.LinearClassifier;
import utils.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClusteringMain {

    private static final double maximumDistanceBetweenTwoPoints = 0.1d;
    private static final int minimalPointsInCluster = 2;
    private static final double levenshteinDistance = 0.15d;

    public static void main(String[] args) throws Exception {

        Namespace ns = getNamespace(args);
        if (ns != null) {
            String modelFilepath = ns.getString("modelFilepath");
            String indicesDirectory = ns.getString("indicesDirectory");
            String resultsDirectory = ns.getString("resultsDirectory");
           clusterData(modelFilepath, indicesDirectory, resultsDirectory);
         //  clusterGoldStandardData(modelFilepath, indicesDirectory, resultsDirectory);
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
        resultsWriter.printResults(resultsDirectory, clusters, dbScan, personIndex.size);
    }


    private static void clusterGoldStandardData(String modelFilepath, String indicesDirectory, String resultsDirectory) throws Exception {
        LinearClassifier linearClassifier = (LinearClassifier) IOUtils.loadModel(new File(modelFilepath).toURI().toURL());
        USHMMPersonIndex personIndex = new USHMMPersonIndex(
                indicesDirectory + "personIdFSA.bin",
                indicesDirectory + "index.bin"
        );
        DBSCANClustering dbScan = new DBSCANClustering(linearClassifier, maximumDistanceBetweenTwoPoints, minimalPointsInCluster, levenshteinDistance, indicesDirectory, personIndex);

        Set<String> d = new HashSet<>();
        List<USHMMGoldStandardEntry> data = USHHMGoldStandardParser.parse("/home/nely/workspace/ehri/person-deduplication-task/model/goldStandardOriginalPairs.tsv");
        for (USHMMGoldStandardEntry entry : data) {
            d.add(entry.personId1);
            d.add(entry.personId2);
        }
        List<Cluster> clusters = dbScan.cluster(d);
        ClusteringResultsWriter resultsWriter = new ClusteringResultsWriter();
        resultsWriter.printResults(resultsDirectory, clusters, dbScan, d.size());
    }
}
