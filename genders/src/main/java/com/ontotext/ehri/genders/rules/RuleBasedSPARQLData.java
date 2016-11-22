package com.ontotext.ehri.genders.rules;

import com.ontotext.ehri.sparql.EndpointConnection;
import com.ontotext.ehri.sparql.QueryResultHandler;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RuleBasedSPARQLData {

    static Map<String, List<String>> getData () throws QueryEvaluationException, TupleQueryResultHandlerException {
        Map<String, List<String>> allData = new HashMap<>();

        EndpointConnection connection = new EndpointConnection();
        connection.open();
        TupleQuery query = connection.prepareSPARQLTupleQuery("" +
                "PREFIX ushmm: <http://data.ehri-project.eu/ushmm/ontology/>\n" +
                "PREFIX onto: <http://data.ehri-project.eu/ontotext/>\n" +
                "select ?personId ?gender ?firstName ?lastName ?firstNameTransliterated ?lastNameTransliterated where {\n" +
                "    ?person a ushmm:Person; ushmm:personId ?personId.\n" +
                "    optional {?person ushmm:gender ?gender}\n" +
                "    optional {?person onto:normalizedFirstName ?firstName}\n" +
                "    optional {?person onto:normalizedLastName ?lastName}\n" +
                "    optional {?person ushmm:nameTransliterated / ushmm:firstName ?firstNameTransliterated}\n" +
                "    optional {?person ushmm:nameTransliterated / ushmm:lastName ?lastNameTransliterated}\n" +
                "}");
        query.evaluate(new QueryResultHandler() {
            @Override
            public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
                List<String> dataList = new ArrayList<>(5);
                String gender = getValue(bindingSet, "gender");
                String firstName = getValue(bindingSet, "firstName");
                String lastName = getValue(bindingSet, "lastName");
                String firstNameTransliterated = getValue(bindingSet, "firstNameTransliterated");
                String lastNameTransliterated = getValue(bindingSet, "lastNameTransliterated");
                dataList.add(gender); dataList.add(firstName); dataList.add(lastName); dataList.add(firstNameTransliterated); dataList.add(lastNameTransliterated);
                allData.put(bindingSet.getValue("personId").stringValue(), dataList);
            }
        });

        connection.close();
        return allData;
    }

    private static String getValue(BindingSet bindingSet, String bindingName) {
        String value = "";
        if (bindingSet.hasBinding(bindingName))
            value = bindingSet.getValue(bindingName).stringValue();
        return value;
    }

}
