package com.ontotext.ehri.deduplication.model;

import com.ontotext.ehri.deduplication.measures.JaroWinkler;
import com.ontotext.ehri.deduplication.measures.Levenshtein;
import com.ontotext.ehri.deduplication.measures.USHMMDate;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.DaitchMokotoffSoundex;
import org.apache.commons.codec.language.bm.BeiderMorseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.Alphabet;
import types.ClassificationInstance;
import types.SparseVector;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates list of classification instances from the parsed data.
 */

class USHMMPersonsFeatureExtractor {

    private static final Logger logger = LoggerFactory.getLogger(USHMMPersonsFeatureExtractor.class);

    private static final String NEGATIVE_CLASS = "NO";
    private static final String POSITIVE_CLASS = "YES";
    private static final String NEUTRAL_CLASS = "UNCERTAIN";

    private USHMMPersonStatementsMapHash statementsMap;

    private static final DaitchMokotoffSoundex daitchMokotoffSoundex = new DaitchMokotoffSoundex();
    private static final BeiderMorseEncoder beiderMorseEncoder = new BeiderMorseEncoder();

    List<ClassificationInstance> getClassificationInstances(List<USHMMGoldStandardEntry> data, String personStatementsMapCache) {

        Alphabet xA = new Alphabet();
        Alphabet yA = new Alphabet();
        statementsMap = new USHMMPersonStatementsMapHash(data, personStatementsMapCache);
        List<ClassificationInstance> classificationInstanceList = new ArrayList<>();

        classificationInstanceList.addAll(data.stream().map(dataEntry -> getInstance(
                dataEntry.personId1, dataEntry.personId2, dataEntry.label, xA, yA
        )).collect(Collectors.toList()));

        return classificationInstanceList;
    }

    private ClassificationInstance getInstance(String personId1, String personId2, String match, Alphabet xA, Alphabet yA) {
        String label = getLabel(match);
        SparseVector sparseVector = getSparseVector(xA, personId1, personId2);
        return new ClassificationInstance(xA, yA, sparseVector, yA.lookupObject(label));
    }

    private String getLabel(String match) {
        if ("-2".equals(match))
            return NEGATIVE_CLASS;
        else if ("2".equals(match))
            return POSITIVE_CLASS;
        else
            return NEUTRAL_CLASS;
    }

    private SparseVector getSparseVector(Alphabet xA, String personId1, String personId2) {
        SparseVector sparseVector = new SparseVector();
        addNamesFeatures(xA, personId1, personId2, sparseVector);
        addPlaceBirthFeatures(xA, personId1, personId2, sparseVector);
        addDateBirthFeatures(xA, personId1, personId2, sparseVector);
        addGenderFeature(xA, personId1, personId2, sparseVector);
        return sparseVector;

    }

    private void addNamesFeatures(Alphabet xA, String personId1, String personId2, SparseVector sparseVector) {
        addJaroWinklerNamesFeatures(xA, personId1, personId2, sparseVector);
        addJaroWinklerNormalizedNamesFeatures(xA, personId1, personId2, sparseVector);
        addDoubleMetaphoneFeatures(xA, personId1, personId2, sparseVector);
        addBeiderMorseFeatures(xA, personId1, personId2, sparseVector);
        addDaitchMokotoffFeatures(xA, personId1, personId2, sparseVector);
    }

    private void addJaroWinklerNamesFeatures(Alphabet xA, String personId1, String personId2, SparseVector sparseVector) {
        String firstPersonFirstName = statementsMap.get(personId1, "firstName");
        String firstPersonLastName = statementsMap.get(personId1, "lastName");
        String secondPersonFirstName = statementsMap.get(personId2, "firstName");
        String secondPersonLastName = statementsMap.get(personId2, "lastName");

        sparseVector.add(xA.lookupObject("jwf"), JaroWinkler.distance(firstPersonFirstName, secondPersonFirstName));
        sparseVector.add(xA.lookupObject("jwl"), JaroWinkler.distance(firstPersonLastName, secondPersonLastName));

        sparseVector.add(xA.lookupObject("jw"), JaroWinkler.distance(firstPersonFirstName + " " + firstPersonLastName,
                secondPersonFirstName + " " + secondPersonLastName));
    }

    private void addJaroWinklerNormalizedNamesFeatures(Alphabet xA, String personId1, String personId2, SparseVector sparseVector) {
        sparseVector.add(xA.lookupObject("jwnf"), JaroWinkler.distance(
                statementsMap.get(personId1, "normalizedFirstName"),
                statementsMap.get(personId2, "normalizedFirstName")));

        sparseVector.add(xA.lookupObject("jwnl"), JaroWinkler.distance(
                statementsMap.get(personId1, "normalizedLastName"),
                statementsMap.get(personId2, "normalizedLastName")));

        sparseVector.add(xA.lookupObject("jwn"), JaroWinkler.distance(
                statementsMap.get(personId1, "normalizedName"),
                statementsMap.get(personId2, "normalizedName")));
    }

