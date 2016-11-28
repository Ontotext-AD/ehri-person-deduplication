package com.ontotext.ehri.deduplication.classifier.model;

import com.ontotext.ehri.deduplication.index.USHMMPersonIndex;
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

import java.util.*;

public class USHMMClassificationInstance {

    private Alphabet xA;
    private SparseVector sparseVector;

    private String personId1;
    private String personId2;

    private USHMMPersonIndex index;

    private static final JaroWinkler jaroWinkler = new JaroWinkler();
    private static final DaitchMokotoffSoundex daitchMokotoffSoundex = new DaitchMokotoffSoundex();
    private static final BeiderMorseEncoder beiderMorseEncoder = new BeiderMorseEncoder();

    private static final Logger logger = LoggerFactory.getLogger(USHMMPersonsFeatureExtractor.class);

    public USHMMClassificationInstance(Alphabet xA, String personId1, String personId2, USHMMPersonIndex index) {
        this.xA = xA;
        this.sparseVector = new SparseVector();

        this.personId1 = personId1;
        this.personId2 = personId2;

        this.index = index;
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
        extractNationalityFeature();
        return sparseVector;
    }

    private void addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(final String featurePrefix, String s1, String s2) {
        String string1 = s1.trim(), string2 = s2.trim();
        if (!string1.isEmpty() || !string2.isEmpty())
            sparseVector.add(xA.lookupObject(featurePrefix), jaroWinkler.compare(string1, string2));
    }

