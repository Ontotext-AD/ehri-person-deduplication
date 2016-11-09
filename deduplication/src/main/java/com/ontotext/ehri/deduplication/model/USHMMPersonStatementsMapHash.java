package com.ontotext.ehri.deduplication.model;

import com.ontotext.ehri.deduplication.sparql.EndpointConnection;
import com.ontotext.ehri.deduplication.sparql.QueryResultHandler;
import com.ontotext.ehri.deduplication.utils.SerializationUtils;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryException;

import java.io.File;
import java.util.*;

/**
 * An utility class for the hash map containing all necessary predicate values for the persons in the gold standard.
 * When executing the program for the first time the map is created via executing SPARQL queries to the EHRI endpoint.
 * After that the map is serialized in a file.
 */

class USHMMPersonStatementsMapHash {

    private static final String[] PREDICATE_NAMES_ARRAY = {
            "firstName",
            "lastName",
            "normalizedFirstName",
            "normalizedLastName",
            "normalizedName",
            "dateBirth",
            "placeBirth",
            "nameDM",
            "firstNameDM",
            "lastNameDM",
            "gender",
            "gender-LinearClass",
            "gender-RuleBased",
            "sourceId",
            "personType",
            "occupation",
            "nameMotherFirstName",
            "nameMotherLastName",
            "nationality",
    };

    private static final String[] PREDICATES_QUERIES_ARRAY = {
            "?s ushmm:firstName ?o.",
            "?s ushmm:lastName ?o.",
            "?s onto:normalizedFirstName ?o.",
            "?s onto:normalizedLastName ?o.",
            "?s onto:normalizedName ?o.",
            "?s ushmm:dateBirth / ushmm:date ?o.",
            "?s ushmm:placeBirth / ushmm:cityTown ?o.",
            "?s onto:nameDM ?o.",
            "?s onto:firstNameDM ?o.",
            "?s onto:lastNameDM ?o.",
            "?s ushmm:gender ?o.",
            "?s onto:gender ?g.\n?g rdf:value ?o.\n?g onto:provenance onto:gender-LinearClass.",
            "?s onto:gender ?g.\n?g rdf:value ?o.\n?g onto:provenance onto:gender-RuleBased.",
            "?source a ushmm:Source; ushmm:documents ?s; ushmm:sourceId ?o.",
            "?s ushmm:personType ?o.",
            "?s ushmm:occupation ?o.",
            "?s ushmm:nameMother / ushmm:firstName ?o.",
            "?s ushmm:nameMother / ushmm:lastName ?o.",
            "?s ushmm:nationality / ushmm:nationality ?o.",

    };

    private static List<String> PREDICATE_NAMES;
    private static List<String> PREDICATES_QUERIES;

    private Map<String, Map<String, Set<String>>> statementsMap;

    @SuppressWarnings("unchecked")
    USHMMPersonStatementsMapHash(List<USHMMGoldStandardEntry> goldStandard, String personStatementsMapCache) {

        PREDICATE_NAMES = Arrays.asList(PREDICATE_NAMES_ARRAY);
        PREDICATES_QUERIES = Arrays.asList(PREDICATES_QUERIES_ARRAY);
        File statementsMapCacheFile = new File(personStatementsMapCache);
        if (statementsMapCacheFile.exists())
            statementsMap = (Map<String, Map<String, Set<String>>>) SerializationUtils.deserialize(
                    personStatementsMapCache
            );
        else {
            EndpointConnection connection = new EndpointConnection();
            connection.open();
            statementsMap = getMap(goldStandard, personStatementsMapCache, connection);
            connection.close();
        }
    }

    USHMMPerson getPerson(String personId) {
        return new USHMMPerson(personId, statementsMap.get(personId));
    }

    private Map<String, Map<String, Set<String>>> getMap(List<USHMMGoldStandardEntry> goldStandard,
                                                             String personStatementsMapCache, EndpointConnection connection) {

        Map<String, Map<String, Set<String>>> statementsMap = new HashMap<>();
        Set<String> personsSetGoldStandard = getPersonsSetGoldStandard(goldStandard);

        for (String person : personsSetGoldStandard)
            for (String predicate : PREDICATE_NAMES)
                putPropertyInMap(statementsMap, person, predicate, connection);

        SerializationUtils.serialize(statementsMap, personStatementsMapCache);
        return statementsMap;
    }

    private Set<String> getPersonsSetGoldStandard(List<USHMMGoldStandardEntry> goldStandard) {
        Set<String> personsGoldStandard = new HashSet<>();
        for (USHMMGoldStandardEntry goldStandardEntry : goldStandard) {
            personsGoldStandard.add(goldStandardEntry.getPersonId1());
            personsGoldStandard.add(goldStandardEntry.getPersonId2());
        }
        return personsGoldStandard;
    }

    private void putPropertyInMap(Map<String, Map<String, Set<String>>> statementsMap, String person, String predicate,
                                  EndpointConnection connection) {
        Map<String, Set<String>> personStatementsMap = statementsMap.get(person);
        if (personStatementsMap == null)
            personStatementsMap = new HashMap<>();
        personStatementsMap.put(predicate, getStatementObject(person, predicate, connection));
        statementsMap.put(person, personStatementsMap);
    }

    private Set<String> getStatementObject(String personId, String predicate, EndpointConnection connection) {
        Set<String> resultBindingSet = new HashSet<>();
        tryToPrepareAndEvaluateQuery(
                personId, PREDICATES_QUERIES.get(PREDICATE_NAMES.indexOf(predicate)), resultBindingSet, connection
        );
        return resultBindingSet;
    }

    private void tryToPrepareAndEvaluateQuery(String personId, String predicate, Set<String> resultBindingSet, EndpointConnection connection) {
        try {
            prepareAndEvaluateQuery(personId, predicate, resultBindingSet, connection);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void prepareAndEvaluateQuery(String personId, String predicate, Set<String> resultBindingSet, EndpointConnection connection)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException {
        TupleQuery query = connection.prepareSPARQLTupleQuery(
                "PREFIX onto: <http://data.ehri-project.eu/ontotext/>\n" +
                "PREFIX ushmm: <http://data.ehri-project.eu/ushmm/ontology/>\n" +
                "select ?o where {\n" +
                "    ?s a ushmm:Person.\n" +
                "    ?s ushmm:personId \"" + personId + "\".\n" +
                "    " + predicate + "\n" +
                "}"
        );
        query.evaluate(new USHMMQueryResultHandler(resultBindingSet));
    }

    private static class USHMMQueryResultHandler extends QueryResultHandler {
        private Set<String> resultBindingSet;

        USHMMQueryResultHandler(Set<String> resultBindingSet) {
            this.resultBindingSet = resultBindingSet;
        }

        @Override
        public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
            for (Binding binding : bindingSet)
                resultBindingSet.add(binding.getValue().stringValue().toLowerCase());
        }
    }
}
