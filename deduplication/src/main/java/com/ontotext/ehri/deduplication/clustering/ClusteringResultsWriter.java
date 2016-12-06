package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.clustering.indices.USHMMPersonIndex;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

class ClusteringResultsWriter<T> {

    void printResults(String resultsDir, List<Cluster> clusters, USHMMPersonIndex personIndex, double epsilon, int minimalPoints, double levenshteinDistance, boolean printUnreachablePoints) {
        try {
            PrintWriter writer = new PrintWriter(getFileNameResults(resultsDir), "UTF-8");
            printResultsToFile(writer, clusters, personIndex, epsilon, minimalPoints, levenshteinDistance, printUnreachablePoints);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileNameResults(String resultsDir) {
        return resultsDir + "log" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }

    private void printResultsToFile(PrintWriter writer, List<Cluster> clusters, USHMMPersonIndex personIndex, double epsilon, int minimalPoints, double levenshteinDistance, boolean printUnreachablePoints) {
        printHeaderInfo(writer, clusters, personIndex.size, epsilon, minimalPoints, levenshteinDistance);
        printClustersToFile(writer, clusters);
        printUnreachablePointsToFile(writer, clusters, personIndex, printUnreachablePoints);
    }

    private void printHeaderInfo(PrintWriter writer, List<Cluster> clusters, int totalPoints, double epsilon, int minimalPoints, double levenshteinDistance) {
        int minClusterSize = totalPoints, maxClusterSize = -1;
        int reachablePointsCount = 0;
        for (Cluster cluster : clusters) {
            int clusterSize = cluster.points.size();
            reachablePointsCount += clusterSize;
            if (minClusterSize > clusterSize) minClusterSize = clusterSize;
            if (maxClusterSize < clusterSize) maxClusterSize = clusterSize;
        }

        writer.println("Epsilon : " + epsilon);
        writer.println("Minimal points " + minimalPoints);
        writer.println("Levenshtein distance " + levenshteinDistance);
        writer.println("Reachable points : " + reachablePointsCount + " from " + totalPoints);
        writer.println("Total clusters : " + clusters.size());
        writer.println("Biggest cluster size : " + maxClusterSize);
        writer.println("Smallest cluster size : " + minClusterSize);
        writer.println();
    }


    private void printClustersToFile(PrintWriter writer, List<Cluster> clusters) {
        for (Cluster cluster : clusters)
            printClusterStatsToFile(writer, cluster);
    }

    private void printClusterStatsToFile(PrintWriter writer, Cluster cluster) {
        writer.println("BEGIN CLUSTER =============================================");
        cluster.points.forEach(writer::println);
        writer.println("END CLUSTER =============================================");
        writer.println();
    }

    private void printUnreachablePointsToFile(PrintWriter writer, List<Cluster> clusters, USHMMPersonIndex personIndex, boolean printUnreachablePoints) {
        if (printUnreachablePoints) {
            writer.println("Unreachable points : ");
            writer.println();
            for (String personId : personIndex)
                if (!isReachable(clusters, personId))
                    writer.println(personId);
        }
    }

    private boolean isReachable(List<Cluster> clusters, String personId) {
        for (Cluster cluster : clusters)
            if (cluster.points.contains(personId))
                return true;
        return false;
    }

}