    private void addDoubleMetaphoneFeatures(Alphabet xA, String personId1, String personId2, SparseVector sparseVector) {
        sparseVector.add(xA.lookupObject("dmn"), (statementsMap.get(personId1, "nameDM").equals(
                statementsMap.get(personId2, "nameDM"))) ? 1.0d : 0.0d);

        sparseVector.add(xA.lookupObject("dmfn"), (statementsMap.get(personId1, "firstNameDM").equals(
                statementsMap.get(personId2, "firstNameDM"))) ? 1.0d : 0.0d);

        sparseVector.add(xA.lookupObject("dmln"), (statementsMap.get(personId1, "lastNameDM").equals(
                statementsMap.get(personId2, "lastNameDM"))) ? 1.0d : 0.0d);
    }

    private void addBeiderMorseFeatures(Alphabet xA, String personId1, String personId2, SparseVector sparseVector) {
        String firstPersonNormalizedName = statementsMap.get(personId1, "normalizedName");
        String secondPersonNormalizedName = statementsMap.get(personId2, "normalizedName");

        try {
            Set<String> firstPersonNormalizedNameBeiderMorseEncodingsSet = new HashSet<>(Arrays.asList(
                    beiderMorseEncoder.encode(firstPersonNormalizedName).split("|")
            ));
            Set<String> secondPersonNormalizedNameBeiderMorseEncodingsSet = new HashSet<>(Arrays.asList(
                    beiderMorseEncoder.encode(secondPersonNormalizedName).split("|")
            ));
            firstPersonNormalizedNameBeiderMorseEncodingsSet.retainAll(secondPersonNormalizedNameBeiderMorseEncodingsSet);

            sparseVector.add(xA.lookupObject("bm"),
                    (firstPersonNormalizedNameBeiderMorseEncodingsSet.size() == 0) ? 0.0d : 1.0d);
        } catch (EncoderException e) {
            logger.warn(String.format(
                    "Beider Morse encoder fail %s %s", firstPersonNormalizedName, secondPersonNormalizedName), e);
        }
    }

    private void addDaitchMokotoffFeatures(Alphabet xA, String personId1, String personId2, SparseVector sparseVector) {
        Set<String> firstPersonNormalizedNameDaitchMokotoffEncodingsSet = new HashSet<>(Arrays.asList(
                daitchMokotoffSoundex.soundex(statementsMap.get(personId1, "normalizedName")).split("|")
        ));
        Set<String> secondPersonNormalizedNameDaitchMokotoffEncodingsSet = new HashSet<>(Arrays.asList(
                daitchMokotoffSoundex.soundex(statementsMap.get(personId2, "normalizedName")).split("|")
        ));
        firstPersonNormalizedNameDaitchMokotoffEncodingsSet.retainAll(secondPersonNormalizedNameDaitchMokotoffEncodingsSet);

        sparseVector.add(xA.lookupObject("dms"),
                (firstPersonNormalizedNameDaitchMokotoffEncodingsSet.size() == 0) ? 0.0d : 1.0d);
    }

    private void addPlaceBirthFeatures(Alphabet xA, String personId1, String personId2, SparseVector sparseVector) {
        sparseVector.add(xA.lookupObject("lpb"), Levenshtein.similarity(
                statementsMap.get(personId1, "placeBirth"),
                statementsMap.get(personId2, "placeBirth"))
        );
    }

    private void addDateBirthFeatures(Alphabet xA, String personId1, String personId2, SparseVector sparseVector) {
        String dateBirthFirstPerson = statementsMap.get(personId1, "dateBirth");
        String dateBirthSecondPerson = statementsMap.get(personId2, "dateBirth");

        sparseVector.add(xA.lookupObject("db"), USHMMDate.similarity(dateBirthFirstPerson, dateBirthSecondPerson));
        sparseVector.add(xA.lookupObject("ldb"), Levenshtein.similarity(dateBirthFirstPerson, dateBirthSecondPerson));
    }

    private void addGenderFeature(Alphabet xA, String personId1, String personId2, SparseVector sparseVector) {
        Set<String> gendersSetFirstPerson = getPersonGenderSet(personId1);
        Set<String> gendersSetSecondPerson = getPersonGenderSet(personId2);
        gendersSetFirstPerson.retainAll(gendersSetSecondPerson);
        sparseVector.add(xA.lookupObject("g"), (gendersSetFirstPerson.size() == 0) ? 0.0d : 1.0d);
    }

    private Set<String> getPersonGenderSet(String personId) {
        Set<String> gendersSetPerson = new HashSet<>();
        gendersSetPerson.add(statementsMap.get(personId, "gender"));
        gendersSetPerson.add(statementsMap.get(personId, "gender-LinearClass"));
        gendersSetPerson.add(statementsMap.get(personId, "gender-RuleBased"));
        return gendersSetPerson;
    }
}
