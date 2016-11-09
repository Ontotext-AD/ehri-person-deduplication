package com.ontotext.ehri.genders.classifier.model;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GendersParser {

    private static final char DELIMITER = '\t';
    private static final String[] HEADER = {"personId", "gender", "firstName", "lastName", "nationality"};

    public static List<PersonClassificationInstance> parseData(String file) throws IOException {

        List<PersonClassificationInstance> parsedData = new ArrayList<>();
        tryToParseTSVFile(file, parsedData);
        return parsedData;
    }

    private static void tryToParseTSVFile(String file, List<PersonClassificationInstance> parsedData) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            parseTSVFile(br, parsedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseTSVFile(BufferedReader br, List<PersonClassificationInstance> parsedData) throws IOException {
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(DELIMITER).withHeader(HEADER).parse(br);
        for (CSVRecord record : records)
            parsedData.add(new PersonClassificationInstance(record.get("firstName"), record.get("lastName"), record.get("gender"), record.get("nationality")));
    }

}
