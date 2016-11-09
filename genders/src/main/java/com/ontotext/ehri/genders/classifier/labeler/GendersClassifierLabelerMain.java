package com.ontotext.ehri.genders.classifier;

import com.ontotext.ehri.genders.classifier.model.PersonClassificationInstance;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import types.Alphabet;
import types.LinearClassifier;
import types.SparseVector;
import utils.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

public class GendersClassifierLabelerMain {
    public static void main(String[] args) throws Exception {

        ArgumentParser parser = ArgumentParsers.newArgumentParser("prog");
        parser.addArgument("input").help("input file");
        parser.addArgument("output").help("output file");
        parser.addArgument("model").help("model");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        String inputFile = ns.getString("input");
        String outputFile = ns.getString("output");
        String modelFile = ns.getString("model");

        LinearClassifier model = (LinearClassifier) IOUtils.loadModel(new File(modelFile).toURI().toURL());
        Alphabet xA = model.getxAlphabet();
        Alphabet yA = model.getyAlphabet();

        RDFWriter rdfWriter = Rio.createWriter(RDFFormat.TRIG, new FileOutputStream(outputFile));
        ValueFactory valueFactory = new ValueFactoryImpl();

        rdfWriter.startRDF();
        handleNamespacesAndAlgorithmStatements(rdfWriter, valueFactory);

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(DELIMITER).withHeader(
                    "personId", "firstName", "lastName", "nationality"
            ).parse(br);
            for (CSVRecord record : records) {
                String personId = record.get("personId");
                String firstName = record.get("firstName");
                String lastName = record.get("lastName");
                String nationality = record.get("nationality");

                PersonClassificationInstance p = new PersonClassificationInstance(firstName, lastName, nationality);
                SparseVector sparseVector = p.getSparseVector(xA);
                String gender = yA.lookupInt(model.label(sparseVector));

                if (p.getNormalizedName().contains(SPACE) && p.getNormalizedName().length() > 5)
                    handleStatements(rdfWriter, valueFactory, personId, gender, model.labelScoreNormalized(sparseVector).get(gender));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        rdfWriter.endRDF();

    }
}
