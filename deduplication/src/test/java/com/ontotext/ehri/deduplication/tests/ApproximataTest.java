package com.ontotext.ehri.deduplication.tests;

import com.ontotext.ehri.deduplication.clustering.approximata.BuildMinAcyclicFSA;
import com.ontotext.ehri.deduplication.clustering.approximata.MinAcyclicFSA;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;

public class ApproximataTest {

    @Test
    public void testBuildApproximataWithRandomStrings() throws Exception {

        int numberOfStrings = 4000000;

        String stringFileName = createTemporaryFileAndWriteRandomStrings("strings", "txt", numberOfStrings);
        String sortedStringsFileName = createTemporaryFile("sortedStrings", "txt");
        String fsaBinaryFileName = createTemporaryFile("fsa", "bin");

        BuildMinAcyclicFSA buildMinAcyclicFSA = new BuildMinAcyclicFSA();
        buildMinAcyclicFSA.sortFile(stringFileName, "UTF-8", sortedStringsFileName, "UTF-8");
        buildMinAcyclicFSA.buildMinAcyclicFSA(sortedStringsFileName, "UTF-8", "false", fsaBinaryFileName);

        MinAcyclicFSA minAcyclicFSA = MinAcyclicFSA.read(new File(fsaBinaryFileName));
        assertEquals(numberOfStrings, minAcyclicFSA.numberOfStrings);

    }

    private String createTemporaryFileAndWriteRandomStrings(String fileName, String fileSuffix, int numberOfStrings) {
        try {
            File tempFile = File.createTempFile(fileName, fileSuffix);
            tempFile.deleteOnExit();
            BufferedWriter out = new BufferedWriter(new FileWriter(tempFile));
            for (int i = 0; i < numberOfStrings; ++i)
                out.write(generateRandomString(1, 40));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName + fileSuffix;
    }

    private String generateRandomString(int minLength, int maxLength) {
        int randomLength = ThreadLocalRandom.current().nextInt(minLength, maxLength + 1);
        String randomString = "";
        for (int i = 0; i < randomLength; ++i)
            randomString += (char) ThreadLocalRandom.current().nextInt('a', 'z' + 1);
        return randomString;
    }

    private String createTemporaryFile(String fileName, String fileSuffix) {
        try {
            File tempFile = File.createTempFile(fileName, fileSuffix);
            tempFile.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName + fileSuffix;
    }
    
}
