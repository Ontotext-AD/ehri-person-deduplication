package com.ontotext.ehri.genders.classifier.train;

import com.ontotext.ehri.genders.classifier.model.PersonGenderInputDataCache;
import com.ontotext.ehri.genders.classifier.model.PersonClassificationInstance;
import com.ontotext.ehri.genders.classifier.model.PersonGenderClassificationInstanceFactory;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import types.ClassificationInstance;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class PersonGenderClassifierTrainerMain {

    public static void main(String[] args) throws IOException, URISyntaxException, QueryEvaluationException, TupleQueryResultHandlerException {

        Namespace ns = getNamespace(args);
        if (ns != null) {
            String inputDataFileCache = ns.getString("inputDataFileCache");
            String resultsTsvFilepath = ns.getString("resultsTsvFilepath");
            String modelFilepath = ns.getString("modelFilepath");

            trainGendersClassifier(inputDataFileCache, resultsTsvFilepath, modelFilepath);
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
        ArgumentParser parser = ArgumentParsers.newArgumentParser("genders-linear-classifier-trainer");
        parser.addArgument("inputDataFileCache").help("Input data set file cache binary");
        parser.addArgument("resultsTsvFilepath").help("output file with results");
        parser.addArgument("modelFilepath").help("file to save the model");
        return parser;
    }

    private static void trainGendersClassifier(String inputDataFileCache, String resultsTsvFilepath, String modelFilepath) throws URISyntaxException, QueryEvaluationException, TupleQueryResultHandlerException, IOException {
        List<PersonClassificationInstance> inputData = PersonGenderInputDataCache.getData(inputDataFileCache);
        List<ClassificationInstance> allData = PersonGenderClassificationInstanceFactory.getInstances(inputData);
        PersonGenderClassifierTrainer classifierTrainer = new PersonGenderClassifierTrainer(allData);
        classifierTrainer.trainAndSaveModel(resultsTsvFilepath, modelFilepath);
    }

}
