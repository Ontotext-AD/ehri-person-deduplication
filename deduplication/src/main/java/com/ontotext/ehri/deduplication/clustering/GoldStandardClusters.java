package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.classifier.model.USHMMClassificationInstance;
import com.ontotext.ehri.deduplication.classifier.model.USHMMGoldStandardEntry;
import com.ontotext.ehri.deduplication.indices.USHMMPersonIndex;
import com.ontotext.ehri.deduplication.measures.Levenshtein;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import types.Alphabet;
import types.LinearClassifier;
import types.SparseVector;
import utils.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GoldStandardClusters {
    public static void main(String[] args) throws ClassNotFoundException, QueryEvaluationException, TupleQueryResultHandlerException, IOException {

        USHMMPersonIndex index = new USHMMPersonIndex(
                "/home/nely/workspace/ehri/ehri-person-deduplication/deduplication/target/indices/personIdFSA.bin",
                "/home/nely/workspace/ehri/ehri-person-deduplication/deduplication/target/indices/index.bin"
        );
        LinearClassifier classifier = (LinearClassifier) IOUtils.loadModel(new File("/home/nely/model.bin").toURI().toURL());
        Alphabet xA = classifier.getxAlphabet();

        Set<String> allPoints = new HashSet<>();
        Map<String, Set<String>> clusters = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("/home/nely/gold.tsv"))) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter('\t').withHeader("p1", "p2", "m", "c").parse(br);
            for (CSVRecord record : records)
            {
                String p1 = record.get("p1").replace("https://www.ushmm.org/online/hsv/person_view.php?PersonId=", "");
                String p2 = record.get("p2").replace("https://www.ushmm.org/online/hsv/person_view.php?PersonId=", "");

                allPoints.add(p1);
                allPoints.add(p2);

                String m = record.get("m");
                String c = record.get("c");

                if (!c.isEmpty()) {
                    Set<String> s = clusters.get(c);
                    if (s == null) s = new HashSet<>();
                    s.add(p1);
                    s.add(p2);
                    clusters.put(c, s);
                } else if (m.equals("2")) {
                    Set<String> s1 = new HashSet<>();
                    s1.add(p1); s1.add(p2);
                    clusters.put(p1 + " - " + p2, s1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        double maximumLevenshteinDistancePercentageError = 0;
        double maximumScore = 0;

        Set<String> reachablePoints = new HashSet<>();
        int biggestClusterSize = 0;
        int smallestClusterSize = allPoints.size();
        for (String c : clusters.keySet()) {
            int size = clusters.get(c).size();
            if (size < smallestClusterSize)
                smallestClusterSize = size;
            if (size > biggestClusterSize)
                biggestClusterSize = size;
            reachablePoints.addAll(clusters.get(c).stream().collect(Collectors.toList()));

            for (String p1 : clusters.get(c)) {
                for (String p2 : clusters.get(c)) {
                    if (!p1.equals(p2)) {
                        String name1 = index.getValueLowerCase(p1, "normalizedName");
                        String name2 = index.getValueLowerCase(p2, "normalizedName");

                        double distance = Levenshtein.distance(name1, name2);
                        if (name1.length() > name2.length()) {
                            double percentage1 = (distance * 100) / name1.length();
                            if (percentage1 > maximumLevenshteinDistancePercentageError)
                                maximumLevenshteinDistancePercentageError = percentage1;
                        }
                        else {
                            double percentage2 = (distance * 100) / name2.length();
                            if (percentage2 > maximumLevenshteinDistancePercentageError)
                                maximumLevenshteinDistancePercentageError = percentage2;
                        }

                        SparseVector sparseVector = new USHMMClassificationInstance(xA, p1, p2, index).getSparseVector();
                        Map<String, Double> scores = classifier.labelScoreNormalized(sparseVector);
                        if (classifier.getyAlphabet().lookupInt(classifier.label(sparseVector)).equals(USHMMGoldStandardEntry.POSITIVE_CLASS)) {
                            double score = 1 - scores.get(USHMMGoldStandardEntry.POSITIVE_CLASS);
                            if (score > maximumScore)
                                maximumScore = score;
                        }
                    }
                }
            }
        }

        System.out.println("Epsilon : " + maximumScore);
        System.out.println("Minimal points : " + smallestClusterSize);
        System.out.println("Maximum Levenshtein Distance Percentage Error : " + maximumLevenshteinDistancePercentageError);
        System.out.println("Reachable points : " + reachablePoints.size() + " from " + allPoints.size());
        System.out.println("Average points in cluster : " + String.format("%.10f", (double) allPoints.size() / (clusters.size() + (allPoints.size() - reachablePoints.size()))));
        System.out.println("Total clusters : " + clusters.size());
        System.out.println("Biggest cluster size : " + biggestClusterSize);
        System.out.println("Smallest cluster size : " + smallestClusterSize);

    }

}
