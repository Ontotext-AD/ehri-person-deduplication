package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.model.USHMMPerson;

import java.util.ArrayList;
import java.util.List;

class Cluster {

    final List<USHMMPerson> points = new ArrayList<>();

    void addPoint(USHMMPerson point) {
        points.add(point);
    }

}
