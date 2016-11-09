package com.ontotext.ehri.genders.rules;

import javafx.util.Pair;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.openrdf.rio.RDFHandlerException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RuleBasedGenderAssignmentMain {

    public static void main(String[] args) throws IOException, RDFHandlerException {

        Namespace ns = getNamespace(args);

        if (ns != null) {
            String inputFile = ns.getString("inputFile");
            String outputFile = ns.getString("outputFile");

            labelDataAndWriteToFile(inputFile, outputFile);

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
        parser.addArgument("inputFile").help("input file");
        parser.addArgument("outputFile").help("output file");
        return parser;
    }

    private static void labelDataAndWriteToFile(String inputFile, String outputFile) throws FileNotFoundException, RDFHandlerException {
        Map<String, List<String>> parsedData = RuleBasedParser.parseData(inputFile);
        Map<String, Pair<String, Double>> labeledData = RuleBasedGenderAssignment.assignGendersByRule(parsedData);
        RuleBasedGenderWriter.writeLabeledDataToTRIGFile(outputFile, labeledData);
    }

}