    private void addLevenshteinSimilarityFeatureBetweenTwoSets(final String featurePrefix, List<String> list1, List<String> list2) {
        double minimum = Double.MAX_VALUE;

        for (String s1 : list1)
            for (String s2 : list2)
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

    private void addUSHMMDateSimilarityFeatureBetweenTwoSets(final String featurePrefix, List<String> list1, List<String> list2) {
        double minimum = Double.MAX_VALUE;

        for (String s1 : list1)
            for (String s2 : list2)
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

    private void addStringFeature(final String featurePrefix) {
        sparseVector.add(xA.lookupObject(featurePrefix), 1.0d);
    }

    private void extractNamesFeatures() {
        extractJaroWinklerNamesFeatures();
        extractJaroWinklerNormalizedNamesFeatures();
        extractDoubleMetaphoneFeatures();
        extractBeiderMorseFeatures();
        extractDaitchMokotoffFeatures();
    }

    private void extractJaroWinklerNamesFeatures() {
        String firstNamePerson1 = index.getValue(personId1, "firstName");
        String lastNamePerson1 = index.getValue(personId1, "lastName");
        String namePerson1 = firstNamePerson1 + " " + lastNamePerson1;
        String firstNamePerson2 = index.getValue(personId2, "firstName");
        String lastNamePerson2 = index.getValue(personId2, "lastName");
        String namePerson2 = firstNamePerson2 + " " + lastNamePerson2;

        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("jwf", firstNamePerson1, firstNamePerson2);
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("jwl", lastNamePerson1, lastNamePerson2);
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("jw", namePerson1, namePerson2);
    }

    private void extractJaroWinklerNormalizedNamesFeatures() {
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jwnf",
                index.getValue(personId1, "normalizedFirstName"),
                index.getValue(personId2, "normalizedFirstName")
        );
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jwnl",
                index.getValue(personId1, "normalizedLastName"),
                index.getValue(personId2, "normalizedLastName")
        );
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jwn",
                index.getValue(personId1, "normalizedName"),
                index.getValue(personId2, "normalizedName")
        );
    }

    private void extractDoubleMetaphoneFeatures() {
        addFeatureTwoNotBlankStringsMatch(
                "dmfn",
                index.getValue(personId1, "firstNameDM"),
                index.getValue(personId2, "firstNameDM")
        );
        addFeatureTwoNotBlankStringsMatch(
                "dmln",
                index.getValue(personId1, "lastNameDM"),
                index.getValue(personId2, "lastNameDM")
        );
        addFeatureTwoNotBlankStringsMatch(
                "dmn",
                index.getValue(personId1, "nameDM"),
                index.getValue(personId2, "nameDM")
        );
    }

    private void extractBeiderMorseFeatures() {
        String firstPersonNormalizedName = index.getValue(personId1, "normalizedName");
        String secondPersonNormalizedName = index.getValue(personId2, "normalizedName");

        try {
            String[] firstPersonNormalizedNameBeiderMorseEncodings = beiderMorseEncoder.encode(firstPersonNormalizedName).split("|");
            String[] secondPersonNormalizedNameBeiderMorseEncodings = beiderMorseEncoder.encode(secondPersonNormalizedName).split("|");
            for (String s1 : firstPersonNormalizedNameBeiderMorseEncodings)
                for (String s2: secondPersonNormalizedNameBeiderMorseEncodings)
                    if(s1.equals(s2))
                    {
                        sparseVector.add(xA.lookupObject("bm"), 1.0d);
                        break;
                    }
        } catch (EncoderException e) {
            logger.warn(String.format(
                    "Beider Morse encoder fail %s %s", firstPersonNormalizedName, secondPersonNormalizedName), e);
        }
    }

    private void extractDaitchMokotoffFeatures() {
        String[] firstPersonNormalizedNameDaitchMokotoffEncodings = daitchMokotoffSoundex.soundex(index.getValue(personId1, "normalizedName")).split("|");
        String[] secondPersonNormalizedNameDaitchMokotoffEncodings = daitchMokotoffSoundex.soundex(index.getValue(personId2, "normalizedName")).split("|");
        for (String s1 : firstPersonNormalizedNameDaitchMokotoffEncodings)
            for (String s2: secondPersonNormalizedNameDaitchMokotoffEncodings)
                if(s1.equals(s2))
                {
                    sparseVector.add(xA.lookupObject("bm"), 1.0d);
                    break;
                }
    }

    private void extractMotherNameFeatures() {
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jwnm",
                index.getValue(personId1, "nameMotherFirstName") + " " + index.getValue(personId1, "nameMotherLastName"),
                index.getValue(personId2, "nameMotherFirstName") + " " + index.getValue(personId2, "nameMotherLastName")
        );
    }

    private void extractPlaceBirthFeatures() {
        addLevenshteinSimilarityFeatureBetweenTwoSets(
                "lpb",
                index.getValues(personId1, "placeBirth"),
                index.getValues(personId2, "placeBirth")
        );
    }

    private void extractDateBirthFeatures() {
        addUSHMMDateSimilarityFeatureBetweenTwoSets("db", index.getValues(personId1, "dateBirth"), index.getValues(personId2, "dateBirth"));
        addLevenshteinSimilarityFeatureBetweenTwoSets("ldb", index.getValues(personId1, "dateBirth"), index.getValues(personId2, "dateBirth"));
    }

    private void extractGenderFeature() {
        List<String> gendersFirstPerson = getPersonGenderSet(personId1);
        List<String> gendersSetSecondPerson = getPersonGenderSet(personId2);
        for (String s1 : gendersFirstPerson)
            for (String s2: gendersSetSecondPerson)
                if(s1.equals(s2))
                {
                    sparseVector.add(xA.lookupObject("g"), 1.0d);
                    break;
                }
    }

    private List<String> getPersonGenderSet(String personId) {
        List<String> gendersSetPerson = new ArrayList<>(2);
        gendersSetPerson.add(index.getValue(personId, "gender"));
        gendersSetPerson.add(index.getValue(personId, "genderLinearClass"));
        gendersSetPerson.add(index.getValue(personId, "genderRuleBased"));
        return gendersSetPerson;
    }

    private void extractSourceFeature() {
        String source1 = index.getValue(personId1, "sourceId"), source2 = index.getValue(personId2, "sourceId");
        String first = source2, second = source1;
        if (source1.compareTo(source2) <= 0) {
            first = source1;
            second = source2;
        }
        addStringFeature("src1_" + first);
        addStringFeature("src2_" + second);
    }

    private void extractPersonTypeFeature() {
        addFeatureTwoNotBlankStringsMatch("pt", index.getValue(personId1, "personType"), index.getValue(personId2, "personType"));
    }

    private void extractOccupationFeature() {
        List<String> occupationsFirstPerson = index.getValues(personId1, "occupation");
        List<String> occupationsSecondPerson = index.getValues(personId2, "occupation");
        for (String s1 : occupationsFirstPerson)
            for (String s2: occupationsSecondPerson)
                if (s1.equals(s2))
                {
                    sparseVector.add(xA.lookupObject("o"), 1.0d);
                    break;
                }
    }

    private void extractNationalityFeature() {
        List<String> nationalitiesFirstPerson = index.getValues(personId1, "nationality");
        List<String> nationalitiesSecondPerson = index.getValues(personId2, "nationality");
        for (String s1 : nationalitiesFirstPerson)
            for (String s2: nationalitiesSecondPerson)
                if (nationalitiesMatch(s1, s2))
                {
                    sparseVector.add(xA.lookupObject("n"), 1.0d);
                    break;
                }
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
