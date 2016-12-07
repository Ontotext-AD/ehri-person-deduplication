package com.ontotext.ehri.deduplication.tests;

import com.ontotext.ehri.deduplication.clustering.approximata.BuildMinAcyclicFSA;
import com.ontotext.ehri.deduplication.clustering.approximata.MinAcyclicFSA;
import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ApproximataTest {

    private static final int RANDOM_SEED = 666;

    private static final int NUMBER_OF_STRINGS = 10000;
    private static final int STRING_MIN_LENGTH = 1;
    private static final int STRING_MAX_LENGTH = 42;

    private static final String UTF_8_ENCODING = "UTF-8";
    private static final String PERFECT_HASH = "true";

    @Test
    public void testBuildApproximataWithRandomStrings() throws Exception {

        Set<String> randomStringsSet = generateRandomStrings(NUMBER_OF_STRINGS, STRING_MIN_LENGTH, STRING_MAX_LENGTH);

        String stringsFileName = createEmptyTemporaryFile("strings.txt");
        writeStringsToFile(stringsFileName, randomStringsSet);
        String sortedStringsFileName = createEmptyTemporaryFile("sortedStrings.txt");
        String fsaBinaryFileName = createEmptyTemporaryFile("fsa.bin");

        BuildMinAcyclicFSA buildMinAcyclicFSA = new BuildMinAcyclicFSA();
        buildMinAcyclicFSA.sortFile(stringsFileName, UTF_8_ENCODING, sortedStringsFileName, UTF_8_ENCODING);
        buildMinAcyclicFSA.buildMinAcyclicFSA(sortedStringsFileName,UTF_8_ENCODING, PERFECT_HASH, fsaBinaryFileName);
        MinAcyclicFSA minAcyclicFSA = MinAcyclicFSA.read(new File(fsaBinaryFileName));

        assertEquals(NUMBER_OF_STRINGS, minAcyclicFSA.numberOfStrings);

    }

    private Set<String> generateRandomStrings(int numberOfStrings, int minLength, int maxLength) {
        Random r = new Random(RANDOM_SEED);
        Set<String> randomStringsSet = new HashSet<>();

        while (randomStringsSet.size() < numberOfStrings)
            randomStringsSet.add(generateRandomString(r, minLength, maxLength));

        return randomStringsSet;
    }

    private String generateRandomString(Random r, int minLength, int maxLength) {
        int randomLength = minLength + r.nextInt(maxLength + 1);
        String randomString = "";
        for (int i = 0; i < randomLength; ++i)
            randomString += 'a' + r.nextInt('z' + 1);
        return randomString;
    }

    private String createEmptyTemporaryFile(String fileName) {
        File tempFile = new File(fileName);
        tempFile.deleteOnExit();
        return fileName;
    }

    private void writeStringsToFile(String fileName, Set<String> stringSet) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), UTF_8_ENCODING));
        stringSet.forEach(out::println);
        out.close();
    }
}
