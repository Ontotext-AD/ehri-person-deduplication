package com.ontotext.ehri.deduplication.model;

import types.ClassificationInstance;

import java.util.List;

public class USHMMGoldStandard {
    private String goldStandardTSVInputFile;
    private String personStatementsMapCache;

    public USHMMGoldStandard(String goldStandardTSVInputFile, String personStatementsMapCache) {
        this.goldStandardTSVInputFile = goldStandardTSVInputFile;
        this.personStatementsMapCache = personStatementsMapCache;
    }

    public List<ClassificationInstance> getClassificationInstances() {
        List<USHMMGoldStandardEntry> data = USHHMGoldStandardParser.parse(goldStandardTSVInputFile);
        USHMMPersonsFeatureExtractor featureExtractor = new USHMMPersonsFeatureExtractor();
        return featureExtractor.getClassificationInstances(data, personStatementsMapCache);
    }
}
