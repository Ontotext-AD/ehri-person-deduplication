package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.clustering.indices.USHMMPersonIndex;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import types.LinearClassifier;
import utils.io.IOUtils;

import java.io.File;
import java.util.List;

public class ClusteringMain {

    private static final double epsilon = 0.00001d;
    private static final int minimalPoints = 2;
    private static final double levenshteinDistance = 0.10d;

    public static void main(String[] args) throws Exception {

        Namespace ns = getNamespace(args);
        if (ns != null) {
            String modelFilepath = ns.getString("modelFilepath");
            clusterData(modelFilepath);
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
        return parser;
    }

    private static void clusterData(String modelFilepath) throws Exception {
        LinearClassifier model = (LinearClassifier) IOUtils.loadModel(new File(modelFilepath).toURI().toURL());
        USHMMPersonIndex personIndex = new USHMMPersonIndex("/home/nely/fwdPersonIdDict.bin", "/home/nely/index.bin");
        DBSCANClustering dbScan = new DBSCANClustering(epsilon, minimalPoints, levenshteinDistance, model, personIndex);
        List<Cluster> clusters = dbScan.cluster();
        ClusteringResultsWriter<String> resultsWriter = new ClusteringResultsWriter<>();
        resultsWriter.printResults("/home/nely/", clusters, personIndex, epsilon, minimalPoints, levenshteinDistance, false);
    }

}
