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

public class USHMMClassificationInstance {

    private Alphabet xA;
    private SparseVector sparseVector;

    private USHMMPerson person1;
    private USHMMPerson person2;

    private static final JaroWinkler jaroWinkler = new JaroWinkler();
    private static final DaitchMokotoffSoundex daitchMokotoffSoundex = new DaitchMokotoffSoundex();
    private static final BeiderMorseEncoder beiderMorseEncoder = new BeiderMorseEncoder();

    private static final Logger logger = LoggerFactory.getLogger(USHMMPersonsFeatureExtractor.class);

    public USHMMClassificationInstance(Alphabet xA, USHMMPerson person1, USHMMPerson person2) {
        this.xA = xA;
        this.sparseVector = new SparseVector();

        this.person1 = person1;
        this.person2 = person2;
    }

    public SparseVector getSparseVector() {
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

    private void addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(final String featurePrefix, String s1, String s2) {
        String string1 = s1.trim(), string2 = s2.trim();
        if (!string1.isEmpty() || !string2.isEmpty())
            sparseVector.add(xA.lookupObject(featurePrefix), jaroWinkler.compare(string1, string2));
    }

    private void addLevenshteinSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(final String featurePrefix, String s1, String s2) {
        String string1 = s1.trim(), string2 = s2.trim();
        if (!string1.isEmpty() || !string2.isEmpty())
            sparseVector.add(xA.lookupObject(featurePrefix), Levenshtein.similarity(string1, string2));
    }

    private void addUSHMMDateSimilarityIfBothStringAreNotBlank(final String featurePrefix, String s1, String s2) {
        String string1 = s1.trim(), string2 = s2.trim();
        if (!string1.isEmpty() && !string2.isEmpty())
            sparseVector.add(xA.lookupObject(featurePrefix), USHMMDate.similarity(string1, string2));
    }

    private void addFeatureTwoNotBlankStringsMatch(final String featurePrefix, String s1, String s2) {
        if (!s1.trim().isEmpty())
            sparseVector.add(xA.lookupObject(featurePrefix), s1.equals(s2) ? 1.0d : 0.0d);
    }

    private void addFeatureSetIsNotEmpty(final String featurePrefix, Set set) {
        if (!set.isEmpty())
            sparseVector.add(xA.lookupObject(featurePrefix), 1.0d);
    }

    private void addStringFeature(final String featurePrefix) {
        sparseVector.add(xA.lookupObject(featurePrefix), 1.0d);
    }

    private void addStringToSetIfStringIsNotBlank(Set<String> set, String string) {
        if (!string.trim().isEmpty())
            set.add(string);
    }

    private void extractNamesFeatures() {
        extractJaroWinklerNamesFeatures();
        extractJaroWinklerNormalizedNamesFeatures();
        extractDoubleMetaphoneFeatures();
        extractBeiderMorseFeatures();
        extractDaitchMokotoffFeatures();
    }

    private void extractJaroWinklerNamesFeatures() {
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("jwf", person1.firstName, person2.firstName);
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("jwl", person1.lastName, person2.lastName);
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("jw", person1.firstName + " " + person1.lastName, person2.firstName + " " + person2.lastName);
    }

    private void extractJaroWinklerNormalizedNamesFeatures() {
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("jwnf", person1.normalizedFirstName, person2.normalizedFirstName);
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("jwnl", person1.normalizedLastName, person2.normalizedLastName);
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("jwn", person1.normalizedName, person2.normalizedName);
    }

    private void extractDoubleMetaphoneFeatures() {
        addFeatureTwoNotBlankStringsMatch("dmfn", person1.firstNameDM, person2.firstNameDM);
        addFeatureTwoNotBlankStringsMatch("dmln", person1.lastNameDM, person2.lastNameDM);
        addFeatureTwoNotBlankStringsMatch("dmn", person1.nameDM, person2.nameDM);
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

            addFeatureSetIsNotEmpty("bm", firstPersonNormalizedNameBeiderMorseEncodingsSet);
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

        addFeatureSetIsNotEmpty("dms", firstPersonNormalizedNameDaitchMokotoffEncodingsSet);
    }

    private void extractMotherNameFeatures() {
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("jwnm", person1.nameMotherFirstName + " " + person1.nameMotherLastName, person2.nameMotherFirstName + " " + person2.nameMotherLastName);
    }

    private void extractPlaceBirthFeatures() {
        addLevenshteinSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("lpb", person1.placeBirth, person2.placeBirth);
    }

    private void extractDateBirthFeatures() {
        addUSHMMDateSimilarityIfBothStringAreNotBlank("db", person1.dateBirth, person2.dateBirth);
        addLevenshteinSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("ldb", person1.dateBirth, person2.dateBirth);
    }

    private void extractGenderFeature() {
        Set<String> gendersSetFirstPerson = getPersonGenderSet(person1);
        Set<String> gendersSetSecondPerson = getPersonGenderSet(person2);
        gendersSetFirstPerson.retainAll(gendersSetSecondPerson);
        addFeatureSetIsNotEmpty("g", gendersSetFirstPerson);
    }

    private Set<String> getPersonGenderSet(USHMMPerson person) {
        Set<String> gendersSetPerson = new HashSet<>();
        addStringToSetIfStringIsNotBlank(gendersSetPerson, person.gender);
        addStringToSetIfStringIsNotBlank(gendersSetPerson, person.genderLinearClass);
        addStringToSetIfStringIsNotBlank(gendersSetPerson, person.genderRuleBased);
        return gendersSetPerson;
    }

    private void extractSourceFeature() {
        String source1 = person1.sourceId, source2 = person2.sourceId;
        String first = source2, second = source1;
        if (source1.compareTo(source2) <= 0) {
            first = source1;
            second = source2;
        }
        addStringFeature("src1_" + first);
        addStringFeature("src2_" + second);
    }

    private void extractPersonTypeFeature() {
        addFeatureTwoNotBlankStringsMatch("pt", person1.personType, person2.personType);
    }

    private void extractOccupationFeature() {
        addFeatureTwoNotBlankStringsMatch("occ", person1.occupation, person2.occupation);
    }

}
