package com.ontotext.ehri.genders.rules;

import javafx.util.Pair;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.rio.RDFHandlerException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RuleBasedGenderAssignmentMain {

    public static void main(String[] args) throws IOException, RDFHandlerException, QueryEvaluationException, TupleQueryResultHandlerException {

        Namespace ns = getNamespace(args);

        if (ns != null) {
            String outputFile = ns.getString("outputFile");

            labelDataAndWriteToFile(outputFile);

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
        parser.addArgument("outputFile").help("output file");
        return parser;
    }

    private static void labelDataAndWriteToFile(String outputFile) throws FileNotFoundException, RDFHandlerException, QueryEvaluationException, TupleQueryResultHandlerException {
        Map<String, List<String>> allData = RuleBasedSPARQLData.getData();
        Map<String, Pair<String, Double>> labeledData = RuleBasedGenderAssignment.assignGendersByRule(allData);
        RuleBasedGenderWriter.writeLabeledDataToTRIGFile(outputFile, labeledData);
    }

}
