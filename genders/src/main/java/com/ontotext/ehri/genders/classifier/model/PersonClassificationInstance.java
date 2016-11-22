package com.ontotext.ehri.genders.classifier.model;

import com.ontotext.ehri.normalization.USHMMNationalityNormalization;
import com.ontotext.ehri.normalization.USHMMPersonNameNormalization;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PersonClassificationInstance {

    private static String SPACE = " ";

    public String firstName;
    public String lastName;
    public String name;

    public String normalizedFirstName;
    public String normalizedLastName;
    public String normalizedName;

    public String gender;
    public Set<String> nationalitiesSet;

    public PersonClassificationInstance(String firstName, String lastName, Set<String> nationalitiesSet) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.name = firstName + SPACE + lastName;

        this.normalizedFirstName = getNameAsKey(USHMMPersonNameNormalization.normalize(firstName));
        this.normalizedLastName = getNameAsKey(USHMMPersonNameNormalization.normalize(lastName));
        this.normalizedName = getNameAsKey(USHMMPersonNameNormalization.normalize(name));

        this.nationalitiesSet = new HashSet<>(nationalitiesSet.size());
        this.nationalitiesSet.addAll(nationalitiesSet.stream().map(USHMMNationalityNormalization::normalize).collect(Collectors.toList()));
    }

    private static String getNameAsKey(String name) {
        String[] names = name.split(SPACE);
        String nameAsKey = "";
        for (String n : names)
            if (n.length() > 1)
                nameAsKey += n + SPACE;
        return nameAsKey.trim();
    }

    public PersonClassificationInstance(String firstName, String lastName, String gender, Set<String> nationality) {
        this(firstName, lastName, nationality);
        this.gender = gender;
    }

}
