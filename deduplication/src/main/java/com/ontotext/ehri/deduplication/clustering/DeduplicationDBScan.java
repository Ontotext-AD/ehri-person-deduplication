package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.model.USHMMPerson;
import com.ontotext.ehri.deduplication.sparql.EndpointConnection;
import com.ontotext.ehri.deduplication.sparql.QueryResultHandler;
import com.ontotext.ehri.deduplication.utils.SerializationUtils;
import org.openrdf.query.*;
import types.LinearClassifier;
import utils.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DeduplicationDBScan {
    public static void main(String[] args) throws QueryEvaluationException, TupleQueryResultHandlerException, IOException, ClassNotFoundException {
        LinearClassifier model = (LinearClassifier) IOUtils.loadModel(new File("/home/nelly/workspace/model.bin").toURI().toURL());
        DBSCANClusterer dbscanClusterer =  new DBSCANClusterer(0.001d, 5, new DistanceMeasure(model));
        Map<String, Map<String, Set<String>>> dataMap = getDataFromCache("/home/nelly/data.cache");
        List<USHMMPerson> data = getDataList(dataMap);
//        Map<String, Map<String, Set<String>>> statementsMap = (Map<String, Map<String, Set<String>>>) SerializationUtils.deserialize(
//                "/home/nelly/workspace/statementsMap.cache"
//        );
       // List<USHMMPerson> data = getData(statementsMap);
        List<Cluster> clusters = dbscanClusterer.cluster(data);
        printClustersStatsToSTDOut(clusters, data.size());
    }

    private static List<USHMMPerson> getData(Map<String, Map<String, Set<String>>> statementsMap) {
        return statementsMap.keySet().stream().map(
                personId -> new USHMMPerson(personId, statementsMap.get(personId))
        ).collect(Collectors.toList());
    }

    private static void printClustersStatsToSTDOut(List<Cluster> clusters, int totalPoints) {
        int minClusterSize = totalPoints, maxClusterSize = -1;
        int reachablePointsCount = 0;
        for (Cluster cluster : clusters)
        {
            int clusterSize = cluster.points.size();
            reachablePointsCount += clusterSize;
            if (minClusterSize > clusterSize) minClusterSize = clusterSize;
            if (maxClusterSize < clusterSize) maxClusterSize = clusterSize;
            printClusterStatsToSTDOut(cluster);
        }
        System.out.println("Reachable points : " + reachablePointsCount + " from " + totalPoints);
        System.out.println("Total clusters : " + clusters.size());
        System.out.println("Biggest cluster size : " + maxClusterSize);
        System.out.println("Smallest cluster size : " + minClusterSize);
    }

    private static void printClusterStatsToSTDOut(Cluster cluster) {
        System.out.println("----------------------");
        cluster.points.forEach(System.out::println);
        System.out.println("----------------------");
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
