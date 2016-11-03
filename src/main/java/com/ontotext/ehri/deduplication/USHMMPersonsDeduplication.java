package com.ontotext.ehri.deduplication;

import com.ontotext.ehri.deduplication.model.USHMMGoldStandard;
import com.ontotext.ehri.deduplication.classifier.ClassifierTrainer;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Train a linear classifier (multithreaded sigmoid perceptron) over the gold standard for the USHMM person deduplication task
 */

public class USHMMPersonsDeduplication {

    public static void main(String[] args) throws Exception {

        Namespace ns = getNamespace(args);
        if (ns != null) {

            String goldStandardTSVInputFile = ns.getString("goldStandardTSVInputFile");
            String personStatementsMapCache = ns.getString("personStatementsMapCache");
            String resultsTsvFilepath = ns.getString("resultsTsvFilepath");
            String modelFilepath = ns.getString("modelFilepath");

            trainClassifierOverUSHMMGoldStandard(
                    goldStandardTSVInputFile, personStatementsMapCache,
                    resultsTsvFilepath, modelFilepath
            );

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
        ArgumentParser parser = ArgumentParsers.newArgumentParser("ehri-person-deduplication");
        parser.addArgument("goldStandardTSVInputFile").help("Gold Standard Input File in tsv format");
        parser.addArgument("personStatementsMapCache").help("Persons Statements Map Cache");
        parser.addArgument("resultsTsvFilepath").help("Output tsv file with the results");
        parser.addArgument("modelFilepath").help("Bin file to save the model");
        return parser;
    }

    private static void trainClassifierOverUSHMMGoldStandard(String goldStandardTSVInputFile, String personStatementsMapCache,
                                                             String resultsTsvFilepath, String modelFilepath)
            throws IOException, URISyntaxException {
        USHMMGoldStandard goldStandard = new USHMMGoldStandard(
                goldStandardTSVInputFile, personStatementsMapCache
        );
        ClassifierTrainer classifierTrainer = new ClassifierTrainer(
                goldStandard.getClassificationInstances(),
                goldStandard.getClassificationInstanceUSHMMPersonPairMap()
        );
        classifierTrainer.trainAndSaveModel(resultsTsvFilepath, modelFilepath);
    }

}
