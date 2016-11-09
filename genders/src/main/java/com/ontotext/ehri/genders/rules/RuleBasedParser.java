package com.ontotext.ehri.genders.rules;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RuleBasedParser {

    private static final String[] HEADER = {"personId", "gender", "firstName", "lastName", "firstNameTransliterated", "lastNameTransliterated"};
    private static final char DELIMITER = '\t';

    static Map<String, List<String>> parseData (String inputFile) {
        Map<String, List<String>> parsedData = new HashMap<>();
        tryToParseDataToMap(inputFile, parsedData);
        return parsedData;
    }

    private static void tryToParseDataToMap(String inputFile, Map<String, List<String>> parsedData) {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            parseDataToMap(br, parsedData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseDataToMap(BufferedReader br, Map<String, List<String>> parsedData) throws IOException {
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(DELIMITER).withHeader(HEADER).parse(br);
        for (CSVRecord record : records)
            addRecordToMap(parsedData, record);
    }

    private static void addRecordToMap(Map<String, List<String>> parsedData, CSVRecord record) {
        List<String> personDataList = getPersonDataList(record);
        parsedData.put(record.get("personId"), personDataList);
    }

    private static List<String> getPersonDataList(CSVRecord record) {
        List<String> personDataList = new ArrayList<>(5);
        personDataList.add(record.get("gender"));
        personDataList.add(record.get("firstName"));
        personDataList.add(record.get("lastName"));
        personDataList.add(record.get("firstNameTransliterated"));
        personDataList.add(record.get("lastNameTransliterated"));
        return personDataList;
    }

}
