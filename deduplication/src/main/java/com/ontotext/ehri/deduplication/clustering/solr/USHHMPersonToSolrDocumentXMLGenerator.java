package com.ontotext.ehri.deduplication.clustering.solr;

import com.ontotext.ehri.deduplication.sparql.EndpointConnection;
import com.ontotext.ehri.deduplication.sparql.QueryResultHandler;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class USHHMPersonToSolrDocumentXMLGenerator {

    private static final String INITIAL_PREVIOUS_PERSON_ID = "-1";

    public static void main(String[] args) throws QueryEvaluationException, TupleQueryResultHandlerException, XMLStreamException, IOException {
        generatePersonDocumentXML();
    }

    private static void generatePersonDocumentXML() throws XMLStreamException, IOException, QueryEvaluationException, TupleQueryResultHandlerException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(new FileWriter("/home/nelly/data.xml"));
        writeDocument(writer);
        writer.flush();
        writer.close();
    }

    private static void writeDocument(XMLStreamWriter writer) throws XMLStreamException, QueryEvaluationException, TupleQueryResultHandlerException {
        writer.writeStartDocument();
        writeAddElement(writer);
        writer.writeEndDocument();
    }

    private static void writeAddElement(XMLStreamWriter writer) throws XMLStreamException, QueryEvaluationException, TupleQueryResultHandlerException {
        writer.writeStartElement("add");
        writeDocElements(writer);
        writer.writeEndElement();
    }

    private static void writeDocElements(XMLStreamWriter writer) throws QueryEvaluationException, TupleQueryResultHandlerException, XMLStreamException {
        String previousPersonId = INITIAL_PREVIOUS_PERSON_ID;
        Map<String, Set<String>> personPredicateObjectSetMap = new HashMap<>();
        querySPARQLEndpointAndWriteDocElements(writer, previousPersonId, personPredicateObjectSetMap);
        writeDocElement(writer, personPredicateObjectSetMap);
    }

    private static void writeDocElement(XMLStreamWriter writer, Map<String, Set<String>> personPredicateObjectSetMap) throws XMLStreamException {
        writer.writeStartElement("doc");
        writeFieldElements(writer, personPredicateObjectSetMap);
        writer.writeEndElement();
    }

    private static void writeFieldElements(XMLStreamWriter writer, Map<String, Set<String>> personPredicateObjectSetMap) throws XMLStreamException {
        for (String predicate : personPredicateObjectSetMap.keySet())
            writePredicateFieldElements(writer, personPredicateObjectSetMap, predicate);
    }

    private static void writePredicateFieldElements(XMLStreamWriter writer, Map<String, Set<String>> personPredicateObjectSetMap, String predicate) throws XMLStreamException {
        for (String value : personPredicateObjectSetMap.get(predicate))
            writeFieldValueElement(writer, predicate, value);
    }

    private static void writeFieldValueElement(XMLStreamWriter writer, String predicate, String value) throws XMLStreamException {
        writer.writeStartElement("field");
        writer.writeAttribute("name", predicate);
        writer.writeCharacters(value);
        writer.writeEndElement();
    }

    private static void querySPARQLEndpointAndWriteDocElements(XMLStreamWriter writer, String previousPersonId, Map<String, Set<String>> personPredicateObjectSetMap) throws QueryEvaluationException, TupleQueryResultHandlerException {
        EndpointConnection connection = new EndpointConnection();
        connection.open();
        TupleQuery query = connection.prepareSPARQLTupleQuery("" +
                "PREFIX ushmm: <http://data.ehri-project.eu/ushmm/ontology/>\n" +
                "PREFIX onto: <http://data.ehri-project.eu/ontotext/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "select ?id ?firstName ?lastName ?normalizedFirstName ?normalizedLastName ?normalizedName ?dateBirth ?placeBirth ?nameDM ?firstNameDM ?lastNameDM ?gender ?genderLinearClass ?genderRuleBased ?sourceId ?personType ?occupation ?nameMotherFirstName ?nameMotherLastName ?nationality where {\n" +
                "    ?person a ushmm:Person;\n" +
                "        ushmm:personId ?id.\n" +
                "    optional { ?person ushmm:firstName ?firstName }\n" +
                "    optional { ?person ushmm:lastName ?lastName }\n" +
                "    optional { ?person onto:normalizedFirstName ?normalizedFirstName }\n" +
                "    optional { ?person onto:normalizedLastName ?normalizedLastName }\n" +
                "    optional { ?person onto:normalizedName ?normalizedName }\n" +
                "    optional { ?person ushmm:dateBirth / ushmm:date ?dateBirth }\n" +
                "    optional { ?person ushmm:placeBirth / ushmm:cityTown ?placeBirth }\n" +
                "    optional { ?person onto:nameDM ?nameDM }\n" +
                "    optional { ?person onto:firstNameDM ?firstNameDM }\n" +
                "    optional { ?person onto:lastNameDM ?lastNameDM }\n" +
                "    optional { ?person ushmm:gender ?gender }\n" +
                "    optional { ?person onto:gender ?g.?g rdf:value ?genderLinearClass. ?g onto:provenance onto:gender-LinearClass }\n" +
                "    optional { ?person onto:gender ?g. ?g rdf:value ?genderRuleBased. ?g onto:provenance onto:gender-RuleBased }\n" +
                "    optional { ?source a ushmm:Source; ushmm:documents ?person; ushmm:sourceId ?sourceId }\n" +
                "    optional { ?person ushmm:personType ?personType }\n" +
                "    optional { ?person ushmm:occupation ?occupation }\n" +
                "    optional { ?person ushmm:nameMother / ushmm:firstName ?nameMotherFirstName }\n" +
                "    optional { ?person ushmm:nameMother / ushmm:lastName ?nameMotherLastName }\n" +
                "    optional { ?person ushmm:nationality / ushmm:nationality ?nationality }\n" +
                "}");
        query.evaluate(new USHHMPersonToSolrDocumentXMLGeneratorHandler(writer, previousPersonId, personPredicateObjectSetMap));
        connection.close();
    }

    private static class USHHMPersonToSolrDocumentXMLGeneratorHandler extends QueryResultHandler {

        XMLStreamWriter writer;
        String previousPersonId;
        Map<String, Set<String>> personPredicateObjectSetMap;

        USHHMPersonToSolrDocumentXMLGeneratorHandler(XMLStreamWriter writer, String previousPersonId, Map<String, Set<String>> personPredicateObjectSetMap) {
            this.writer = writer;
            this.previousPersonId = previousPersonId;
            this.personPredicateObjectSetMap = personPredicateObjectSetMap;
        }

        @Override
        public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
            tryToWriteDocElement(bindingSet);
        }

        private void tryToWriteDocElement(BindingSet bindingSet) {
            try {
                writeDocElementIfCurrentPersonIdNotEqualPrevious(bindingSet);
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }

        private void writeDocElementIfCurrentPersonIdNotEqualPrevious(BindingSet bindingSet) throws XMLStreamException {
            String personId = bindingSet.getValue("id").stringValue();
            if (!personId.equals(previousPersonId))
                writeAndClearPersonPredicateObjectMap();
            addBindingsValueSetToPersonPredicateObjectsSetMap(bindingSet);
            previousPersonId = personId;
        }

        private void writeAndClearPersonPredicateObjectMap() throws XMLStreamException {
            if (!personPredicateObjectSetMap.isEmpty())
                writeDocElement(writer, personPredicateObjectSetMap);
            personPredicateObjectSetMap.clear();
        }

        private void addBindingsValueSetToPersonPredicateObjectsSetMap(BindingSet bindingSet) {
            bindingSet.getBindingNames().stream().filter(bindingSet::hasBinding).forEach(bindingName -> addBindingValueSetToPersonPredicateObjectsSetMap(bindingSet, bindingName));
        }

        private void addBindingValueSetToPersonPredicateObjectsSetMap(BindingSet bindingSet, String bindingName) {
            Set<String> valuesSet = personPredicateObjectSetMap.get(bindingName);
            if (valuesSet == null)
                valuesSet = new HashSet<>();
            valuesSet.add(bindingSet.getValue(bindingName).stringValue());
            personPredicateObjectSetMap.put(bindingName, valuesSet);
        }
    }
}
