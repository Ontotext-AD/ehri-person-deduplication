package com.ontotext.ehri.normalization.tests;

import com.ontotext.ehri.normalization.USHMMNationalityNormalization;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class USHMMNationalityNormalizationTest {

    @Test
    public void testGermanNationality() throws Exception {
        assertEquals("German", USHMMNationalityNormalization.normalize("German"));
        assertEquals("German", USHMMNationalityNormalization.normalize("German (\"altreichsdeutsche\")"));
        assertEquals("German", USHMMNationalityNormalization.normalize("German (\"reichsdeutsch\")"));
        assertEquals("German", USHMMNationalityNormalization.normalize("German (\"volksdeutsch\")"));
    }

}
