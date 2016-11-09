package com.ontotext.ehri.normalization.tests;

import com.ontotext.ehri.normalization.USHMMPersonNameNormalization;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class USHMMPersonNameNormalizationTest {

    @Test
    public void testCyrillicTransliteration() throws Exception {
        assertEquals("Zyunya", USHMMPersonNameNormalization.normalize("Зюня"));
    }

    @Test
    public void testNormalizedNameTitleCase() throws Exception {
        assertEquals("Zysa Brucha", USHMMPersonNameNormalization.normalize("ZYSA BRUCHA"));
    }

    @Test
    public void testHyphenIsReplacedWithSpace() throws Exception {
        assertEquals("Zysa Blima", USHMMPersonNameNormalization.normalize("Zysa-Blima"));
    }

    @Test
    public void testApostropheIsRemoved() throws Exception {
        assertEquals("Zyscindowi", USHMMPersonNameNormalization.normalize("Zyscińdowi"));
    }

    @Test
    public void testQuestionMarkIsRemoved() throws Exception {
        assertEquals("Zysie", USHMMPersonNameNormalization.normalize("Zysie?"));
    }

    @Test
    public void testDotsAreRemoved() throws Exception {
        assertEquals("Zyson", USHMMPersonNameNormalization.normalize("...zyson"));
    }

    @Test
    public void testPolishLIsReplacedWithL() throws Exception {
        assertEquals("Zysa Laja", USHMMPersonNameNormalization.normalize("Zysa Łaja"));
    }

    @Test
    public void testAUmlaut() throws Exception {
        assertEquals("Zuckerbaeker", USHMMPersonNameNormalization.normalize("Zuckerbäker"));
    }

}
