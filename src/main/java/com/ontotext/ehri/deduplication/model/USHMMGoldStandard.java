package com.ontotext.ehri.deduplication.model;

import types.ClassificationInstance;

import java.util.List;
import java.util.Map;

public class USHMMGoldStandard {

    private String goldStandardTSVInputFile;
    private String personStatementsMapCache;

    private Map<ClassificationInstance, USHMMPersonPair> classificationInstanceUSHMMPersonPairMap;

    public USHMMGoldStandard(String goldStandardTSVInputFile, String personStatementsMapCache) {
        this.goldStandardTSVInputFile = goldStandardTSVInputFile;
        this.personStatementsMapCache = personStatementsMapCache;
    }

    public List<ClassificationInstance> getClassificationInstances() {
        List<USHMMGoldStandardEntry> data = USHHMGoldStandardParser.parse(goldStandardTSVInputFile);
        USHMMPersonsFeatureExtractor featureExtractor = new USHMMPersonsFeatureExtractor();
        List<ClassificationInstance> classificationInstanceList = featureExtractor.getClassificationInstances(data, personStatementsMapCache);
        classificationInstanceUSHMMPersonPairMap = featureExtractor.getClassificationInstanceUSHMMPersonPairMap();
        return classificationInstanceList;
    }

    public Map<ClassificationInstance, USHMMPersonPair> getClassificationInstanceUSHMMPersonPairMap() {
        return classificationInstanceUSHMMPersonPairMap;
    }
}
