package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.model.USHMMPerson;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

class ClusteringResultsWriter {

    static void printResults(String resultsDir, List<Cluster> clusters, List<USHMMPerson> data, double epsilon, int minimalPoints) {
        try {
            PrintWriter writer = new PrintWriter(getFileNameResults(resultsDir), "UTF-8");
            printResultsToFile(writer, clusters, data, epsilon, minimalPoints);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFileNameResults(String resultsDir) {
        return resultsDir + "log" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }

    private static void printResultsToFile(PrintWriter writer, List<Cluster> clusters, List<USHMMPerson> data, double epsilon, int minimalPoints) {
        printHeaderInfo(writer, clusters, data.size(), epsilon, minimalPoints);
        printClustersToFile(writer, clusters);
        printUnreachablePointsToFile(writer, clusters, data);
    }

    private static void printHeaderInfo(PrintWriter writer, List<Cluster> clusters, int totalPoints, double epsilon, int minimalPoints) {
        int minClusterSize = totalPoints, maxClusterSize = -1;
        int reachablePointsCount = 0;
        for (Cluster cluster : clusters) {
            int clusterSize = cluster.points.size();
            reachablePointsCount += clusterSize;
            if (minClusterSize > clusterSize) minClusterSize = clusterSize;
            if (maxClusterSize < clusterSize) maxClusterSize = clusterSize;
        }

        writer.println("Epsilon : " + epsilon + "; minimal points " + minimalPoints);
        writer.println("Reachable points : " + reachablePointsCount + " from " + totalPoints);
        writer.println("Total clusters : " + clusters.size());
        writer.println("Biggest cluster size : " + maxClusterSize);
        writer.println("Smallest cluster size : " + minClusterSize);
        writer.println();
    }


    private static void printClustersToFile(PrintWriter writer, List<Cluster> clusters) {
        for (Cluster cluster : clusters)
            printClusterStatsToFile(writer, cluster);
    }

    private static void printClusterStatsToFile(PrintWriter writer, Cluster cluster) {
        writer.println("BEGIN CLUSTER =============================================");
        cluster.points.forEach(writer::println);
        writer.println("END CLUSTER =============================================");
        writer.println();
    }

    private static void printUnreachablePointsToFile(PrintWriter writer, List<Cluster> clusters, List<USHMMPerson> data) {
        writer.println("Unreachable points : ");
        writer.println();
        data.stream().filter(ushmmPerson -> !isReachable(clusters, ushmmPerson)).forEach(writer::println);
    }

    private static boolean isReachable(List<Cluster> clusters, USHMMPerson ushmmPerson) {
        for (Cluster cluster : clusters)
            if (cluster.points.contains(ushmmPerson))
                return true;
        return false;
    }

}
