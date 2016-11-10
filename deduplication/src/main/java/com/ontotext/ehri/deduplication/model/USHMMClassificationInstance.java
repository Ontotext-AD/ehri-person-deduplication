package com.ontotext.ehri.deduplication.model;

import com.ontotext.ehri.deduplication.measures.Levenshtein;
import com.ontotext.ehri.deduplication.measures.USHMMDate;
import com.ontotext.ehri.normalization.USHMMNationalityNormalization;
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
        extractNationalityFeature();
        return sparseVector;
    }

    private void addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(final String featurePrefix, String s1, String s2) {
        String string1 = s1.trim(), string2 = s2.trim();
        if (!string1.isEmpty() || !string2.isEmpty())
            sparseVector.add(xA.lookupObject(featurePrefix), jaroWinkler.compare(string1, string2));
    }

    private void addLevenshteinSimilarityFeatureBetweenTwoSets(final String featurePrefix, Set<String> set1, Set<String> set2) {
        double minimum = Double.MAX_VALUE;

        for (String s1 : set1)
            for (String s2 : set2)
                minimum = getLevenshteinSimilarityMinimum(minimum, s1, s2);

        if (minimum != Double.MAX_VALUE)
            sparseVector.add(xA.lookupObject(featurePrefix), minimum);
    }

    private double getLevenshteinSimilarityMinimum(double minimum, String s1, String s2) {
        String string1 = s1.trim(), string2 = s2.trim();
        if (!string1.isEmpty() || !string2.isEmpty()) {
            double similarity = Levenshtein.similarity(string1, string2);
            if (similarity < minimum)
                minimum = similarity;
        }
        return minimum;
    }

    private void addUSHMMDateSimilarityFeatureBetweenTwoSets(final String featurePrefix, Set<String> set1, Set<String> set2) {
        double minimum = Double.MAX_VALUE;

        for (String s1 : set1)
            for (String s2 : set2)
                minimum = getUSHMMDateSimilarityMinimum(minimum, s1, s2);

        if (minimum != Double.MAX_VALUE)
            sparseVector.add(xA.lookupObject(featurePrefix), minimum);
    }

    private double getUSHMMDateSimilarityMinimum(double minimum, String s1, String s2) {
        String string1 = s1.trim(), string2 = s2.trim();
        if (!string1.isEmpty() && !string2.isEmpty()) {
            double similarity = USHMMDate.similarity(string1, string2);
            if (similarity < minimum)
                minimum = similarity;
        }
        return minimum;
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
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jwf",
                person1.getStringValue("firstName"),
                person2.getStringValue("firstName")
        );
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jwl",
                person1.getStringValue("lastName"),
                person2.getStringValue("lastName")
        );
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jw",
                person1.getStringValue("firstName") + " " + person1.getStringValue("lastName"),
                person2.getStringValue("firstName") + " " + person2.getStringValue("lastName")
        );
    }

    private void extractJaroWinklerNormalizedNamesFeatures() {
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jwnf",
                person1.getStringValue("normalizedFirstName"),
                person2.getStringValue("normalizedFirstName")
        );
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jwnl",
                person1.getStringValue("normalizedLastName"),
                person2.getStringValue("normalizedLastName")
        );
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jwn",
                person1.getStringValue("normalizedName"),
                person2.getStringValue("normalizedName")
        );
    }

    private void extractDoubleMetaphoneFeatures() {
        addFeatureTwoNotBlankStringsMatch(
                "dmfn",
                person1.getStringValue("firstNameDM"),
                person2.getStringValue("firstNameDM")
        );
        addFeatureTwoNotBlankStringsMatch(
                "dmln",
                person1.getStringValue("lastNameDM"),
                person2.getStringValue("lastNameDM")
        );
        addFeatureTwoNotBlankStringsMatch(
                "dmn",
                person1.getStringValue("nameDM"),
                person2.getStringValue("nameDM")
        );
    }

    private void extractBeiderMorseFeatures() {
        String firstPersonNormalizedName = person1.getStringValue("normalizedName");
        String secondPersonNormalizedName = person2.getStringValue("normalizedName");

        try {
            Set<String> firstPersonNormalizedNameBeiderMorseEncodingsSet = new HashSet<>(Arrays.asList(
                    beiderMorseEncoder.encode(firstPersonNormalizedName).split("|")
            ));
            Set<String> secondPersonNormalizedNameBeiderMorseEncodingsSet = new HashSet<>(Arrays.asList(
                    beiderMorseEncoder.encode(secondPersonNormalizedName).split("|")
            ));
            Set<String> commonBeiderMorseEncodings = new HashSet<>(firstPersonNormalizedNameBeiderMorseEncodingsSet);
            commonBeiderMorseEncodings.retainAll(secondPersonNormalizedNameBeiderMorseEncodingsSet);
            addFeatureSetIsNotEmpty("bm", commonBeiderMorseEncodings);
        } catch (EncoderException e) {
            logger.warn(String.format(
                    "Beider Morse encoder fail %s %s", firstPersonNormalizedName, secondPersonNormalizedName), e);
        }
    }

    private void extractDaitchMokotoffFeatures() {
        Set<String> firstPersonNormalizedNameDaitchMokotoffEncodingsSet = new HashSet<>(Arrays.asList(
                daitchMokotoffSoundex.soundex(person1.getStringValue("normalizedName")).split("|")
        ));
        Set<String> secondPersonNormalizedNameDaitchMokotoffEncodingsSet = new HashSet<>(Arrays.asList(
                daitchMokotoffSoundex.soundex(person2.getStringValue("normalizedName")).split("|")
        ));
        Set<String> commonDaitchMokotoffEncodings = new HashSet<>(firstPersonNormalizedNameDaitchMokotoffEncodingsSet);
        commonDaitchMokotoffEncodings.retainAll(secondPersonNormalizedNameDaitchMokotoffEncodingsSet);
        addFeatureSetIsNotEmpty("dms", commonDaitchMokotoffEncodings);
    }

    private void extractMotherNameFeatures() {
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jwnm",
                person1.getStringValue("nameMotherFirstName") + " " + person1.getStringValue("nameMotherLastName"),
                person2.getStringValue("nameMotherFirstName") + " " + person2.getStringValue("nameMotherLastName")
        );
    }

    private void extractPlaceBirthFeatures() {
        addLevenshteinSimilarityFeatureBetweenTwoSets(
                "lpb",
                person1.getSetOfStringsValues("placeBirth"),
                person2.getSetOfStringsValues("placeBirth")
        );
    }

    private void extractDateBirthFeatures() {
        addUSHMMDateSimilarityFeatureBetweenTwoSets("db", person1.getSetOfStringsValues("dateBirth"), person2.getSetOfStringsValues("dateBirth"));
        addLevenshteinSimilarityFeatureBetweenTwoSets("ldb", person1.getSetOfStringsValues("dateBirth"), person2.getSetOfStringsValues("dateBirth"));
    }

    private void extractGenderFeature() {
        Set<String> gendersSetFirstPerson = getPersonGenderSet(person1);
        Set<String> gendersSetSecondPerson = getPersonGenderSet(person2);
        Set<String> commonGenders = new HashSet<>(gendersSetFirstPerson);
        commonGenders.retainAll(gendersSetSecondPerson);
        addFeatureSetIsNotEmpty("g", commonGenders);
    }

    private Set<String> getPersonGenderSet(USHMMPerson person) {
        Set<String> gendersSetPerson = new HashSet<>();
        addStringToSetIfStringIsNotBlank(gendersSetPerson, person.getStringValue("gender"));
        addStringToSetIfStringIsNotBlank(gendersSetPerson, person.getStringValue("genderLinearClass"));
        addStringToSetIfStringIsNotBlank(gendersSetPerson, person.getStringValue("genderRuleBased"));
        return gendersSetPerson;
    }

    private void extractSourceFeature() {
        String source1 = person1.getStringValue("sourceId"), source2 = person2.getStringValue("sourceId");
        String first = source2, second = source1;
        if (source1.compareTo(source2) <= 0) {
            first = source1;
            second = source2;
        }
        addStringFeature("src1_" + first);
        addStringFeature("src2_" + second);
    }

    private void extractPersonTypeFeature() {
        addFeatureTwoNotBlankStringsMatch("pt", person1.getStringValue("personType"), person2.getStringValue("personType"));
    }

    private void extractOccupationFeature() {
        Set<String> occupationsFirstPerson = person1.getSetOfStringsValues("occupation");
        Set<String> occupationsSecondPerson = person2.getSetOfStringsValues("occupation");
        Set<String> commonOccupations = new HashSet<>(occupationsFirstPerson);
        commonOccupations.retainAll(occupationsSecondPerson);
        addFeatureSetIsNotEmpty("occ", commonOccupations);
    }

    private void extractNationalityFeature() {
        Set<String> nationalitiesFirstPerson = person1.getSetOfStringsValues("nationality");
        Set<String> nationalitiesSecondPerson = person1.getSetOfStringsValues("nationality");
        Set<String> commonNationalities = getCommonNationalities(nationalitiesFirstPerson, nationalitiesSecondPerson);
        addFeatureSetIsNotEmpty("nat", commonNationalities);
    }

    private Set<String> getCommonNationalities(Set<String> nationalitiesFirstPerson, Set<String> nationalitiesSecondPerson) {
        Set<String> commonNationalities = new HashSet<>();
        for (String nationality1 : nationalitiesFirstPerson)
            for (String nationality2 : nationalitiesSecondPerson)
                if (nationalitiesMatch(nationality1, nationality2))
                    commonNationalities.add(nationality1);
        return commonNationalities;
    }

    private boolean nationalitiesMatch(String nationality1, String nationality2) {
        String normalized1 = USHMMNationalityNormalization.normalize(nationality1), normalized2 = USHMMNationalityNormalization.normalize(nationality2);
        String shorter = normalized2, longer = normalized1;
        if (normalized1.length() < normalized2.length()) {
            shorter = normalized1;
            longer = normalized2;
        }
        return (shorter.equals(longer) || (shorter.equals("Czech") && longer.equals("Czechoslovakian")) ||
                (shorter.equals("Slovak") && longer.equals("Czechoslovakian")));
    }

}
