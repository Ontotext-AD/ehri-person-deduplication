package com.ontotext.ehri.deduplication.clustering;

import java.util.ArrayList;
import java.util.List;

class Cluster<T> {

    final List<T> points = new ArrayList<>();

    void addPoint(T point) {
        points.add(point);
    }

}
