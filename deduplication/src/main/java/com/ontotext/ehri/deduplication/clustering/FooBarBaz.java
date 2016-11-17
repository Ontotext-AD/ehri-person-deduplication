package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.model.USHMMClassificationInstance;
import com.ontotext.ehri.deduplication.model.USHMMGoldStandardEntry;
import com.ontotext.ehri.deduplication.model.USHMMPerson;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import types.Alphabet;
import types.LinearClassifier;
import types.SparseVector;
import utils.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FooBarBaz {
    private static final double eps = 0.00001d;
    private static final int minPts = 2;

    private static LinearClassifier model;
    private static String urlString = "http://localhost:8983/solr/ehri";
    private static SolrClient solr = new HttpSolrClient.Builder(urlString).build();
    private static SolrQuery query = new SolrQuery();

    FooBarBaz() throws IOException, ClassNotFoundException {
        model = (LinearClassifier) IOUtils.loadModel(new File("/home/nelly/worspace/model.bin").toURI().toURL());
    }
    public static void main(String[] args) throws IOException, SolrServerException {
//        List<Cluster> clusters = new ArrayList<>();
//        Map<String, PointStatus> visited = new HashMap<>();
//
//        for (String point : points) {
//            if (visited.get(point) == null) {
//                List<String> neighbors = getNeighbors(point);
//                if (neighbors.size() >= minPts) {
//                    Cluster cluster = new Cluster();
//                    clusters.add(expandCluster(cluster, point, neighbors, visited));
//                } else {
//                    visited.put(point, PointStatus.NOISE);
//                }
//            }
//        }

        List<String> neighbors = getCandidatesIdsForPerson("511555");
        System.out.println(neighbors.size());
    }
//
//    private static Cluster expandCluster(Cluster cluster, String point, List<String> neighbors, Map<String, PointStatus> visited) throws IOException, SolrServerException {
//        cluster.addPoint(point);
//        visited.put(point, PointStatus.PART_OF_CLUSTER);
//        List<String> seeds = new ArrayList<>(neighbors);
//
//        for (int index = 0; index < seeds.size(); ++index) {
//            String current = seeds.get(index);
//            PointStatus pStatus = visited.get(current);
//            if (pStatus == null) {
//                List<String> currentNeighbors = getNeighbors(current);
//                if (currentNeighbors.size() >= minPts)
//                    seeds = merge(seeds, currentNeighbors);
//            }
//
//            if (pStatus != PointStatus.PART_OF_CLUSTER) {
//                visited.put(current, PointStatus.PART_OF_CLUSTER);
//                cluster.addPoint(current);
//            }
//        }
//
//        return cluster;
//    }
//
//    private static List<String> getNeighbors(String personId) throws IOException, SolrServerException {
//        return getCandidatesIdsForPerson(personId).stream().filter(
//                candidateId -> computeDistance(candidateId, personId) <= eps
//        ).collect(Collectors.toList());
//    }

    private static List<String> getCandidatesIdsForPerson(String personId) throws SolrServerException, IOException {
        query.set("q", "id:" + personId);
        QueryResponse response = solr.query(query);
        SolrDocumentList list = response.getResults();
        String normalizedName = list.get(0).getFieldValueMap().get("normalizedName").toString();

        int start = 0, rows = 1000;

        query.set("q", "{!func}strdist(\"" + normalizedName + "\",normalizedName,jw)");
        query.set("fl", "*, score");
        query.set("fq", "{!frange l=0.93} strdist(\"" + normalizedName + "\",normalizedName,jw)");
        query.setStart(start);
        query.setRows(rows);

        List<String> candidates = new ArrayList<>();

        boolean hasMore = true;
        while (hasMore) {
            response = solr.query(query);
            list = response.getResults();
            for (SolrDocument doc : list) {
                for (String field : doc.getFieldValueMap().keySet())
                    System.out.println(field + " " + doc.getFieldValueMap().get(field));
                candidates.add(doc.getFieldValueMap().get("id").toString());
            }

            if (list.size() < rows)
                hasMore = false;

            start += rows;
            query.setStart(start);
            query.setRows(rows);
        }
        System.out.println(candidates.size());
        return candidates;
    }
//
//    private static double computeDistance(String id, String personId) {
//        Alphabet xA = model.getxAlphabet();
//        USHMMPerson var1 = new USHMMPerson(id, map1);
//        USHMMPerson var2 = new USHMMPerson(personId, map2);
//        SparseVector sparseVector = new USHMMClassificationInstance(xA, var1, var2).getSparseVector();
//        Map<String, Double> scores = model.labelScoreNormalized(sparseVector);
//        return 1 - scores.get(USHMMGoldStandardEntry.POSITIVE_CLASS);
//    }
//
//    private static List<String> merge(List<String> list1, List<String> list2) {
//        Set<String> setList1 = new HashSet<>(list1);
//        list1.addAll(list2.stream().filter(item -> !setList1.contains(item)).collect(Collectors.toList()));
//        return list1;
//    }
//
//    private enum PointStatus {
//        NOISE,
//        PART_OF_CLUSTER
//    }
}
