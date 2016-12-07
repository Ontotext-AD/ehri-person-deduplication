package com.ontotext.ehri.deduplication.indices;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Predicates implements Iterable<Integer> {

    private List<String> predicates;
    private List<String> queries;

    public Predicates() {

        predicates = Arrays.asList(
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
                "genderLinearClass",
                "genderRuleBased",
                "sourceId",
                "personType",
                "occupation",
                "nameMotherFirstName",
                "nameMotherLastName",
                "nationality",
                "daitchMokotoffEncoding"
        );

        queries = Arrays.asList(
                "?s ushmm:firstName ?o.",
                "?s ushmm:lastName ?o.",
                "?s onto:normalizedFirstName ?o.",
                "?s onto:normalizedLastName ?o.",
                "?s onto:normalizedName ?o.",
                "{?s ushmm:dateBirth ?o} union {?s ushmm:dateBirth / ushmm:date ?o}",
                "?s ushmm:placeBirth / ushmm:cityTown ?o.",
                "?s onto:nameDM ?o.",
                "?s onto:firstNameDM ?o.",
                "?s onto:lastNameDM ?o.",
                "?s ushmm:gender ?o.",
                "?s onto:gender ?g. ?g rdf:value ?o. ?g onto:provenance onto:gender-LinearClass.",
                "?s onto:gender ?g. ?g rdf:value ?o. ?g onto:provenance onto:gender-RuleBased.",
                "?s ushmm:documentedIn / ushmm:sourceId ?o.",
                "?s ushmm:personType ?o.",
                "?s ushmm:occupation ?o.",
                "?s ushmm:nameMother / ushmm:firstName ?o.",
                "?s ushmm:nameMother / ushmm:lastName ?o.",
                "?s ushmm:nationality / ushmm:nationality ?o.",
                "?s onto:daitchMokotoffEncoding ?o."
        );

    }

    public int size() {
        return predicates.size();
    }

    public int stringToInt(String predicate) {
        return predicates.indexOf(predicate);
    }

    public String intToString(int predicate) {
        return predicates.get(predicate);
    }

    public String getQueryForPredicate(int predicate) {
        return queries.get(predicate);
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {

            private int current = 0;

            @Override
            public boolean hasNext() {
                return current < predicates.size();
            }

            @Override
            public Integer next() {
                return current++;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

    }
}
