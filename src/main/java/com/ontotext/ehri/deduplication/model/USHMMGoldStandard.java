package com.ontotext.ehri.deduplication.model;

import types.ClassificationInstance;

import java.util.List;
import java.util.Map;

public class USHMMGoldStandard {

    private String goldStandardTSVInputFile;
    private String personStatementsMapCache;

    public USHMMGoldStandard(String goldStandardTSVInputFile, String personStatementsMapCache) {
        this.goldStandardTSVInputFile = goldStandardTSVInputFile;
        this.personStatementsMapCache = personStatementsMapCache;
    }

    public Map<ClassificationInstance, USHMMPersonPair> getClassificationInstanceUSHMMPersonPairMap() {
        List<USHMMGoldStandardEntry> data = USHHMGoldStandardParser.parse(goldStandardTSVInputFile);
        USHMMPersonsFeatureExtractor featureExtractor = new USHMMPersonsFeatureExtractor();
        return featureExtractor.getClassificationInstanceUSHMMPersonPairMap(data, personStatementsMapCache);
    }
}
