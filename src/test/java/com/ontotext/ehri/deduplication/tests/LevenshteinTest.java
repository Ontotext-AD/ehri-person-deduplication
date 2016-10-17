package com.ontotext.ehri.deduplication.tests;

import com.ontotext.ehri.deduplication.measures.Levenshtein;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LevenshteinTest {

    private final double DELTA = 0.001;

    @Test
    public void testLevenshteinDistance() throws Exception {

        assertEquals(0, Levenshtein.distance("", ""));
        assertEquals(3, Levenshtein.distance("FOO", ""));
        assertEquals(3, Levenshtein.distance("", "BAR"));
        assertEquals(3, Levenshtein.distance("FOO", "BAR"));
        assertEquals(1, Levenshtein.distance("FOO", "FOF"));
        assertEquals(2, Levenshtein.distance("FOO", "OFO"));

    }

    @Test
    public void testNormalizedLevenshteinDistance() throws Exception {

        assertEquals(0.000, Levenshtein.similarity("FOO", ""), DELTA);
        assertEquals(0.000, Levenshtein.similarity("", "BAR"), DELTA);
        assertEquals(0.000, Levenshtein.similarity("FOO", "BAR"), DELTA);
        assertEquals(0.333, Levenshtein.similarity("FOO", "OFO"), DELTA);
        assertEquals(0.600, Levenshtein.similarity("Lorinzfalu", "Lorinz"), DELTA);
        assertEquals(0.666, Levenshtein.similarity("FOO", "FOF"), DELTA);
        assertEquals(1.000, Levenshtein.similarity("", ""), DELTA);

    }
}
