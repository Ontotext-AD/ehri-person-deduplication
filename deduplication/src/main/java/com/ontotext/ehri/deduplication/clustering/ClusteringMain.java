package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.model.USHMMPerson;
import com.ontotext.ehri.deduplication.sparql.EndpointConnection;
import com.ontotext.ehri.deduplication.sparql.QueryResultHandler;
import com.ontotext.ehri.deduplication.utils.SerializationUtils;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import types.LinearClassifier;
import utils.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ClusteringMain {

    private static final double epsilon = 0.1d;
    private static final int minimalPoints = 2;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Namespace ns = getNamespace(args);
        if (ns != null) {
            String modelFilepath = ns.getString("modelFilepath");
            String dataFileCache = ns.getString("dataFileCache");
            String resultsDir = ns.getString("resultsDir");
            clusterData(modelFilepath, dataFileCache, resultsDir);
        }
    }

    private static Namespace getNamespace(String[] args) {
        ArgumentParser parser = getArgumentParser();
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
        return ns;
    }

    private static ArgumentParser getArgumentParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("ehri-person-clustering");
        parser.addArgument("modelFilepath").help("Bin file to save the model");
        parser.addArgument("dataFileCache").help("Data file cache");
        parser.addArgument("resultsDir").help("Directory to save the results");
        return parser;
    }

    private static void clusterData(String modelFilepath, String dataFileCache, String resultsDir) throws IOException, ClassNotFoundException {
        LinearClassifier model = (LinearClassifier) IOUtils.loadModel(new File(modelFilepath).toURI().toURL());
        DBSCANClustering dbScan =  new DBSCANClustering(epsilon, minimalPoints, new DistanceMeasure(model));
//        Map<String, Map<String, Set<String>>> dataMap = getDataFromCache(dataFileCache);
//        List<USHMMPerson> data = getDataList(dataMap);
        Map<String, Map<String, Set<String>>> statementsMap = (Map<String, Map<String, Set<String>>>) SerializationUtils.deserialize(dataFileCache);
        List<USHMMPerson> data = getData(statementsMap);
        List<Cluster> clusters = dbScan.cluster(data);
        ClusteringResultsWriter.printResults(resultsDir, clusters, data, epsilon, minimalPoints);
    }

    private static List<USHMMPerson> getData(Map<String, Map<String, Set<String>>> statementsMap) {
        return statementsMap.keySet().stream().map(
                personId -> new USHMMPerson(personId, statementsMap.get(personId))
        ).collect(Collectors.toList());
    }

    private static List<USHMMPerson> getDataList(Map<String, Map<String, Set<String>>> dataMap) {
        return dataMap.keySet().stream().map(
                personId -> new USHMMPerson(personId, dataMap.get(personId))
        ).collect(Collectors.toList());
    }

    private static Map<String, Map<String, Set<String>>> getDataFromCache(String cacheFileName) throws QueryEvaluationException, TupleQueryResultHandlerException {
        File cacheFile = new File(cacheFileName);
        if (cacheFile.exists())
            return (Map<String, Map<String, Set<String>>>) SerializationUtils.deserialize(cacheFileName);
        Map<String, Map<String, Set<String>>> data = getDataSPARQL();
        SerializationUtils.serialize(data, cacheFileName);
        return data;
    }

    private static Map<String, Map<String, Set<String>>> getDataSPARQL() throws QueryEvaluationException, TupleQueryResultHandlerException {
        Map<String, Map<String, Set<String>>> data = new HashMap<>();
        EndpointConnection connection = new EndpointConnection();
        connection.open();
        TupleQuery query = connection.prepareSPARQLTupleQuery("" +
                "PREFIX ushmm: <http://data.ehri-project.eu/ushmm/ontology/>\n" +
                "PREFIX onto: <http://data.ehri-project.eu/ontotext/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "select ?personId ?firstName ?lastName ?normalizedFirstName ?normalizedLastName ?normalizedName ?dateBirth ?placeBirth ?nameDM ?firstNameDM ?lastNameDM ?gender ?genderLinearClass ?genderRuleBased ?sourceId ?personType ?occupation ?nameMotherFirstName ?nameMotherLastName ?nationality where {\n" +
                "    ?person a ushmm:Person;\n" +
                "        onto:normalizedLastName \"Klein\";\n" +
                "        ushmm:personId ?personId.\n" +
                "    ?person ushmm:lastName ?lastName.\n" +
                "    ?person onto:normalizedName ?normalizedName.\n" +
                "    optional {\n" +
                "        ?person ushmm:firstName ?firstName\n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person onto:normalizedFirstName ?normalizedFirstName \n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person ushmm:dateBirth / ushmm:date ?dateBirth \n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person ushmm:placeBirth / ushmm:cityTown ?placeBirth \n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person onto:nameDM ?nameDM \n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person onto:firstNameDM ?firstNameDM \n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person ushmm:gender ?gender \n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person onto:gender ?g.\n" +
                "        ?g rdf:value ?genderLinearClass.\n" +
                "        ?g onto:provenance onto:gender-LinearClass \n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person onto:gender ?g.\n" +
                "        ?g rdf:value ?genderRuleBased.\n" +
                "        ?g onto:provenance onto:gender-RuleBased \n" +
                "    }\n" +
                "    optional {\n" +
                "        ?source a ushmm:Source;\n" +
                "            ushmm:documents ?person;\n" +
                "            ushmm:sourceId ?sourceId \n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person ushmm:personType ?personType \n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person ushmm:occupation ?occupation \n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person ushmm:nameMother / ushmm:firstName ?nameMotherFirstName \n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person ushmm:nameMother / ushmm:lastName ?nameMotherLastName \n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person ushmm:nationality / ushmm:nationality ?nationality \n" +
                "    }\n" +
                "    bind(\"Klein\" as ?normalizedLastName)\n" +
                "    bind(\"KLN\" as ?lastNameDM)\n" +
                "}\n");
        query.evaluate(new DeduplicationHandler(data));
        connection.close();
        return data;
    }

    private static class DeduplicationHandler extends QueryResultHandler {
        Map<String, Map<String, Set<String>>> data;

        DeduplicationHandler(Map<String, Map<String, Set<String>>> data) {
            this.data = data;
        }

        @Override
        public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
            String personId = bindingSet.getValue("personId").stringValue();
            Map<String, Set<String>> personStatementsMap = data.get(personId);
            if (personStatementsMap == null) personStatementsMap = new HashMap<>();
            for (String binding : bindingSet.getBindingNames())
                if (bindingSet.hasBinding(binding)) {
                    Set<String> set = personStatementsMap.get(binding);
                    if (set == null)
                        set = new HashSet<>();
                    set.add(bindingSet.getValue(binding).stringValue().toLowerCase());
                    personStatementsMap.put(binding, set);
                }
                else
                {
                    Set<String> set = personStatementsMap.get(binding);
                    if (set == null)
                        set = new HashSet<>();
                    set.add("");
                    personStatementsMap.put(binding, set);
                }
            data.put(personId, personStatementsMap);
        }
    }

}
