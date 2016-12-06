package com.ontotext.ehri.deduplication.clustering.indices;

import com.ontotext.ehri.deduplication.clustering.approximata.BuildMinAcyclicFSA;
import com.ontotext.ehri.deduplication.clustering.approximata.MinAcyclicFSA;
import com.ontotext.ehri.sparql.EndpointConnection;
import com.ontotext.ehri.sparql.QueryResultHandler;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class PersonIdIndex {
    public static void main(String[] args) throws Exception {

        File fout = new File("/home/nely/personIdDict.txt");
        FileOutputStream fos = new FileOutputStream(fout);

        OutputStreamWriter osw = new OutputStreamWriter(fos);
        EndpointConnection connection = new EndpointConnection();
        connection.open();
        TupleQuery query = connection.prepareSPARQLTupleQuery("" +
                "PREFIX ushmm: <http://data.ehri-project.eu/ushmm/ontology/>\n" +
                "select distinct ?personId where {\n" +
                "    ?s ushmm:personId ?personId\n" +
                "}");
        query.evaluate(new QueryResultHandler() {
            @Override
            public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
                try {
                    osw.write(bindingSet.getValue("personId").stringValue() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        connection.close();
        osw.close();

        BuildMinAcyclicFSA buildMinAcyclicFSA = new BuildMinAcyclicFSA();
        buildMinAcyclicFSA.sortFile(
                "/home/nely/personIdDict.txt",
                "UTF-8",
                "/home/nely/fwdPersonIdDict.txt",
                "UTF-8"
        );
        buildMinAcyclicFSA.buildMinAcyclicFSA(
                "/home/nely/fwdPersonIdDict.txt",
                "UTF-8",
                "true",
                "/home/nely/fwdPersonIdDict.bin"
        );
        MinAcyclicFSA fwd = MinAcyclicFSA.read(new File("/home/nely/fwdPersonIdDict.bin"));

    }
}
