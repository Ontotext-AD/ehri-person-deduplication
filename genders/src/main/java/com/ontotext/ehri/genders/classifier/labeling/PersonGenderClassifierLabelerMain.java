package com.ontotext.ehri.genders.classifier.labeling;

import javafx.util.Pair;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.rio.RDFHandlerException;
import types.LinearClassifier;
import utils.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class PersonGenderClassifierLabelerMain {

    public static void main(String[] args) throws Exception {
        Namespace ns = getNamespace(args);

        if (ns != null) {
            String outputFile = ns.getString("output");
            String modelFile = ns.getString("model");

            loadModelAndLabelData(outputFile, modelFile);
        }
    }

    private static Namespace getNamespace(String[] args) {
        ArgumentParser parser = getArgumentParser();
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
        return ns;
    }

    private static ArgumentParser getArgumentParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("prog");
        parser.addArgument("output").help("output file");
        parser.addArgument("model").help("model");
        return parser;
    }

    private static void loadModelAndLabelData(String outputFile, String modelFile) throws IOException, ClassNotFoundException, RDFHandlerException, QueryEvaluationException, TupleQueryResultHandlerException {
        LinearClassifier model = (LinearClassifier) IOUtils.loadModel(new File(modelFile).toURI().toURL());
        Map<String, Pair<String, Double>> labeledData = PersonGenderClassifierLabeler.getLabeledData(model);
        PersonGenderClassifierWriter.writeLabeledDataToTRIGFile(outputFile, labeledData);

    }

}
