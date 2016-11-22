package com.ontotext.ehri.genders.classifier.labeling;

import com.ontotext.ehri.sparql.EndpointConnection;
import com.ontotext.ehri.sparql.QueryResultHandler;
import com.ontotext.ehri.genders.classifier.model.PersonClassificationInstance;
import com.ontotext.ehri.genders.classifier.model.PersonGenderFeatureExtractor;
import javafx.util.Pair;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import types.Alphabet;
import types.LinearClassifier;
import types.SparseVector;

import java.io.FileNotFoundException;
import java.util.*;

class PersonGenderClassifierLabeler {

    static Map<String, Pair<String, Double>> getLabeledData(LinearClassifier model) throws FileNotFoundException, QueryEvaluationException, TupleQueryResultHandlerException {
        Map<String, Map<String, Set<String>>> dataMap = getDataToLabel();
        return labelData(dataMap, model);
    }

    private static Map<String, Map<String, Set<String>>> getDataToLabel() throws QueryEvaluationException, TupleQueryResultHandlerException {
        Map<String, Map<String, Set<String>>> personIdPredicateObjectValueSetMap = new HashMap<>();

        EndpointConnection connection = new EndpointConnection();
        connection.open();
        TupleQuery query = connection.prepareSPARQLTupleQuery("" +
                "PREFIX ushmm: <http://data.ehri-project.eu/ushmm/ontology/>\n" +
                "select ?personId ?firstName ?lastName ?nationality where {\n" +
                "    ?person a ushmm:Person;\n" +
                "        ushmm:personId ?personId.\n" +
                "    filter not exists {\n" +
                "        ?person ushmm:gender ?gender\n" +
                "    }\n" +
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

    private static Map<String, Pair<String, Double>> labelData(Map<String, Map<String, Set<String>>> personIdPredicateObjectValueSetMap, LinearClassifier model) {
        Map<String, Pair<String, Double>> labeledData = new HashMap<>();

        for (String personId:personIdPredicateObjectValueSetMap.keySet() ) {
            Map<String, Set<String>> predicateObjectValueSetMap = personIdPredicateObjectValueSetMap.get(personId);
            PersonClassificationInstance p = new PersonClassificationInstance(
                    predicateObjectValueSetMap.get("firstName").iterator().next(),
                    predicateObjectValueSetMap.get("lastName").iterator().next(),
                    predicateObjectValueSetMap.get("nationality")
            );
            addLabeledRecordToData(labeledData, model, predicateObjectValueSetMap.get("personId").iterator().next(), p);
        }
        return labeledData;
    }


    private static void addLabeledRecordToData(Map<String, Pair<String, Double>> labeledData, LinearClassifier model, String personId, PersonClassificationInstance p) {
        Alphabet xA = model.getxAlphabet(), yA = model.getyAlphabet();
        PersonGenderFeatureExtractor featureExtractor = new PersonGenderFeatureExtractor(p);
        SparseVector sparseVector = featureExtractor.getSparseVector(xA);
        String label = yA.lookupInt(model.label(sparseVector));

        if (p.normalizedName.contains(" ") && p.normalizedName.length() > 5)
            labeledData.put(personId, new Pair<>(label, model.labelScoreNormalized(sparseVector).get(label)));
    }

}
