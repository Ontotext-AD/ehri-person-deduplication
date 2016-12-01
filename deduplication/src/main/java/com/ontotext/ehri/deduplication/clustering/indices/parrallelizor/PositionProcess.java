package com.ontotext.ehri.deduplication.clustering.indices.parrallelizor;

public interface PositionProcess {
    void process(int position, int threadIndex);
}
