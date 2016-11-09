package com.ontotext.ehri.genders.rules;

import javafx.util.Pair;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;

class RuleBasedGenderWriter {

    private static final String BASE = "http://data.ehri-project.eu/";
    private static final String USHMM_PERSON_NAMESPACE = BASE + "ushmm/person/";
    private static final String ONTO_NAMESPACE = BASE + "ontotext/";
    private static final String ONTO_USHMM_NAMESPACE = BASE + "ontotext/ushmm/";

    static void writeLabeledDataToTRIGFile(String outputFile, Map<String, Pair<String, Double>> labeledData) throws RDFHandlerException, FileNotFoundException {
        RDFWriter rdfWriter = Rio.createWriter(RDFFormat.TRIG, new FileOutputStream(outputFile));
        ValueFactory valueFactory = new ValueFactoryImpl();

        rdfWriter.startRDF();
        writeLabeledData(labeledData, rdfWriter, valueFactory);
        rdfWriter.endRDF();
    }

    private static void writeLabeledData(Map<String, Pair<String, Double>> labeledData, RDFWriter rdfWriter, ValueFactory valueFactory) throws RDFHandlerException {
        writeHeaderStatements(rdfWriter, valueFactory);
        writeStatements(labeledData, rdfWriter, valueFactory);
    }

    private static void writeHeaderStatements(RDFWriter rdfWriter, ValueFactory valueFactory) throws RDFHandlerException {

        rdfWriter.handleNamespace(RDF.PREFIX, RDF.NAMESPACE);
        rdfWriter.handleNamespace(RDFS.PREFIX, RDFS.NAMESPACE);

        rdfWriter.handleStatement(valueFactory.createStatement(
                valueFactory.createURI(ONTO_NAMESPACE, "gender-RuleBased"),
                RDF.TYPE,
                valueFactory.createURI(ONTO_NAMESPACE, "Algorithm")
        ));
        rdfWriter.handleStatement(valueFactory.createStatement(
                valueFactory.createURI(ONTO_NAMESPACE, "gender-RuleBased"),
                valueFactory.createURI(ONTO_NAMESPACE, "computesProperty"),
                valueFactory.createURI(ONTO_NAMESPACE, "gender")
        ));
        rdfWriter.handleStatement(valueFactory.createStatement(
                valueFactory.createURI(ONTO_NAMESPACE, "gender-RuleBased"),
                RDFS.LABEL,
                new LiteralImpl("Rule Based Assignment of genders")
        ));

    }

    private static void writeStatements(Map<String, Pair<String, Double>> labeledData, RDFWriter rdfWriter, ValueFactory valueFactory) throws RDFHandlerException {
        for (String personId : labeledData.keySet()) {
            Pair<String, Double> labelScorePair = labeledData.get(personId);
            writeStatement(rdfWriter, valueFactory, personId, labelScorePair.getKey(), labelScorePair.getValue());
        }
    }

    private static void writeStatement(RDFWriter rdfWriter, ValueFactory valueFactory,
                                       String personId, String gender, Double p) throws RDFHandlerException {
        rdfWriter.handleStatement(valueFactory.createStatement(
                valueFactory.createURI(USHMM_PERSON_NAMESPACE, personId),
                valueFactory.createURI(ONTO_NAMESPACE, "gender"),
                valueFactory.createURI(ONTO_USHMM_NAMESPACE, personId + "-gender-RuleBased"),
                valueFactory.createURI(ONTO_USHMM_NAMESPACE, "gender-RuleBased")
        ));
        rdfWriter.handleStatement(valueFactory.createStatement(
                valueFactory.createURI(ONTO_USHMM_NAMESPACE, personId + "-gender-RuleBased"),
                RDF.TYPE,
                valueFactory.createURI(ONTO_NAMESPACE, "Statement"),
                valueFactory.createURI(ONTO_USHMM_NAMESPACE, "gender-RuleBased")
        ));
        rdfWriter.handleStatement(valueFactory.createStatement(
                valueFactory.createURI(ONTO_USHMM_NAMESPACE, personId + "-gender-RuleBased"),
                RDF.VALUE,
                valueFactory.createLiteral(gender.toLowerCase()),
                valueFactory.createURI(ONTO_USHMM_NAMESPACE, "gender-RuleBased")
        ));
        rdfWriter.handleStatement(valueFactory.createStatement(
                valueFactory.createURI(ONTO_USHMM_NAMESPACE, personId + "-gender-RuleBased"),
                valueFactory.createURI(ONTO_NAMESPACE, "probability"),
                valueFactory.createLiteral(p),
                valueFactory.createURI(ONTO_USHMM_NAMESPACE, "gender-RuleBased")
        ));
        rdfWriter.handleStatement(valueFactory.createStatement(
                valueFactory.createURI(ONTO_USHMM_NAMESPACE, personId + "-gender-RuleBased"),
                valueFactory.createURI(ONTO_NAMESPACE, "provenance"),
                valueFactory.createURI(ONTO_NAMESPACE, "gender-RuleBased"),
                valueFactory.createURI(ONTO_USHMM_NAMESPACE, "gender-RuleBased")
        ));
    }

}
