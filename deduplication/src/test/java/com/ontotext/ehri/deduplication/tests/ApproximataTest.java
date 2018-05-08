package com.ontotext.ehri.deduplication.tests;

import com.ontotext.ehri.deduplication.clustering.approximata.Approximate;
import com.ontotext.ehri.deduplication.clustering.approximata.BuildMinAcyclicFSA;
import com.ontotext.ehri.deduplication.clustering.approximata.MinAcyclicFSA;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class ApproximataTest {

    private static final int RANDOM_SEED = 666;

    private static final int NUMBER_OF_STRINGS = 10000;
    private static final int STRING_MIN_LENGTH = 1;
    private static final int STRING_MAX_LENGTH = 42;

    private static final File temporaryDir = new File("/tmp");

    private static final String TXT_SUFFIX = "txt";
    private static final String BIN_SUFFIX = "bin";

    private static final String STRINGS_FILE_NAME = "strings";

    private static final String SORTED_STRINGS_FILE_NAME = "sortedStrings";
    private static final String FWD_FSA_FILE_NAME = "fwdFSA";

    private static final String REVERSED_STRINGS_FILE_NAME = "reversedStrings";
    private static final String BWD_FSA_FILE_NAME = "bwdFSA";

    private static final String UTF_8_ENCODING = "UTF-8";
    private static final String PERFECT_HASH = "true";

    private static final String STRING_QUERY = "LOREMIPSUM";
    private static final String GARBLED_STRING = "OREMIPPSUN";

    @Ignore
    @Test
    public void testApproximateSearch() throws Exception {

        BuildMinAcyclicFSA buildMinAcyclicFSA = new BuildMinAcyclicFSA();

        Set<String> randomStringsSet = generateRandomStrings(NUMBER_OF_STRINGS, STRING_MIN_LENGTH, STRING_MAX_LENGTH);

        String stringsFileName = createEmptyTemporaryFile(STRINGS_FILE_NAME, TXT_SUFFIX, temporaryDir);
        writeStringsToFile(stringsFileName, randomStringsSet);
        String sortedStringsFileName = createEmptyTemporaryFile(SORTED_STRINGS_FILE_NAME, TXT_SUFFIX, temporaryDir);
        String fwdFSAFileName = createEmptyTemporaryFile(FWD_FSA_FILE_NAME, BIN_SUFFIX, temporaryDir);
        String reversedStringsFileName = createEmptyTemporaryFile(REVERSED_STRINGS_FILE_NAME, TXT_SUFFIX, temporaryDir);
        String bwdFSAFileName = createEmptyTemporaryFile(BWD_FSA_FILE_NAME, BIN_SUFFIX, temporaryDir);

        buildMinAcyclicFSA.sortFile(stringsFileName, UTF_8_ENCODING, sortedStringsFileName, UTF_8_ENCODING);
        buildMinAcyclicFSA.buildMinAcyclicFSA(sortedStringsFileName, UTF_8_ENCODING, PERFECT_HASH, fwdFSAFileName);
        MinAcyclicFSA fwdFSA = MinAcyclicFSA.read(new File(fwdFSAFileName));

        assertEquals(NUMBER_OF_STRINGS, fwdFSA.numberOfStrings);

        buildMinAcyclicFSA.reverseFile(stringsFileName, UTF_8_ENCODING, reversedStringsFileName, UTF_8_ENCODING);
        buildMinAcyclicFSA.buildMinAcyclicFSA(reversedStringsFileName, UTF_8_ENCODING, PERFECT_HASH, bwdFSAFileName);
        MinAcyclicFSA bwdFSA = MinAcyclicFSA.read(new File(FWD_FSA_FILE_NAME + "." + BIN_SUFFIX));

        Approximate approx = new Approximate();

        String[] resultsFwd = approx.findFwd(fwdFSA, STRING_QUERY, 3, Approximate.TYPE_LEVENSHTEIN);
        int[] resultsFwdBwd = approx.findFwdBwd(fwdFSA, bwdFSA, STRING_QUERY, 3, Approximate.TYPE_LEVENSHTEIN);

        assertEquals(resultsFwd.length, resultsFwdBwd.length);

        boolean containsGarbledString = false;
        for (String result : resultsFwd)
            if (result.equals(GARBLED_STRING)) {
                containsGarbledString = true;
                break;
            }
        assertTrue(containsGarbledString);

        containsGarbledString = false;
        int garbledStringInt = fwdFSA.stringToInt(GARBLED_STRING);
        for (int result : resultsFwdBwd)
            if (result == garbledStringInt) {
                containsGarbledString = true;
                break;
            }
        assertTrue(containsGarbledString);

    }

    private Set<String> generateRandomStrings(int numberOfStrings, int minLength, int maxLength) {
        Random r = new Random(RANDOM_SEED);
        Set<String> randomStringsSet = new HashSet<>();

        while (randomStringsSet.size() < numberOfStrings - 1)
            randomStringsSet.add(generateRandomString(r, minLength, maxLength));

        randomStringsSet.add(GARBLED_STRING);

        return randomStringsSet;
    }

    private String generateRandomString(Random r, int minLength, int maxLength) {
        int randomLength = minLength + r.nextInt(maxLength + 1);
        String randomString = "";
        for (int i = 0; i < randomLength; ++i)
            randomString += 'a' + r.nextInt('z' + 1);
        return randomString;
    }

    private String createEmptyTemporaryFile(String prefix, String suffix, File dir) throws IOException {
        File tempFile = File.createTempFile(prefix, suffix, dir);
        tempFile.deleteOnExit();
        return tempFile.getAbsolutePath();
    }

    private void writeStringsToFile(String fileName, Set<String> stringSet) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), UTF_8_ENCODING));
        stringSet.forEach(out::println);
        out.close();
    }
}
