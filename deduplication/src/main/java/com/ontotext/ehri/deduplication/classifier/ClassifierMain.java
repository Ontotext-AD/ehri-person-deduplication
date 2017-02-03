package com.ontotext.ehri.deduplication.classifier;

import com.ontotext.ehri.deduplication.classifier.model.USHHMGoldStandardParser;
import com.ontotext.ehri.deduplication.classifier.model.USHMMGoldStandardEntry;
import com.ontotext.ehri.deduplication.classifier.model.USHMMPersonsFeatureExtractor;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Train a linear classifier (multithreaded sigmoid perceptron) over the gold standard for the USHMM person deduplication task
 */

public class ClassifierMain {

    public static void main(String[] args) throws Exception {

        Namespace ns = getNamespace(args);
        if (ns != null) {

            String goldStandardTSVFile = ns.getString("goldStandardTSVFile");
            String personsIndexFileName = ns.getString("personsIndexFileName");
            String personIdFSABin = ns.getString("personIdFSABin");
            String resultsTsvFilepath = ns.getString("resultsTsvFilepath");
            String modelFilepath = ns.getString("modelFilepath");

            trainClassifierOverUSHMMGoldStandard(
                    goldStandardTSVFile, personsIndexFileName, personIdFSABin,
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
        ArgumentParser parser = ArgumentParsers.newArgumentParser("classifier-train");
        parser.addArgument("goldStandardTSVFile").help("Gold Standard File in tsv format");
        parser.addArgument("personsIndexFileName").help("Persons Index File Name");
        parser.addArgument("personIdFSABin").help("PersonId Finite State Automaton Binary File");
        parser.addArgument("resultsTsvFilepath").help("Output tsv file with the results");
        parser.addArgument("modelFilepath").help("Bin file to save the model");
        return parser;
    }

    private static void trainClassifierOverUSHMMGoldStandard(String goldStandardTSVFile, String personsIndexFileName, String personIdFSABin,
                                                             String resultsTsvFilepath, String modelFilepath)
            throws IOException, URISyntaxException, QueryEvaluationException, TupleQueryResultHandlerException, ClassNotFoundException {
        List<USHMMGoldStandardEntry> data = USHHMGoldStandardParser.parse(goldStandardTSVFile);
        USHMMPersonsFeatureExtractor featureExtractor = new USHMMPersonsFeatureExtractor();
        ClassifierTrainer classifierTrainer = new ClassifierTrainer(featureExtractor.getClassificationInstanceUSHMMPersonPairMap(data, personsIndexFileName, personIdFSABin));
        classifierTrainer.trainAndSaveModel(resultsTsvFilepath, modelFilepath);
    }

}
