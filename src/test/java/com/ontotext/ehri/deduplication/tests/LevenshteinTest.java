package com.ontotext.ehri.deduplication.tests;

import com.ontotext.ehri.deduplication.measures.Levenshtein;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LevenshteinTest {

    @Test
    public void testLevenshteinDistance() throws Exception {

        assertEquals(Levenshtein.distance("", ""), 0);
        assertEquals(Levenshtein.distance("FOO", ""), 3);
        assertEquals(Levenshtein.distance("", "BAR"), 3);
        assertEquals(Levenshtein.distance("FOO", "BAR"), 3);
        assertEquals(Levenshtein.distance("FOO", "FOF"), 1);

    }

}
