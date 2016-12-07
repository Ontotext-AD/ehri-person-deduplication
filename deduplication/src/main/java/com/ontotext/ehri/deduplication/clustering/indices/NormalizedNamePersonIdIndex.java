package com.ontotext.ehri.deduplication.clustering.indices;

import com.ontotext.ehri.deduplication.clustering.approximata.MinAcyclicFSA;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NormalizedNamePersonIdIndex {

    private static final Logger logger = Logger.getLogger(NormalizedNamePersonIdIndex.class);

    private static int COUNT = 2030176;

    private List[] index;
    private MinAcyclicFSA normalizedNameFSA;
    private USHMMPersonIndex personIndex;

    public NormalizedNamePersonIdIndex(USHMMPersonIndex personIndex, String indexFileName) throws Exception {
        this.personIndex = personIndex;

        normalizedNameFSA = MinAcyclicFSA.read(new File("/home/nely/index/normalizedNameLowerCaseFSA.bin"));

        File indexFile = new File(indexFileName);
        if (indexFile.exists())
            readIndexFromFileAndLogExecutionTime(indexFileName);
        else
            buildIndexAndWriteToFile(indexFileName);
    }

    public List get(int normalizedNameInt) {
        return index[normalizedNameInt];
    }

    private void readIndexFromFileAndLogExecutionTime(String indexFileName) throws IOException {
        logger.info("Begin read index");
        long startExecution = System.currentTimeMillis();
        readIndexFromFile(indexFileName);
        long executionTime = System.currentTimeMillis() - startExecution;
        logger.info("Finish read index in " + TimeUnit.MILLISECONDS.toSeconds(executionTime) + " seconds");
    }

    private void readIndexFromFile(String indexFileName) throws IOException {
        index = new List[COUNT];
        DataInputStream dataIn = new DataInputStream(new FileInputStream(indexFileName));

        for (int i = 0; i < COUNT; ++i) {
            int size = dataIn.readInt();
            index[i] = new ArrayList<String>(size);
            for (int k = 0; k < size; ++k)
                index[i].add(dataIn.readUTF());
        }
    }

    private void buildIndexAndWriteToFile(String indexFileName) throws IOException {
        buildIndexAndLogExecutionTime();
        writeIndexToFileAndLogExecutionTime(indexFileName);
    }

    private void buildIndexAndLogExecutionTime() {
        logger.info("Begin build index");
        long startExecution = System.currentTimeMillis();
        buildIndex();
        long executionTime = System.currentTimeMillis() - startExecution;
        System.out.println("Finish build index in " + TimeUnit.MILLISECONDS.toMinutes(executionTime) + " minutes");
    }

    private void buildIndex() {
        index = new List[COUNT];
        for (String personId : personIndex) {
            String normalizedNameLowerCase = personIndex.getValueLowerCase(personId, "normalizedName");
            if (!normalizedNameLowerCase.isEmpty()) {
                int intNormalizedNameLowerCase = normalizedNameFSA.stringToInt(normalizedNameLowerCase);
                List<String> personIdsList = index[intNormalizedNameLowerCase];
                if (personIdsList == null)
                    personIdsList = new ArrayList<>();
                personIdsList.add(personId);
                index[intNormalizedNameLowerCase] = personIdsList;
            }
        }
    }

    private void writeIndexToFileAndLogExecutionTime(String indexFileName) throws IOException {
        logger.info("Begin write index");
        long startExecution = System.currentTimeMillis();
        writeIndexToFile(indexFileName);
        long executionTime = System.currentTimeMillis() - startExecution;
        logger.info("Finish write index in " + TimeUnit.MILLISECONDS.toMinutes(executionTime) + " minutes");
    }

    private void writeIndexToFile(String indexFileName) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(indexFileName));
        for (int i = 0; i < COUNT; i++) {
            List<String> valuesList = index[i];
            dataOut.writeInt(valuesList.size());
            for (int k = 0; k < valuesList.size(); ++k)
                dataOut.writeUTF(valuesList.get(k));
            if (i % 100000 == 0)
                dataOut.flush();
        }
        dataOut.flush();
        dataOut.close();
    }

}
