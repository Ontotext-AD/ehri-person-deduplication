package com.ontotext.ehri.deduplication.tests;

import com.ontotext.ehri.deduplication.measures.JaroWinkler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JaroWinklerTest {

    private final double DELTA = 0.001;

    @Test
    public void testJaroWinklerDistance() throws Exception {

        assertEquals(JaroWinkler.distance("MARTHA", "MARHTA"), 0.961, DELTA);
        assertEquals(JaroWinkler.distance("DWAYNE", "DUANE"), 0.84, DELTA);
        assertEquals(JaroWinkler.distance("DIXON", "DICKSONX"), 0.814, DELTA);

    }
}
