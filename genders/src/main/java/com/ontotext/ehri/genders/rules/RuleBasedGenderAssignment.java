package com.ontotext.ehri.genders.rules;

import com.ontotext.ehri.normalization.USHMMPersonNameNormalization;
import javafx.util.Pair;
import org.apache.commons.lang.WordUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


class RuleBasedGenderAssignment {

    private static final String SPACE = " ";

    private static final double GENERATED_GENDERS_PROBABILITY = 0.9d;

    private static Map<String, HashSet<String>> firstNameGenderMap;
    private static Map<String, HashSet<String>> nameGenderMap;

    static Map<String, Pair<String, Double>> assignGendersByRule(Map<String, List<String>> allData) {
        getKnownNameGenderMap(allData);
        Map<String, Pair<String, Double>> labeledData = new HashMap<>();
        extractGenderByRule(allData, labeledData);
        return labeledData;
    }

    private static void getKnownNameGenderMap(Map<String, List<String>> parsedData) {
        nameGenderMap = new HashMap<>();
        firstNameGenderMap = new HashMap<>();
        for (String personId : parsedData.keySet())
            addPersonToMap(parsedData, personId);
    }

    private static void addPersonToMap(Map<String, List<String>> parsedData, String personId) {
        List<String> personDataList = parsedData.get(personId);
        String gender = personDataList.get(0);
        if (!gender.isEmpty())
            addKnownNameGenderToMap(personDataList);
    }

    private static void addKnownNameGenderToMap(List<String> personDataList) {
        String gender = personDataList.get(0);
        String firstName = personDataList.get(1), lastName = personDataList.get(2);
        String firstNameTransliterated = personDataList.get(3), lastNameTransliterated = personDataList.get(4);

        if (!firstNameTransliterated.isEmpty())
            firstName = firstNameTransliterated;
        if (!lastNameTransliterated.isEmpty())
            lastName = lastNameTransliterated;

        putNameGenderToMap(firstName, gender, firstNameGenderMap);
        putNameGenderToMap(firstName + SPACE + lastName, gender, nameGenderMap);
    }


    private static void putNameGenderToMap(String name, String gender, Map<String, HashSet<String>> map) {

        String normalizedName = WordUtils.capitalize(USHMMPersonNameNormalization.normalize(name));
        String[] names = normalizedName.split(SPACE);
        String nameAsKey = getNameAsKey(normalizedName);

        if ((names.length == 1 && nameAsKey.length() > 1) || (names.length > 1 && nameAsKey.length() > 5)) {
            HashSet<String> genderSet = map.get(nameAsKey);
            if (genderSet == null)
                genderSet = new HashSet<>();
            genderSet.add(gender);
            map.put(nameAsKey, genderSet);
        }

    }

    private static String getNameAsKey(String name) {
        String[] names = name.split(SPACE);
        String nameAsKey = "";
        for (String n : names)
            if (n.length() > 1)
                nameAsKey += n + SPACE;
        return nameAsKey.trim();
    }

    private static void extractGenderByRule(Map<String, List<String>> parsedData, Map<String, Pair<String, Double>> labeledData) {
        for (String personId : parsedData.keySet()) {
            List<String> personDataList = parsedData.get(personId);
            String gender = personDataList.get(0);
            if (gender.isEmpty())
                tryToExtractPersonGender(labeledData, personId, personDataList);
        }
    }

    private static void tryToExtractPersonGender(Map<String, Pair<String, Double>> labeledData, String personId, List<String> personDataList) {
        String firstName = personDataList.get(1), lastName = personDataList.get(2);
        String firstNameTransliterated = personDataList.get(3), lastNameTransliterated = personDataList.get(4);

        if (!firstNameTransliterated.isEmpty())
            firstName = firstNameTransliterated;
        if (!lastNameTransliterated.isEmpty())
            lastName = lastNameTransliterated;
        String name = firstName + " " + lastName;

        if (nameGenderMap.containsKey(name))
            extractPersonGenderByName(labeledData, nameGenderMap, personId, name);
        else if (firstNameGenderMap.containsKey(firstName))
            extractPersonGenderByName(labeledData, firstNameGenderMap, personId, firstName);
    }

    private static void extractPersonGenderByName(Map<String, Pair<String, Double>> labeledData, Map<String, HashSet<String>> nameMap, String personId, String name) {
        HashSet<String> genderSet = nameMap.get(name);
        if (genderSet.size() == 1)
            labeledData.put(personId, new Pair<>(genderSet.iterator().next(), GENERATED_GENDERS_PROBABILITY));
    }

}
