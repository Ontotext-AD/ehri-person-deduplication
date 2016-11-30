package com.ontotext.ehri.deduplication.clustering.approximata;

public class ApproximateSearch {

    public int[] approximateSearch(MinAcyclicFSA fwd, MinAcyclicFSA bwd, int type, int distance, String query) throws Exception {

        Approximate approx = new Approximate();
        return approx.findFwdBwd(fwd, bwd, query, distance, type);

    }

}
