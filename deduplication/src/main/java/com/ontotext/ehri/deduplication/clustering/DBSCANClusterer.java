package com.ontotext.ehri.deduplication.clustering;


import java.util.*;
import java.util.stream.Collectors;

class DBSCANClusterer<T>  {

    private final double eps;
    private final int minPts;
    private DistanceMeasure measure;

    DBSCANClusterer(double eps, int minPts, DistanceMeasure measure) {
        this.measure = measure;
        this.eps = eps;
        this.minPts = minPts;
    }

    List<Cluster<T>> cluster(Collection<T> points) {
        List<Cluster<T>> clusters = new ArrayList<>();
        Map<T, PointStatus> visited = new HashMap<>();

        for (T point : points) {
            if (visited.get(point) == null) {
                List<T> neighbors = getNeighbors(point, points);
                if (neighbors.size() >= minPts) {
                    Cluster<T> cluster = new Cluster<>();
                    clusters.add(expandCluster(cluster, point, neighbors, points, visited));
                } else {
                    visited.put(point, PointStatus.NOISE);
                }
            }
        }

        return clusters;
    }

    private Cluster<T> expandCluster(Cluster<T> cluster, T point, List<T> neighbors, Collection<T> points, Map<T, PointStatus> visited) {
        cluster.addPoint(point);
        visited.put(point, PointStatus.PART_OF_CLUSTER);
        List<T> seeds = new ArrayList<>(neighbors);

        for (int index = 0; index < seeds.size(); ++index) {
            T current = seeds.get(index);
            PointStatus pStatus = visited.get(current);
            if (pStatus == null) {
                List<T> currentNeighbors = getNeighbors(current, points);
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

    private List<T> getNeighbors(T point, Collection<T> points) {
        return points.stream().filter(
                point1 -> point != point1 && distance(point1, point) <= eps
        ).collect(Collectors.toList());
    }

    private double distance(T p1, T p2) {
        return measure.compute(p1, p2);
    }

    private List<T> merge(List<T> list1, List<T> list2) {
        Set<T> setList1 = new HashSet<>(list1);
        list1.addAll(list2.stream().filter(item -> !setList1.contains(item)).collect(Collectors.toList()));
        return list1;
    }

    private enum PointStatus {
        NOISE,
        PART_OF_CLUSTER
    }
}
