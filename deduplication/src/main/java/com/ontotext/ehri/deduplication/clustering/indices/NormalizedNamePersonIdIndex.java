package com.ontotext.ehri.deduplication.clustering.indices;

import com.ontotext.ehri.deduplication.clustering.approximata.MinAcyclicFSA;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NormalizedNamePersonIdIndex {

    private static int COUNT = 2030176;
    private static final Logger logger = Logger.getLogger(NormalizedNamePersonIdIndex.class);

    private List[] inverseIndex;
    MinAcyclicFSA normalizedNameFSA;
   USHMMPersonIndex personIndex;

    public NormalizedNamePersonIdIndex(USHMMPersonIndex personIndex, String indexFileName) throws Exception {
        this.personIndex = personIndex;
        normalizedNameFSA = buildAndSerializeNormalizedNameFSA();
        File indexFile = new File(indexFileName);
        if (indexFile.exists()) {
            logger.info("Begin read index");
            long startExecution = System.currentTimeMillis();
            readIndexFromFile(indexFileName);
            long executionTime = System.currentTimeMillis() - startExecution;
            logger.info("Finish read index in " + TimeUnit.MILLISECONDS.toSeconds(executionTime) + " seconds");
        } else {
            logger.info("Begin build index");
            long startExecution = System.currentTimeMillis();
            buildIndex();
            long executionTime = System.currentTimeMillis() - startExecution;
            System.out.println("Finish build index in " + TimeUnit.MILLISECONDS.toMinutes(executionTime) + " minutes");
            logger.info("Begin write index");
            startExecution = System.currentTimeMillis();
            writeIndexToFile(indexFileName);
            executionTime = System.currentTimeMillis() - startExecution;
            logger.info("Finish write index in " + TimeUnit.MILLISECONDS.toMinutes(executionTime) + " minutes");
        }
    }

    private MinAcyclicFSA buildAndSerializeNormalizedNameFSA() throws Exception {
        return MinAcyclicFSA.read(new File("/home/nely/normalizedNameLowerCaseDictSorted.bin"));

    }

    private void buildIndex() {
        inverseIndex = new List[COUNT];
        for (String personId : personIndex) {
            String normalizedNameLowerCase = personIndex.getValueLowerCase(personId, "normalizedName");
            if (!normalizedNameLowerCase.isEmpty()) {
                int intNormalizedNameLowerCase = normalizedNameFSA.stringToInt(normalizedNameLowerCase);
                List<String> personIdsList = inverseIndex[intNormalizedNameLowerCase];
                if (personIdsList == null)
                    personIdsList = new ArrayList<>();
                personIdsList.add(personId);
                inverseIndex[intNormalizedNameLowerCase] = personIdsList;
            }
        }

    }

    private void writeIndexToFile(String indexFileName) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(indexFileName));
        for (int i = 0; i < COUNT; i++) {
            List<String> valuesList = inverseIndex[i];
            dataOut.writeInt(valuesList.size());
            for (int k = 0; k < valuesList.size(); ++k)
                dataOut.writeUTF(valuesList.get(k));
            if (i % 100000 == 0)
                dataOut.flush();
        }
        dataOut.flush();
        dataOut.close();
    }

    private void readIndexFromFile(String indexFileName) throws IOException {
        inverseIndex = new List[COUNT];
        DataInputStream dataIn = new DataInputStream(new FileInputStream(indexFileName));

        for (int i = 0; i < COUNT; ++i) {
            int size = dataIn.readInt();
            inverseIndex[i] = new ArrayList<String>(size);
            for (int k = 0; k < size; ++k)
                inverseIndex[i].add(dataIn.readUTF());
        }
    }

    public List get(int normalizedNameInt) {
        return inverseIndex[normalizedNameInt];
    }

}
