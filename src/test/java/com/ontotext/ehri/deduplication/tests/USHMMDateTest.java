package com.ontotext.ehri.deduplication.tests;

import com.ontotext.ehri.deduplication.measures.USHMMDate;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class USHMMDateTest {

    private final double DELTA = 0.001;

    @Test
    public void testUSHMMDateSimilarity() throws Exception {

        assertEquals(0.6, USHMMDate.similarity("", ""), DELTA);
        assertEquals(0.6, USHMMDate.similarity("", "19120931"), DELTA);
        assertEquals(1, USHMMDate.similarity("19120931", "19120931"), DELTA);
        assertTrue(USHMMDate.similarity("19120931", "19120931") > USHMMDate.similarity("19120931", "19120901"));
        assertTrue(USHMMDate.similarity("19120931", "19120901") > USHMMDate.similarity("19120931", "19120201"));
        assertTrue(USHMMDate.similarity("19120931", "19120201") > USHMMDate.similarity("600", "18500512"));
        assertTrue(USHMMDate.similarity("600", "18500512") > USHMMDate.similarity("19120713", "19130612"));

    }

}
