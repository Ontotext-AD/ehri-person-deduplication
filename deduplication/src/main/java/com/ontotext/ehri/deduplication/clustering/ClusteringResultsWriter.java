package com.ontotext.ehri.deduplication.clustering;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

class ClusteringResultsWriter {

    void printResults(String resultsDirectory, List<Cluster> clusters, DBSCANClustering dbscan) {
        try {
            PrintWriter writer = new PrintWriter(getFileNameResults(resultsDirectory), "UTF-8");
            printResultsToFile(writer, clusters, dbscan);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileNameResults(String resultsDirectory) {
        return resultsDirectory + "clusteringResults-" + new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(new Date()) + ".txt";
    }

    private void printResultsToFile(PrintWriter writer, List<Cluster> clusters, DBSCANClustering dbscan) {
        printHeaderInfo(writer, clusters, dbscan.personIndex.size, dbscan.eps, dbscan.minPts, dbscan.levenshteinDistance);
        printClustersToFile(writer, clusters);
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
}
