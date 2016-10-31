package com.ontotext.ehri.deduplication.model;

public class USHMMPerson {

    String firstName;
    String lastName;
    String normalizedFirstName;
    String normalizedLastName;
    String normalizedName;
    String dateBirth;
    String placeBirth;
    String nameDM;
    String firstNameDM;
    String lastNameDM;
    String gender;
    String genderLinearClass;
    String genderRuleBased;
    String sourceId;
    String personType;
    String occupation;
    String nameMotherFirstName;
    String nameMotherLastName;

    public USHMMPerson(String ... args) {
        this.firstName = args[0];
        this.lastName = args[1];
        this.normalizedFirstName = args[2];
        this.normalizedLastName = args[3];
        this.normalizedName = args[4];
        this.dateBirth = args[5];
        this.placeBirth = args[6];
        this.nameDM = args[7];
        this.firstNameDM = args[8];
        this.lastNameDM = args[9];
        this.gender = args[10];
        this.genderLinearClass = args[11];
        this.genderRuleBased = args[12];
        this.sourceId = args[13];
        this.personType = args[14];
        this.occupation = args[15];
        this.nameMotherFirstName = args[16];
        this.nameMotherLastName = args[17];
    }

}
