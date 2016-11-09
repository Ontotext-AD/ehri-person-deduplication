package com.ontotext.ehri.genders.classifier.model;

import com.ontotext.ehri.normalization.USHMMNationalityNormalization;
import com.ontotext.ehri.normalization.USHMMPersonNameNormalization;

public class PersonClassificationInstance {

    private static String SPACE = " ";

    public String firstName;
    public String lastName;
    public String name;

    public String normalizedFirstName;
    public String normalizedLastName;
    public String normalizedName;

    public String gender;
    public String nationality;

    public PersonClassificationInstance(String firstName, String lastName, String nationality) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.name = firstName + SPACE + lastName;

        this.normalizedFirstName = getNameAsKey(USHMMPersonNameNormalization.normalize(firstName));
        this.normalizedLastName = getNameAsKey(USHMMPersonNameNormalization.normalize(lastName));
        this.normalizedName = getNameAsKey(USHMMPersonNameNormalization.normalize(name));

        this.nationality = USHMMNationalityNormalization.normalize(nationality);
    }

    private static String getNameAsKey(String name) {
        String[] names = name.split(SPACE);
        String nameAsKey = "";
        for (String n : names)
            if (n.length() > 1)
                nameAsKey += n + SPACE;
        return nameAsKey.trim();
    }

    public PersonClassificationInstance(String firstName, String lastName, String gender, String nationality) {
        this(firstName, lastName, nationality);
        this.gender = gender;
    }

}
