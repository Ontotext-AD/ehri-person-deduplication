package com.ontotext.ehri.deduplication.model;

import com.ontotext.ehri.deduplication.measures.Levenshtein;
import com.ontotext.ehri.deduplication.measures.USHMMDate;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.DaitchMokotoffSoundex;
import org.apache.commons.codec.language.bm.BeiderMorseEncoder;
import org.simmetrics.metrics.JaroWinkler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.Alphabet;
import types.SparseVector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class USHMMClassificationInstance {

    private Alphabet xA;
    private SparseVector sparseVector;

    private USHMMPerson person1;
    private USHMMPerson person2;

    private static final JaroWinkler jaroWinkler = new JaroWinkler();
    private static final DaitchMokotoffSoundex daitchMokotoffSoundex = new DaitchMokotoffSoundex();
    private static final BeiderMorseEncoder beiderMorseEncoder = new BeiderMorseEncoder();

    private static final Logger logger = LoggerFactory.getLogger(USHMMPersonsFeatureExtractor.class);

    USHMMClassificationInstance(Alphabet xA, USHMMPerson person1, USHMMPerson person2) {
        this.xA = xA;
        this.sparseVector = new SparseVector();

        this.person1 = person1;
        this.person2 = person2;
    }

    SparseVector getSparseVector() {
        extractNamesFeatures();
        extractMotherNameFeatures();
        extractPlaceBirthFeatures();
        extractDateBirthFeatures();
        extractGenderFeature();
        extractSourceFeature();
        extractPersonTypeFeature();
        extractOccupationFeature();
        return sparseVector;
    }

    private void extractNamesFeatures() {
        extractJaroWinklerNamesFeatures();
        extractJaroWinklerNormalizedNamesFeatures();
        extractDoubleMetaphoneFeatures();
        extractBeiderMorseFeatures();
        extractDaitchMokotoffFeatures();
    }

    private void extractJaroWinklerNamesFeatures() {
        sparseVector.add(xA.lookupObject("jwf"), jaroWinkler.compare(person1.firstName, person2.firstName));
        sparseVector.add(xA.lookupObject("jwl"), jaroWinkler.compare(person1.lastName, person2.lastName));

        sparseVector.add(xA.lookupObject("jw"), jaroWinkler.compare(person1.firstName + " " + person1.lastName,
                person2.firstName + " " + person2.lastName));
    }

    private void extractJaroWinklerNormalizedNamesFeatures() {
        sparseVector.add(xA.lookupObject("jwnf"), jaroWinkler.compare(person1.normalizedFirstName, person2.normalizedFirstName));
        sparseVector.add(xA.lookupObject("jwnl"), jaroWinkler.compare(person1.normalizedLastName, person2.normalizedLastName));
        sparseVector.add(xA.lookupObject("jwn"), jaroWinkler.compare(person1.normalizedName, person2.normalizedName));
    }

    private void extractDoubleMetaphoneFeatures() {
        sparseVector.add(xA.lookupObject("dmn"), person1.nameDM.equals(person2.nameDM) ? 1.0d : 0.0d);
        sparseVector.add(xA.lookupObject("dmfn"), (person1.firstNameDM.equals(person2.firstNameDM)) ? 1.0d : 0.0d);
        sparseVector.add(xA.lookupObject("dmln"), (person1.lastNameDM.equals(person2.lastNameDM)) ? 1.0d : 0.0d);
    }

    private void extractBeiderMorseFeatures() {
        String firstPersonNormalizedName = person1.normalizedName;
        String secondPersonNormalizedName = person2.normalizedName;

        try {
            Set<String> firstPersonNormalizedNameBeiderMorseEncodingsSet = new HashSet<>(Arrays.asList(
                    beiderMorseEncoder.encode(firstPersonNormalizedName).split("|")
            ));
            Set<String> secondPersonNormalizedNameBeiderMorseEncodingsSet = new HashSet<>(Arrays.asList(
                    beiderMorseEncoder.encode(secondPersonNormalizedName).split("|")
            ));
            firstPersonNormalizedNameBeiderMorseEncodingsSet.retainAll(secondPersonNormalizedNameBeiderMorseEncodingsSet);

            sparseVector.add(xA.lookupObject("bm"), (firstPersonNormalizedNameBeiderMorseEncodingsSet.size() == 0) ? 0.0d : 1.0d);
        } catch (EncoderException e) {
            logger.warn(String.format(
                    "Beider Morse encoder fail %s %s", firstPersonNormalizedName, secondPersonNormalizedName), e);
        }
    }

    private void extractDaitchMokotoffFeatures() {
        Set<String> firstPersonNormalizedNameDaitchMokotoffEncodingsSet = new HashSet<>(Arrays.asList(
                daitchMokotoffSoundex.soundex(person1.normalizedName).split("|")
        ));
        Set<String> secondPersonNormalizedNameDaitchMokotoffEncodingsSet = new HashSet<>(Arrays.asList(
                daitchMokotoffSoundex.soundex(person2.normalizedName).split("|")
        ));
        firstPersonNormalizedNameDaitchMokotoffEncodingsSet.retainAll(secondPersonNormalizedNameDaitchMokotoffEncodingsSet);

        sparseVector.add(xA.lookupObject("dms"), (firstPersonNormalizedNameDaitchMokotoffEncodingsSet.size() == 0) ? 0.0d : 1.0d);
    }

    private void extractMotherNameFeatures() {
        sparseVector.add(xA.lookupObject("jwnm"), jaroWinkler.compare(
                person1.nameMotherFirstName + " " + person1.nameMotherLastName,
                person1.nameMotherFirstName + " " + person1.nameMotherLastName
        ));
    }

    private void extractPlaceBirthFeatures() {
        sparseVector.add(xA.lookupObject("lpb"), Levenshtein.similarity(person1.placeBirth, person2.placeBirth));
    }

    private void extractDateBirthFeatures() {
        sparseVector.add(xA.lookupObject("db"), USHMMDate.similarity(person1.dateBirth, person2.dateBirth));
        sparseVector.add(xA.lookupObject("ldb"), Levenshtein.similarity(person1.dateBirth, person2.dateBirth));
    }

    private void extractGenderFeature() {
        Set<String> gendersSetFirstPerson = getPersonGenderSet(person1);
        Set<String> gendersSetSecondPerson = getPersonGenderSet(person2);
        gendersSetFirstPerson.retainAll(gendersSetSecondPerson);
        sparseVector.add(xA.lookupObject("g"), (gendersSetFirstPerson.size() == 0) ? 0.0d : 1.0d);
    }

    private Set<String> getPersonGenderSet(USHMMPerson person) {
        Set<String> gendersSetPerson = new HashSet<>();
        addGenderIfGenderIsNotEmptyString(gendersSetPerson, person.gender);
        addGenderIfGenderIsNotEmptyString(gendersSetPerson, person.genderLinearClass);
        addGenderIfGenderIsNotEmptyString(gendersSetPerson, person.genderRuleBased);
        return gendersSetPerson;
    }

    private void addGenderIfGenderIsNotEmptyString(Set<String> gendersSetPerson, String gender) {
        if (!gender.isEmpty())
            gendersSetPerson.add(gender);
    }

    private void extractSourceFeature() {
        String source1 = person1.sourceId, source2 = person2.sourceId;
        String first = source2, second = source1;
        if (source1.compareTo(source2) <= 0) {
            first = source1;
            second = source2;
        }
        sparseVector.add(xA.lookupObject("src1_" + first), 1.0d);
        sparseVector.add(xA.lookupObject("src2_" + second), 1.0d);
    }

    private void extractPersonTypeFeature() {
        sparseVector.add(xA.lookupObject("pt"), (person1.personType.equals(person2.personType)) ? 0.0d : 1.0d);
    }

    private void extractOccupationFeature() {
        sparseVector.add(xA.lookupObject("occ"), (person1.occupation.equals(person2.occupation)) ? 0.0d : 1.0d);
    }

}
