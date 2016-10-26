package com.ontotext.ehri.deduplication.model;

import com.ontotext.ehri.deduplication.sparql.QueryResultHandler;
import com.ontotext.ehri.deduplication.sparql.SPARQLEndpointConfig;
import com.ontotext.ehri.deduplication.utils.SerializationUtils;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

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
    };

    private static List<String> PREDICATE_NAMES;
    private static List<String> PREDICATES_QUERIES;

    private RepositoryConnection connection;
    private HTTPRepository repository;
    private Map<String, HashMap<String, String>> statementsMap;

    @SuppressWarnings("unchecked")
    USHMMPersonStatementsMapHash(List<USHMMGoldStandardEntry> goldStandard, String personStatementsMapCache) {

        PREDICATE_NAMES = Arrays.asList(PREDICATE_NAMES_ARRAY);
        PREDICATES_QUERIES = Arrays.asList(PREDICATES_QUERIES_ARRAY);
        File statementsMapCacheFile = new File(personStatementsMapCache);
        if (statementsMapCacheFile.exists())
            statementsMap = (Map<String, HashMap<String, String>>) SerializationUtils.deserialize(
                    personStatementsMapCache
            );
        else {
            openConnection();
            statementsMap = getMap(goldStandard, personStatementsMapCache);
            closeConnection();
        }
    }

    private void openConnection() {
        SPARQLEndpointConfig config = new SPARQLEndpointConfig();
        this.repository = new HTTPRepository(config.repositoryURL);
        this.repository.setUsernameAndPassword(config.username, config.password);
        this.connection = null;
        try {
            this.connection = repository.getConnection();
        } catch (RepositoryException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void closeConnection() {
        try {
            connection.close();
            repository.shutDown();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    String get(String person, String predicate) {
        return statementsMap.get(person).get(predicate);
    }

    private Map<String, HashMap<String, String>> getMap(List<USHMMGoldStandardEntry> goldStandard, String personStatementsMapCache) {

        Map<String, HashMap<String, String>> statementsMap = new HashMap<>();
        Set<String> personsSetGoldStandard = getPersonsSetGoldStandard(goldStandard);

        for (String person : personsSetGoldStandard)
            for (String predicate : PREDICATE_NAMES)
                putPropertyInMap(statementsMap, person, predicate);

        SerializationUtils.serialize(statementsMap, personStatementsMapCache);
        return statementsMap;
    }

    private Set<String> getPersonsSetGoldStandard(List<USHMMGoldStandardEntry> goldStandard) {
        Set<String> personsGoldStandard = new HashSet<>();
        for (USHMMGoldStandardEntry goldStandardEntry : goldStandard) {
            personsGoldStandard.add(goldStandardEntry.personId1);
            personsGoldStandard.add(goldStandardEntry.personId2);
        }
        return personsGoldStandard;
    }

    private void putPropertyInMap(Map<String, HashMap<String, String>> statementsMap, String person, String predicate) {
        HashMap<String, String> personStatementsMap = statementsMap.get(person);
        if (personStatementsMap == null)
            personStatementsMap = new HashMap<>();
        personStatementsMap.put(predicate, getStatementObject(person, predicate).toLowerCase());
        statementsMap.put(person, personStatementsMap);
    }

    private String getStatementObject(String personId, String predicate) {
        Set<String> resultBindingSet = new HashSet<>();
        tryToPrepareAndEvaluateQuery(
                personId, PREDICATES_QUERIES.get(PREDICATE_NAMES.indexOf(predicate)), resultBindingSet
        );
        if (resultBindingSet.iterator().hasNext())
            return resultBindingSet.iterator().next();
        else
            return "";
    }

    private void tryToPrepareAndEvaluateQuery(String personId, String predicate, Set<String> results) {
        try {
            prepareAndEvaluateQuery(personId, predicate, results);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void prepareAndEvaluateQuery(String personId, String predicate, Set<String> results)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException, TupleQueryResultHandlerException {
        TupleQuery query = prepareQuery(personId, predicate);
        query.evaluate(new USHMMQueryResultHandler(results));
    }

    private TupleQuery prepareQuery(String personId, String predicate) throws RepositoryException, MalformedQueryException {
        return connection.prepareTupleQuery(QueryLanguage.SPARQL, "" +
                "PREFIX onto: <http://data.ehri-project.eu/ontotext/>\n" +
                "PREFIX ushmm: <http://data.ehri-project.eu/ushmm/ontology/>\n" +
                "select ?o where {\n" +
                "    ?s a ushmm:Person.\n" +
                "    ?s ushmm:personId \"" + personId + "\".\n" +
                "    " + predicate + "\n" +
                "}");
    }

    private static class USHMMQueryResultHandler extends QueryResultHandler {
        private Set<String> resultBindingSet;

        USHMMQueryResultHandler(Set<String> results) {
            this.resultBindingSet = results;
        }

        @Override
        public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
            if (bindingSet.iterator().hasNext())
                resultBindingSet.add(bindingSet.iterator().next().getValue().stringValue());
        }
    }
}
