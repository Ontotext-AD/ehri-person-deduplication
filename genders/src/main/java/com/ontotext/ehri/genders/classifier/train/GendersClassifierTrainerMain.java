package com.ontotext.ehri.genders.classifier.train;

import com.ontotext.ehri.genders.classifier.model.GendersParser;
import com.ontotext.ehri.genders.classifier.model.PersonClassificationInstance;
import com.ontotext.ehri.genders.classifier.model.PersonGenderFeatureExtractor;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import types.ClassificationInstance;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class GendersClassifierTrainerMain {

    public static void main(String[] args) throws IOException, URISyntaxException {

        Namespace ns = getNamespace(args);
        if (ns != null) {

            String inputDataSetPath = ns.getString("file");
            String resultsTsvFilepath = ns.getString("resultsTsvFilepath");
            String modelFilepath = ns.getString("modelFilepath");

            trainGendersClassifier(inputDataSetPath, resultsTsvFilepath, modelFilepath);
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
        parser.addArgument("file").help("input data set file (tsv format)");
        parser.addArgument("resultsTsvFilepath").help("output file with results");
        parser.addArgument("modelFilepath").help("file to save the model");
        return parser;
    }

    private static void trainGendersClassifier(String inputDataSetPath, String resultsTsvFilepath, String modelFilepath) throws IOException, URISyntaxException {
        List<PersonClassificationInstance> parsedData = GendersParser.parseData(inputDataSetPath);
        List<ClassificationInstance> allData = PersonGenderFeatureExtractor.getInstances(parsedData);
        GendersClassifierTrainer classifierTrainer = new GendersClassifierTrainer(allData);
        classifierTrainer.trainAndSaveModel(resultsTsvFilepath, modelFilepath);
    }

}
