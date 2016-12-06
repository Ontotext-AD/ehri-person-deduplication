package com.ontotext.ehri.deduplication.clustering.indices;

import com.ontotext.ehri.deduplication.clustering.approximata.MinAcyclicFSA;
import com.ontotext.ehri.sparql.EndpointConnection;
import com.ontotext.ehri.sparql.QueryResultHandler;
import org.apache.log4j.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class USHMMPersonIndex implements Iterable<String> {

    private static final Logger logger = Logger.getLogger(USHMMPersonIndex.class);

    private static final String EMPTY_STRING = "";

    protected List[][] index;
    private MinAcyclicFSA personIdFSA;
    private PredicateIndex predicateIntCache;

    public final int size = 3622508;

    public USHMMPersonIndex(String personIdFSABin, String indexFileName) throws IOException, ClassNotFoundException, QueryEvaluationException, TupleQueryResultHandlerException {

        personIdFSA = MinAcyclicFSA.read(new File(personIdFSABin));
        predicateIntCache = new PredicateIndex();

        File indexFile = new File(indexFileName);
        if (indexFile.exists()) {
            logger.info("Begin read index");
            long startExecution = System.currentTimeMillis();
            readIndexFromFile(indexFileName);
            long executionTime = System.currentTimeMillis() - startExecution;
            logger.info("Finish read index in " + TimeUnit.MILLISECONDS.toMinutes(executionTime) + " minutes");
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

    private void buildIndex() throws QueryEvaluationException, TupleQueryResultHandlerException, IOException {
        index = new List[size][predicateIntCache.PREDICATES_COUNT];
        for (int i = 0; i < size; ++i)
            for (int j = 0; j < predicateIntCache.PREDICATES_COUNT; ++j)
                index[i][j] = new ArrayList<String>(1);

        for (String predicate : predicateIntCache.predicates) {
            EndpointConnection connection = new EndpointConnection();
            connection.open();
            TupleQuery query = connection.prepareSPARQLTupleQuery("" +
                    "PREFIX ushmm: <http://data.ehri-project.eu/ushmm/ontology/>\n" +
                    "PREFIX onto: <http://data.ehri-project.eu/ontotext/>\n" +
                    "select ?personId ?o where {\n" +
                    "    ?s ushmm:personId ?personId.\n" +
                    predicateIntCache.predicateQuery(predicate) +
                    "}\n");
            query.evaluate(new QueryResultHandler() {
                @Override
                public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
                    int personInt = personIdFSA.stringToInt(bindingSet.getValue("personId").stringValue());
                    int predicateInt = predicateIntCache.predicateToInt(predicate);
                    index[personInt][predicateInt].add(bindingSet.getValue("o").stringValue());
                }
            });
            connection.close();
        }

    }

    private void writeIndexToFile(String indexFileName) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(indexFileName));
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < predicateIntCache.PREDICATES_COUNT; ++j) {
                List<String> valuesList = index[i][j];
                dataOut.writeInt(valuesList.size());
                for (int k = 0; k < valuesList.size(); ++k)
                    dataOut.writeUTF(valuesList.get(k));
            }
            if (i % 100000 == 0)
                dataOut.flush();
        }
        dataOut.flush();
        dataOut.close();
    }

    private void readIndexFromFile(String indexFileName) throws IOException {
        index = new List[size][predicateIntCache.PREDICATES_COUNT];
        DataInputStream dataIn = new DataInputStream(new FileInputStream(indexFileName));

        for (int i = 0; i < size; ++i)
            for (int j = 0; j < predicateIntCache.PREDICATES_COUNT; ++j) {
                int size = dataIn.readInt();
                index[i][j] = new ArrayList<String>(size);
                for (int k = 0; k < size; ++k)
                    index[i][j].add(dataIn.readUTF());
            }

    }

    public List[] getPerson(String personId) {
        return index[personIdFSA.stringToInt(personId)];
    }


    public String getValueLowerCase(int personIdInt, String predicate) {
        List<String> valuesList = index[personIdInt][predicateIntCache.predicateToInt(predicate)];
        if (valuesList.size() > 0)
            return valuesList.get(0).toLowerCase();
        else
            return EMPTY_STRING;
    }

    public String getValueLowerCase(String personId, String predicate) {
        List<String> valuesList = index[personIdFSA.stringToInt(personId)][predicateIntCache.predicateToInt(predicate)];
        if (valuesList.size() > 0)
            return valuesList.get(0).toLowerCase();
        else
            return EMPTY_STRING;
    }

    public List<String> getValuesLowerCase(String personId, String predicate) {
        List<String> valuesList = index[personIdFSA.stringToInt(personId)][predicateIntCache.predicateToInt(predicate)];
        if (valuesList.size() > 0)
            return valuesList.stream().map(String::toLowerCase).collect(Collectors.toList());
        else
            return Collections.emptyList();
    }

    @Override
    public Iterator iterator() {
        Iterator<String> it = new Iterator<String>() {

            private int current = 0;

            @Override
            public boolean hasNext() {
                return current < size;
            }

            @Override
            public String next() {
                return personIdFSA.intToString(current++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

        return it;
    }


}
