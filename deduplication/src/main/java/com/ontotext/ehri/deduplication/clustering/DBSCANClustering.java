package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.classifier.model.USHMMClassificationInstance;
import com.ontotext.ehri.deduplication.classifier.model.USHMMGoldStandardEntry;
import com.ontotext.ehri.deduplication.clustering.approximata.ApproximateSearch;
import com.ontotext.ehri.deduplication.clustering.approximata.MinAcyclicFSA;
import com.ontotext.ehri.deduplication.clustering.indices.NormalizedNamePersonIdIndex;
import com.ontotext.ehri.deduplication.clustering.indices.USHMMPersonIndex;
import types.Alphabet;
import types.LinearClassifier;
import types.SparseVector;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

class DBSCANClustering {

    private final double eps;
    private final int minPts;
    private final double levenshteinDistance;

    private LinearClassifier model;
    private Alphabet xA;
    private USHMMPersonIndex personIndex;

    private MinAcyclicFSA personIdFSA;
    private MinAcyclicFSA normalizedNameLowerCaseFSA;
    private MinAcyclicFSA normalizedNameLowerCaseReversedFSA;

    private ApproximateSearch search = new ApproximateSearch();
    private NormalizedNamePersonIdIndex normalizedNamePersonIdIndex;

    DBSCANClustering(double eps, int minPts, double levenshteinDistance, LinearClassifier model, USHMMPersonIndex personIndex) throws Exception {
        this.model = model;
        this.eps = eps;
        this.minPts = minPts;
        this.levenshteinDistance = levenshteinDistance;
        this.personIndex = personIndex;

        xA = model.getxAlphabet();

        personIdFSA = MinAcyclicFSA.read(new File("/home/nely/fwdPersonIdDict.bin"));
        normalizedNameLowerCaseFSA = MinAcyclicFSA.read(new File("/home/nely/normalizedNameLowerCaseDictSorted.bin"));
        normalizedNameLowerCaseReversedFSA = MinAcyclicFSA.read(new File("/home/nely/normalizedNameLowerCaseDictReversed.bin"));

        normalizedNamePersonIdIndex = new NormalizedNamePersonIdIndex(
                personIndex, "/home/nely/normalizedNamePersonIdIndex.bin"
        );
    }

    List<Cluster> cluster() throws Exception {
        List<Cluster> clusters = new ArrayList<>();
        Map<String, PointStatus> visited = new HashMap<>();

        long start = System.currentTimeMillis();
        for (String personId : personIndex) {
            if (visited.size() % 1000 == 0)
                System.out.println("Visited " + visited.size());
            if (visited.get(personId) == null) {
                List<String> neighbors = getNeighbors(personId);
                if (neighbors.size() >= minPts) {
                    Cluster cluster = new Cluster();
                    clusters.add(expandCluster(cluster, personId, neighbors, visited));
                } else {
                    visited.put(personId, PointStatus.NOISE);
                }
            }
        }

        System.out.println("Total execution time " + (System.currentTimeMillis() - start));
        return clusters;
    }

    private Cluster expandCluster(Cluster cluster, String point, List<String> neighbors,
                                          Map<String, PointStatus> visited) throws Exception {
        cluster.addPoint(point);
        visited.put(point, PointStatus.PART_OF_CLUSTER);
        List<String> seeds = new ArrayList<>(neighbors);

        for (int index = 0; index < seeds.size(); ++index) {
            String current = seeds.get(index);
            PointStatus pStatus = visited.get(current);
            if (pStatus == null) {
                List<String> currentNeighbors = getNeighbors(current);
                if (currentNeighbors.size() >= minPts) {
                    seeds = merge(seeds, currentNeighbors);
                }
            }

            if (pStatus != PointStatus.PART_OF_CLUSTER) {
                visited.put(current, PointStatus.PART_OF_CLUSTER);
                cluster.addPoint(current);
            }
        }

        return cluster;
    }

    private List<String> getNeighbors(String personId) throws Exception {
        List<String> neighbors = new ArrayList<>();

        int personIdInt = personIdFSA.stringToInt(personId);
        String normalizedNameLowerCase = personIndex.getValueLowerCase(personIdInt, "normalizedName");
        int distance = (int) (normalizedNameLowerCase.length() * levenshteinDistance);

        int[] candidates = new int[0];
        try {
            candidates = search.approximateSearch(
                    normalizedNameLowerCaseFSA, normalizedNameLowerCaseReversedFSA, 0, distance, normalizedNameLowerCase
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<String> personIdsWithSimilarNames = new ArrayList<>();
        for (int i = 0; i < candidates.length; ++i)
            personIdsWithSimilarNames.addAll(normalizedNamePersonIdIndex.get(candidates[i]));

        int ind = normalizedNameLowerCaseFSA.stringToInt(normalizedNameLowerCase);
        if (ind != -1) {
            List<String> personsSameName = normalizedNamePersonIdIndex.get(ind);
            for (String personWithSameName : personsSameName)
                personIdsWithSimilarNames.add(personWithSameName);
        }

        neighbors.addAll(personIdsWithSimilarNames.stream().filter(
                personId1 -> !personId.equals(personId1) && distance(personId, personId1) <= eps
        ).collect(Collectors.toList()));
        return neighbors;
    }

    private double distance(String personId1, String personId2) {
        SparseVector sparseVector = new USHMMClassificationInstance(xA, personId1, personId2, personIndex).getSparseVector();
        Map<String, Double> scores = model.labelScoreNormalized(sparseVector);
        return 1 - scores.get(USHMMGoldStandardEntry.POSITIVE_CLASS);
    }

    private List<String> merge(List<String> list1, List<String> list2) {
        Set<String> setList1 = new HashSet<>(list1);
        list1.addAll(list2.stream().filter(item -> !setList1.contains(item)).collect(Collectors.toList()));
        return list1;
    }

    private enum PointStatus {
        NOISE,
        PART_OF_CLUSTER
    }
}
