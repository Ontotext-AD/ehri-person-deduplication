package com.ontotext.ehri.deduplication.classifier.model;

import com.ontotext.ehri.deduplication.indices.USHMMPersonIndex;
import javafx.util.Pair;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResultHandlerException;
import types.Alphabet;
import types.ClassificationInstance;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates list of classification instances from the parsed data.
 */

public class USHMMPersonsFeatureExtractor {

    public Map<ClassificationInstance, Pair<Pair<String, List[]>, Pair<String, List[]>>> getClassificationInstanceUSHMMPersonPairMap(
            List<USHMMGoldStandardEntry> data, String indexFileName, String personIdFSABin)
            throws ClassNotFoundException, QueryEvaluationException, TupleQueryResultHandlerException, IOException {

        Alphabet xA = new Alphabet(), yA = new Alphabet();
        Map<ClassificationInstance, Pair<Pair<String, List[]>, Pair<String, List[]>>> classificationInstanceUSHMMPersonPairMap = new HashMap<>();
        USHMMPersonIndex index = new USHMMPersonIndex(personIdFSABin, indexFileName);

        for (USHMMGoldStandardEntry entry : data) {
            Pair<Pair<String, List[]>, Pair<String, List[]>> personPair = new Pair<>(
                    new Pair<>(entry.personId1, index.getPerson(entry.personId1)),
                    new Pair<>(entry.personId2, index.getPerson(entry.personId2))
            );
            USHMMClassificationInstance instance = new USHMMClassificationInstance(xA, entry.personId1, entry.personId2, index);
            ClassificationInstance classificationInstance = new ClassificationInstance(xA, yA, instance.getSparseVector(), yA.lookupObject(entry.label));
            classificationInstanceUSHMMPersonPairMap.put(classificationInstance, personPair);
        }

        return classificationInstanceUSHMMPersonPairMap;

    }

}
