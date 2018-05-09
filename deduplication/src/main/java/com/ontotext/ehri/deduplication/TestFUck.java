package com.ontotext.ehri.deduplication;

import com.ontotext.ehri.deduplication.classifier.model.USHHMGoldStandardParser;
import com.ontotext.ehri.deduplication.classifier.model.USHMMGoldStandardEntry;
import com.ontotext.ehri.deduplication.classifier.model.USHMMPersonsFeatureExtractor;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;
import types.ClassificationInstance;

public class TestFUck {


  public static void main(String[] args) throws Exception {
    String goldStandardTSVFile = "/home/neli/projects/ehri/person-deduplication-task/model/goldStandardOriginalPairs.tsv";
    String personsIndexFileName = "/home/neli/projects/ehri/ehri-person-deduplication/deduplication/target/indices/index.bin";
    String personIdFSABin = "/home/neli/projects/ehri/ehri-person-deduplication/deduplication/target/indices/personIdFSA.bin";
    List<USHMMGoldStandardEntry> data = USHHMGoldStandardParser.parse(goldStandardTSVFile);
    USHMMPersonsFeatureExtractor featureExtractor = new USHMMPersonsFeatureExtractor();

    Map<ClassificationInstance, Pair<Pair<String, List[]>, Pair<String, List[]>>> m =
        featureExtractor.getClassificationInstanceUSHMMPersonPairMap(data, personsIndexFileName, personIdFSABin);

    for (ClassificationInstance inst : m.keySet()) {
      System.out.println(inst.x);
      System.out.println(inst.y);
    }
  }

}
