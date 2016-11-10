package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.model.USHMMPerson;
import org.simmetrics.metrics.JaroWinkler;

import java.util.*;
import java.util.stream.Collectors;

class DBSCANClusterer  {

    private final double eps;
    private final int minPts;
    private DistanceMeasure measure;
    private JaroWinkler jaroWinkler;

    DBSCANClusterer(double eps, int minPts, DistanceMeasure measure) {
        this.measure = measure;
        this.eps = eps;
        this.minPts = minPts;
        jaroWinkler = new JaroWinkler();
    }

    List<Cluster> cluster(List<USHMMPerson> points) {
        List<Cluster> clusters = new ArrayList<>();
        Map<USHMMPerson, PointStatus> visited = new HashMap<>();

        for (USHMMPerson point : points) {
            if (visited.get(point) == null) {
                List<USHMMPerson> neighbors = getNeighbors(point, points);
                if (neighbors.size() >= minPts) {
                    Cluster cluster = new Cluster();
                    clusters.add(expandCluster(cluster, point, neighbors, points, visited));
                } else {
                    visited.put(point, PointStatus.NOISE);
                }
            }
        }

        return clusters;
    }

    private Cluster expandCluster(Cluster cluster, USHMMPerson point, List<USHMMPerson> neighbors,
                                  List<USHMMPerson> points, Map<USHMMPerson, PointStatus> visited) {
        cluster.addPoint(point);
        visited.put(point, PointStatus.PART_OF_CLUSTER);
        List<USHMMPerson> seeds = new ArrayList<>(neighbors);

        for (int index = 0; index < seeds.size(); ++index) {
            USHMMPerson current = seeds.get(index);
            PointStatus pStatus = visited.get(current);
            if (pStatus == null) {
                List<USHMMPerson> currentNeighbors = getNeighbors(current, points);
                if (currentNeighbors.size() >= minPts)
                    seeds = merge(seeds, currentNeighbors);
            }

            if (pStatus != PointStatus.PART_OF_CLUSTER) {
                visited.put(current, PointStatus.PART_OF_CLUSTER);
                cluster.addPoint(current);
            }
        }

        return cluster;
    }

    private List<USHMMPerson> getNeighbors(USHMMPerson point, List<USHMMPerson> points) {
        return points.stream().filter(
                point1 -> point != point1 && jaroWinkler.compare(point1.getStringValue("normalizedName"),
                        point.getStringValue("normalizedName")) >= 0.93 &&
                        distance(point1, point) <= eps
        ).collect(Collectors.toList());
    }

    private double distance(USHMMPerson p1, USHMMPerson p2) {
        return measure.compute(p1, p2);
    }

    private List<USHMMPerson> merge(List<USHMMPerson> list1, List<USHMMPerson> list2) {
        Set<USHMMPerson> setList1 = new HashSet<>(list1);
        list1.addAll(list2.stream().filter(item -> !setList1.contains(item)).collect(Collectors.toList()));
        return list1;
    }

    private enum PointStatus {
        NOISE,
        PART_OF_CLUSTER
    }
}
