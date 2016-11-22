package com.ontotext.ehri.genders.classifier.model;

import com.ontotext.ehri.deduplication.sparql.EndpointConnection;
import com.ontotext.ehri.deduplication.sparql.QueryResultHandler;
import com.ontotext.ehri.deduplication.utils.SerializationUtils;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;

import java.io.File;
import java.util.*;

public class PersonGenderInputDataCache {

    public static List<PersonClassificationInstance> getData(String inputDataFileCache) throws QueryEvaluationException, TupleQueryResultHandlerException {

        Map<String, Map<String, Set<String>>> personIdPredicateObjectValueSetMap = getPersonIdPredicateObjectValueSetMap(inputDataFileCache);
        return getPersonClassificationInstancesList(personIdPredicateObjectValueSetMap);

    }

    private static Map<String, Map<String, Set<String>>> getPersonIdPredicateObjectValueSetMap(String inputDataFileCache) throws QueryEvaluationException, TupleQueryResultHandlerException {
        File inputDataCacheFile = new File(inputDataFileCache);
        if (inputDataCacheFile.exists())
            return (Map<String, Map<String, Set<String>>>) SerializationUtils.deserialize(inputDataFileCache);
        else {
            Map<String, Map<String, Set<String>>> personIdPredicateObjectValueSetMap = createPersonIdPredicateObjectValueSetMap();
            SerializationUtils.serialize(personIdPredicateObjectValueSetMap, inputDataFileCache);
            return personIdPredicateObjectValueSetMap;
        }
    }

    private static Map<String, Map<String, Set<String>>> createPersonIdPredicateObjectValueSetMap() throws QueryEvaluationException, TupleQueryResultHandlerException {
        Map<String, Map<String, Set<String>>> personIdPredicateObjectValueSetMap = new HashMap<>();

        EndpointConnection connection = new EndpointConnection();
        connection.open();
        TupleQuery query = connection.prepareSPARQLTupleQuery("" +
                "PREFIX ushmm: <http://data.ehri-project.eu/ushmm/ontology/>\n" +
                "select ?personId ?gender ?firstName ?lastName ?nationality where {\n" +
                "    ?person a ushmm:Person;\n" +
                "        ushmm:personId ?personId.\n" +
                "    ?person ushmm:gender ?gender.\n" +
                "    optional {\n" +
                "        ?person ushmm:firstName ?firstName\n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person ushmm:lastName ?lastName\n" +
                "    }\n" +
                "    optional {\n" +
                "        ?person ushmm:nationality / ushmm:nationality ?nationality\n" +
                "    }\n" +
                "}");
        query.evaluate(new QueryResultHandler() {
            @Override
            public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
                String personId = bindingSet.getValue("personId").stringValue();
                Map<String, Set<String>> predicateObjectValueSetMap = getPredicateObjectValueSetMap(bindingSet, personId);
                personIdPredicateObjectValueSetMap.put(personId, predicateObjectValueSetMap);

            }

            private Map<String, Set<String>> getPredicateObjectValueSetMap(BindingSet bindingSet, String personId) {
                Map<String, Set<String>> predicateObjectValueSetMap = personIdPredicateObjectValueSetMap.get(personId);
                if (predicateObjectValueSetMap == null)
                    predicateObjectValueSetMap = new HashMap<>();
                for (String bindingName : bindingSet.getBindingNames())
                    putObjectValueSetInPredicateObjectValueSetMap(bindingSet, bindingName, predicateObjectValueSetMap);
                return predicateObjectValueSetMap;
            }

            private void putObjectValueSetInPredicateObjectValueSetMap(BindingSet bindingSet, String bindingName, Map<String, Set<String>> predicateObjectValueSetMap) {
                Set<String> objectValueSet = predicateObjectValueSetMap.get(bindingName);
                if (objectValueSet == null)
                    objectValueSet = new HashSet<>();
                objectValueSet.add(getBindingNameValue(bindingSet, bindingName));
                predicateObjectValueSetMap.put(bindingName, objectValueSet);
            }

            private String getBindingNameValue(BindingSet bindingSet, String bindingName) {
                String value = "";
                if (bindingSet.hasBinding(bindingName))
                    value = bindingSet.getValue(bindingName).stringValue();
                return value;
            }
        });

        connection.close();
        return personIdPredicateObjectValueSetMap;
    }

    private static List<PersonClassificationInstance> getPersonClassificationInstancesList(Map<String, Map<String, Set<String>>> personIdPredicateObjectValueSetMap) {
        List<PersonClassificationInstance> personClassificationInstancesList = new ArrayList<>();
        for (String personId : personIdPredicateObjectValueSetMap.keySet()) {
            Map<String, Set<String>> predicateObjectValueSetMap = personIdPredicateObjectValueSetMap.get(personId);
            personClassificationInstancesList.add(new PersonClassificationInstance(
                    predicateObjectValueSetMap.get("firstName").iterator().next(),
                    predicateObjectValueSetMap.get("lastName").iterator().next(),
                    predicateObjectValueSetMap.get("gender").iterator().next(),
                    predicateObjectValueSetMap.get("nationality")
            ));
        }
        return personClassificationInstancesList;
    }

}
