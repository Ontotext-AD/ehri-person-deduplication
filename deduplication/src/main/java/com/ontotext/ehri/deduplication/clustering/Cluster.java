package com.ontotext.ehri.deduplication.clustering;

import java.util.ArrayList;
import java.util.List;

class Cluster {

    final List<String> points = new ArrayList<>();

    void addPoint(String point) {
        points.add(point);
    }

}
