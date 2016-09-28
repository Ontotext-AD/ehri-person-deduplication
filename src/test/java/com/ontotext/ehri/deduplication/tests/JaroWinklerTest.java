package com.ontotext.ehri.deduplication.tests;

import com.ontotext.ehri.deduplication.measures.JaroWinkler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JaroWinklerTest {

    private final double DELTA = 0.001;

    @Test
    public void testJaroWinklerDistance() throws Exception {

        assertEquals(0.961, JaroWinkler.distance("MARTHA", "MARHTA"), DELTA);
        assertEquals(0.84, JaroWinkler.distance("DWAYNE", "DUANE"), DELTA);
        assertEquals(0.814, JaroWinkler.distance("DIXON", "DICKSONX"), DELTA);

    }
}
