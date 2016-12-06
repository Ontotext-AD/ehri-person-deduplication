package com.ontotext.ehri.deduplication.classifier.model;

import com.ontotext.ehri.deduplication.clustering.indices.USHMMPersonIndex;
import com.ontotext.ehri.deduplication.measures.Levenshtein;
import com.ontotext.ehri.deduplication.measures.USHMMDate;
import com.ontotext.ehri.normalization.USHMMNationalityNormalization;
import org.simmetrics.metrics.JaroWinkler;
import types.Alphabet;
import types.SparseVector;

import java.util.ArrayList;
import java.util.List;

public class USHMMClassificationInstance {

    private Alphabet xA;
    private SparseVector sparseVector;

    private String personId1;
    private String personId2;

    private USHMMPersonIndex index;

    private static final JaroWinkler jaroWinkler = new JaroWinkler();

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
        extractDaitchMokotoffFeatures();
    }

    private void extractJaroWinklerNamesFeatures() {
        String firstNamePerson1 = index.getValueLowerCase(personId1, "firstName");
        String lastNamePerson1 = index.getValueLowerCase(personId1, "lastName");
        String namePerson1 = firstNamePerson1 + " " + lastNamePerson1;
        String firstNamePerson2 = index.getValueLowerCase(personId2, "firstName");
        String lastNamePerson2 = index.getValueLowerCase(personId2, "lastName");
        String namePerson2 = firstNamePerson2 + " " + lastNamePerson2;

        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("jwf", firstNamePerson1, firstNamePerson2);
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("jwl", lastNamePerson1, lastNamePerson2);
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank("jw", namePerson1, namePerson2);
    }

    private void extractJaroWinklerNormalizedNamesFeatures() {
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jwnf",
                index.getValueLowerCase(personId1, "normalizedFirstName"),
                index.getValueLowerCase(personId2, "normalizedFirstName")
        );
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jwnl",
                index.getValueLowerCase(personId1, "normalizedLastName"),
                index.getValueLowerCase(personId2, "normalizedLastName")
        );
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jwn",
                index.getValueLowerCase(personId1, "normalizedName"),
                index.getValueLowerCase(personId2, "normalizedName")
        );
    }

    private void extractDoubleMetaphoneFeatures() {
        addFeatureTwoNotBlankStringsMatch(
                "dmfn",
                index.getValueLowerCase(personId1, "firstNameDM"),
                index.getValueLowerCase(personId2, "firstNameDM")
        );
        addFeatureTwoNotBlankStringsMatch(
                "dmln",
                index.getValueLowerCase(personId1, "lastNameDM"),
                index.getValueLowerCase(personId2, "lastNameDM")
        );
        addFeatureTwoNotBlankStringsMatch(
                "dmn",
                index.getValueLowerCase(personId1, "nameDM"),
                index.getValueLowerCase(personId2, "nameDM")
        );
    }

    private void extractDaitchMokotoffFeatures() {
        String[] firstPersonNormalizedNameDaitchMokotoffEncodings = index.getValueLowerCase(personId1, "daitchMokotoffEncoding").split("|");
        String[] secondPersonNormalizedNameDaitchMokotoffEncodings = index.getValueLowerCase(personId2, "daitchMokotoffEncoding").split("|");
        for (String s1 : firstPersonNormalizedNameDaitchMokotoffEncodings)
            for (String s2 : secondPersonNormalizedNameDaitchMokotoffEncodings)
                if (s1.equals(s2)) {
                    sparseVector.add(xA.lookupObject("bm"), 1.0d);
                    break;
                }
    }

    private void extractMotherNameFeatures() {
        addJaroWinklerSimilarityFeatureIfOneOfTheTwoStringsIsNotBlank(
                "jwnm",
                index.getValueLowerCase(personId1, "nameMotherFirstName") + " " + index.getValueLowerCase(personId1, "nameMotherLastName"),
                index.getValueLowerCase(personId2, "nameMotherFirstName") + " " + index.getValueLowerCase(personId2, "nameMotherLastName")
        );
    }

    private void extractPlaceBirthFeatures() {
        addLevenshteinSimilarityFeatureBetweenTwoSets(
                "lpb",
                index.getValuesLowerCase(personId1, "placeBirth"),
                index.getValuesLowerCase(personId2, "placeBirth")
        );
    }

    private void extractDateBirthFeatures() {
        addUSHMMDateSimilarityFeatureBetweenTwoSets("db", index.getValuesLowerCase(personId1, "dateBirth"), index.getValuesLowerCase(personId2, "dateBirth"));
        addLevenshteinSimilarityFeatureBetweenTwoSets("ldb", index.getValuesLowerCase(personId1, "dateBirth"), index.getValuesLowerCase(personId2, "dateBirth"));
    }

    private void extractGenderFeature() {
        List<String> gendersFirstPerson = getPersonGenderSet(personId1);
        List<String> gendersSetSecondPerson = getPersonGenderSet(personId2);
        for (String s1 : gendersFirstPerson)
            for (String s2 : gendersSetSecondPerson)
                if (s1.equals(s2)) {
                    sparseVector.add(xA.lookupObject("g"), 1.0d);
                    break;
                }
    }

    private List<String> getPersonGenderSet(String personId) {
        List<String> gendersSetPerson = new ArrayList<>(2);
        gendersSetPerson.add(index.getValueLowerCase(personId, "gender"));
        gendersSetPerson.add(index.getValueLowerCase(personId, "genderLinearClass"));
        gendersSetPerson.add(index.getValueLowerCase(personId, "genderRuleBased"));
        return gendersSetPerson;
    }

    private void extractSourceFeature() {
        String source1 = index.getValueLowerCase(personId1, "sourceId"), source2 = index.getValueLowerCase(personId2, "sourceId");
        String first = source2, second = source1;
        if (source1.compareTo(source2) <= 0) {
            first = source1;
            second = source2;
        }
        addStringFeature("src1_" + first);
        addStringFeature("src2_" + second);
    }

    private void extractPersonTypeFeature() {
        addFeatureTwoNotBlankStringsMatch("pt", index.getValueLowerCase(personId1, "personType"), index.getValueLowerCase(personId2, "personType"));
    }

    private void extractOccupationFeature() {
        List<String> occupationsFirstPerson = index.getValuesLowerCase(personId1, "occupation");
        List<String> occupationsSecondPerson = index.getValuesLowerCase(personId2, "occupation");
        for (String s1 : occupationsFirstPerson)
            for (String s2 : occupationsSecondPerson)
                if (s1.equals(s2)) {
                    sparseVector.add(xA.lookupObject("o"), 1.0d);
                    break;
                }
    }

    private void extractNationalityFeature() {
        List<String> nationalitiesFirstPerson = index.getValuesLowerCase(personId1, "nationality");
        List<String> nationalitiesSecondPerson = index.getValuesLowerCase(personId2, "nationality");
        for (String s1 : nationalitiesFirstPerson)
            for (String s2 : nationalitiesSecondPerson)
                if (nationalitiesMatch(s1, s2)) {
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
