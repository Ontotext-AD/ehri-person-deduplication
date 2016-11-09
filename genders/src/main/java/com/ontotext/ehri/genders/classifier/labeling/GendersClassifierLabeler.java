package com.ontotext.ehri.genders.classifier.labeling;

import com.ontotext.ehri.genders.classifier.model.PersonClassificationInstance;
import com.ontotext.ehri.genders.classifier.model.USHMMPersonGenderFeatureExtraction;
import javafx.util.Pair;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import types.Alphabet;
import types.LinearClassifier;
import types.SparseVector;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class GendersClassifierLabeler {

    private static final char DELIMITER = '\t';
    private static final String SPACE = " ";
    private static final String[] HEADER = {"personId", "firstName", "lastName", "nationality"};

    static Map<String, Pair<String, Double>> parseAndLabelData(String inputFile, LinearClassifier model, Alphabet xA, Alphabet yA) throws FileNotFoundException {
        Map<String, Pair<String, Double>> labeledData = new HashMap<>();
        tryToParseAndLabelData(inputFile, labeledData, model, xA, yA);
        return labeledData;
    }

    private static void tryToParseAndLabelData(String inputFile, Map<String, Pair<String, Double>> labeledData, LinearClassifier model, Alphabet xA, Alphabet yA) {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            parseAndLabelRecords(br, labeledData, model, xA, yA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseAndLabelRecords(BufferedReader br, Map<String, Pair<String, Double>> labeledData, LinearClassifier model, Alphabet xA, Alphabet yA) throws IOException {
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(DELIMITER).withHeader(HEADER).parse(br);
        for (CSVRecord record : records) {
            String personId = record.get("personId"), nationality = record.get("nationality");
            String firstName = record.get("firstName"), lastName = record.get("lastName");
            addLabeledRecordToData(labeledData, model, xA, yA, personId, nationality, firstName, lastName);
        }
    }

    private static void addLabeledRecordToData(Map<String, Pair<String, Double>> labeledData, LinearClassifier model, Alphabet xA, Alphabet yA, String personId, String nationality, String firstName, String lastName) {
        PersonClassificationInstance p = new PersonClassificationInstance(firstName, lastName, nationality);
        USHMMPersonGenderFeatureExtraction featureExtraction = new USHMMPersonGenderFeatureExtraction(p);
        SparseVector sparseVector = featureExtraction.getSparseVector(xA);
        String label = yA.lookupInt(model.label(sparseVector));

        if (p.normalizedName.contains(SPACE) && p.normalizedName.length() > 5)
            labeledData.put(personId, new Pair<>(label, model.labelScoreNormalized(sparseVector).get(label)));
    }

}
