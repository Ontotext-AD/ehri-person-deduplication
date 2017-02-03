package com.ontotext.ehri.deduplication.classifier.model;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class USHHMGoldStandardParser {

    private static final Logger logger = LoggerFactory.getLogger(USHHMGoldStandardParser.class);

    private static final String[] HEADER = {"personId1", "personId2", "label"};
    private static final char DELIMITER = '\t';

    public static List<USHMMGoldStandardEntry> parse(String file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            return parseData(br);
        } catch (FileNotFoundException e) {
            logger.error("Gold Standard File Not Found ", e);
            throw new USHMMGoldStandardParserException();
        } catch (IOException e) {
            logger.error("Input/Output Exception ", e);
            throw new USHMMGoldStandardParserException();
        }
    }

    private static List<USHMMGoldStandardEntry> parseData(BufferedReader br)
            throws IOException {
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(DELIMITER).withHeader(HEADER).parse(br);
        List<USHMMGoldStandardEntry> data = new ArrayList<>();
        for (CSVRecord record : records)
            data.add(new USHMMGoldStandardEntry(record.get("personId1"), record.get("personId2"), record.get("label")));
        return data;
    }

    private static class USHMMGoldStandardParserException extends RuntimeException {
    }

}
