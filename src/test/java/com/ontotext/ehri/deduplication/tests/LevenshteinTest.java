package com.ontotext.ehri.deduplication.tests;

import com.ontotext.ehri.deduplication.measures.Levenshtein;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LevenshteinTest {

    @Test
    public void testLevenshteinDistance() throws Exception {

        assertEquals(0, Levenshtein.distance("", ""));
        assertEquals(3, Levenshtein.distance("FOO", ""));
        assertEquals(3, Levenshtein.distance("", "BAR"));
        assertEquals(3, Levenshtein.distance("FOO", "BAR"));
        assertEquals(1, Levenshtein.distance("FOO", "FOF"));

    }

}
