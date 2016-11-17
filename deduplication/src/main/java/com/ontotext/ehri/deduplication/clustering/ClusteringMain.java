package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.model.USHMMPerson;
import com.ontotext.ehri.deduplication.utils.SerializationUtils;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import types.LinearClassifier;
import utils.io.IOUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ClusteringMain {

    private static final Logger logger = LoggerFactory.getLogger(ClusteringMain.class);

    private static final double epsilon = 0.000001d;
    private static final int minimalPoints = 2;

    public static void main(String[] args) throws IOException, ClassNotFoundException, QueryEvaluationException, TupleQueryResultHandlerException {
        Namespace ns = getNamespace(args);
        if (ns != null) {
            String modelFilepath = ns.getString("modelFilepath");
            String cacheFileName = ns.getString("cacheFileName");
            String resultsDir = ns.getString("resultsDir");
            String dataFileName = ns.getString("dataFileName");
            clusterData(modelFilepath, cacheFileName, dataFileName, resultsDir);
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
        ArgumentParser parser = ArgumentParsers.newArgumentParser("ehri-person-clustering");
        parser.addArgument("modelFilepath").help("Bin file to save the model");
        parser.addArgument("cacheFileName").help("Data file cache");
        parser.addArgument("resultsDir").help("Directory to save the results");
        parser.addArgument("dataFileName").help("Data file name");
        return parser;
    }

    private static void clusterData(String modelFilepath, String cacheFileName, String dataFileName, String resultsDir) throws IOException, ClassNotFoundException, QueryEvaluationException, TupleQueryResultHandlerException {
        LinearClassifier model = (LinearClassifier) IOUtils.loadModel(new File(modelFilepath).toURI().toURL());
        DBSCANClustering dbScan =  new DBSCANClustering(epsilon, minimalPoints, new DistanceMeasure(model));
        List<USHMMPerson> data = getDataFromCache(cacheFileName, dataFileName);
        System.out.println(data.size());
//        Map<String, Map<String, Set<String>>> statementsMap = (Map<String, Map<String, Set<String>>>) SerializationUtils.deserialize(dataFileCache);
//        List<USHMMPerson> data = getUSHMMPersonsListFromMap(statementsMap);
        List<Cluster<USHMMPerson>> clusters = dbScan.cluster(data);
        ClusteringResultsWriter<USHMMPerson> writer = new ClusteringResultsWriter<>();
        writer.printResults(resultsDir, clusters, data, epsilon, minimalPoints);
    }

    private static List<USHMMPerson> getDataFromCache(String cacheFileName, String dataFileName) {
        File cacheFile = new File(cacheFileName);
        if (cacheFile.exists())
            return (List<USHMMPerson>) SerializationUtils.deserialize(cacheFileName);
        Map<String, Map<String, Set<String>>> dataMap = tryToParseData(dataFileName);
        List<USHMMPerson> data = getUSHMMPersonsListFromMap(dataMap);
        SerializationUtils.serialize(data, cacheFileName);
        return data;
    }

    private static List<USHMMPerson> getUSHMMPersonsListFromMap(Map<String, Map<String, Set<String>>> dataMap) {
        return dataMap.keySet().stream().map(
                personId -> new USHMMPerson(personId, dataMap.get(personId))
        ).collect(Collectors.toList());
    }

    private static Map<String, Map<String, Set<String>>> tryToParseData(String dataFileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(dataFileName))) {
            return parseData(br);
        } catch (FileNotFoundException e) {
            logger.error("Gold Standard File Not Found ", e);
            throw new ParserException();
        } catch (IOException e) {
            logger.error("Input/Output Exception ", e);
            throw new ParserException();
        }
    }

    private static Map<String, Map<String, Set<String>>> parseData(BufferedReader br) throws IOException {
        String[] header = {
                "personId", "firstName", "lastName", "normalizedFirstName", "normalizedLastName", "normalizedName",
                "dateBirth", "placeBirth", "nameDM", "firstNameDM", "lastNameDM", "gender", "genderLinearClass",
                "genderRuleBased", "sourceId", "personType", "occupation", "nameMotherFirstName", "nameMotherLastName",
                "nationality"
        };
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(',').withHeader(header).parse(br);
        Map<String, Map<String, Set<String>>> data = new HashMap<>();
        for (CSVRecord record : records) {
            String personId = record.get("personId");
            Map<String, Set<String>> statementsMap = data.get(personId);
            if (statementsMap == null)
                statementsMap = new HashMap<>();
            for (String predicate : header)
            {
                Set<String> predicateValues = statementsMap.get(predicate);
                if (predicateValues == null)
                    predicateValues = new HashSet<>();
                predicateValues.add(record.get(predicate));
                statementsMap.put(predicate, predicateValues);
            }
            data.put(personId, statementsMap);
        }
        return data;
    }

    private static class ParserException extends RuntimeException {
    }
}
