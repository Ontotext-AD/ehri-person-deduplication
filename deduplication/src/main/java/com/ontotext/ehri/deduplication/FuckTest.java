package com.ontotext.ehri.deduplication;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;

public class FuckTest {
    public static void main(String[] args) throws FileNotFoundException, RDFHandlerException {

        RDFWriter rdfWriter = Rio.createWriter(RDFFormat.TURTLE, new FileOutputStream("/home/nely/data.ttl"));
        ValueFactory valueFactory = new ValueFactoryImpl();

        rdfWriter.startRDF();

//        rdfWriter.handleNamespace(RDF.PREFIX, RDF.NAMESPACE);
//        rdfWriter.handleNamespace(RDFS.PREFIX, RDFS.NAMESPACE);

        try (BufferedReader br = new BufferedReader(new FileReader("/home/nely/query-result-persons.csv"))) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader("c", "t", "l").parse(br);
            for (CSVRecord record : records) {
                rdfWriter.handleStatement(valueFactory.createStatement(
                        valueFactory.createURI(record.get("c")),
                        valueFactory.createURI(record.get("t")),
                        valueFactory.createLiteral(record.get("l"))
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new FileReader("/home/nely/query-result-locations.csv"))) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader("c", "t", "l").parse(br);
            for (CSVRecord record : records) {
                rdfWriter.handleStatement(valueFactory.createStatement(
                        valueFactory.createURI(record.get("c")),
                        valueFactory.createURI(record.get("t")),
                        valueFactory.createLiteral(record.get("l"))
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new FileReader("/home/nely/query-result-organizations.csv"))) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader("c", "t", "l").parse(br);
            for (CSVRecord record : records) {
                rdfWriter.handleStatement(valueFactory.createStatement(
                        valueFactory.createURI(record.get("c")),
                        valueFactory.createURI(record.get("t")),
                        valueFactory.createLiteral(record.get("l"))
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        rdfWriter.endRDF();

    }
}
