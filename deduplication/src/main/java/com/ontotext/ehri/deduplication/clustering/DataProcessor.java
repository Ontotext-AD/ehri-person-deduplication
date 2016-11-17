package com.ontotext.ehri.deduplication.clustering;

import com.ontotext.ehri.deduplication.sparql.EndpointConnection;
import com.ontotext.ehri.deduplication.sparql.QueryResultHandler;
import org.openrdf.query.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class DataProcessor {

    public static void main(String[] args) throws QueryEvaluationException, TupleQueryResultHandlerException, XMLStreamException, IOException {

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = factory.createXMLStreamWriter(new FileWriter("/home/nelly/data1.xml"));

        writer.writeStartDocument();
        writer.writeStartElement("add");

        String previousPersonId = "-1";
        Map<String, Set<String>> personPredicateObjectSetMap = new HashMap<>();

        EndpointConnection connection = new EndpointConnection();
        connection.open();
        TupleQuery query = connection.prepareSPARQLTupleQuery("" +
                "PREFIX ushmm: <http://data.ehri-project.eu/ushmm/ontology/>\n" +
                "PREFIX onto: <http://data.ehri-project.eu/ontotext/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "select ?personId ?firstName ?lastName ?normalizedFirstName ?normalizedLastName ?normalizedName ?dateBirth ?placeBirth ?nameDM ?firstNameDM ?lastNameDM ?gender ?genderLinearClass ?genderRuleBased ?sourceId ?personType ?occupation ?nameMotherFirstName ?nameMotherLastName ?nationality where {\n" +
                "    ?person a ushmm:Person;\n" +
                "        ushmm:personId ?personId.\n" +
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
        query.evaluate(new DataProcessorHandler(writer, previousPersonId, personPredicateObjectSetMap));
        connection.close();

        writePersonPredicateObjectSetMap(writer, personPredicateObjectSetMap);

        writer.writeEndElement();
        writer.writeEndDocument();

        writer.flush();
        writer.close();

    }

    private static void writePersonPredicateObjectSetMap(XMLStreamWriter writer, Map<String, Set<String>> personPredicateObjectSetMap) throws XMLStreamException {
        if ("5518082".equals(personPredicateObjectSetMap.get("personId").iterator().next())) {
            writer.writeStartElement("doc");

            writer.writeStartElement("field");
            writer.writeAttribute("name", "id");
            writer.writeCharacters(personPredicateObjectSetMap.get("personId").iterator().next());
            writer.writeEndElement();

            for (String predicate : personPredicateObjectSetMap.keySet()) {
                for (String value : personPredicateObjectSetMap.get(predicate)) {
                    writer.writeStartElement("field");
                    writer.writeAttribute("name", predicate);
                    writer.writeCharacters(value);
                    writer.writeEndElement();
                }
            }

            writer.writeEndElement();
        }
    }

    private static class DataProcessorHandler extends QueryResultHandler {
        XMLStreamWriter writer;
        String previousPersonId;
        Map<String, Set<String>> personPredicateObjectSetMap;
        int c = 0;

        DataProcessorHandler(XMLStreamWriter writer, String previousPersonId, Map<String, Set<String>> personPredicateObjectSetMap) {
            this.writer = writer;
            this.previousPersonId = previousPersonId;
            this.personPredicateObjectSetMap = personPredicateObjectSetMap;
        }

        @Override
        public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
            try {
                String personId = bindingSet.getValue("personId").stringValue();

                if ("5518082".equals(personId)) {
                    System.out.println(previousPersonId);
                    System.out.println(!personId.equals(previousPersonId));
                }

                if (!personId.equals(previousPersonId)) {
                    if ("5518082".equals(personId)) {
                        System.out.println(!personPredicateObjectSetMap.isEmpty());
                    }
                    if (!personPredicateObjectSetMap.isEmpty()) {
                        writePersonPredicateObjectSetMap(writer, personPredicateObjectSetMap);
                        ++c;
                        if (c % 100000 == 0) System.out.println(c);
                    }
                    personPredicateObjectSetMap.clear();
                }

                for (String bindingName : bindingSet.getBindingNames()) {
                    if (bindingSet.hasBinding(bindingName)) {
                        Set<String> valuesSet = personPredicateObjectSetMap.get(bindingName);
                        if (valuesSet == null) valuesSet = new HashSet<>();
                        valuesSet.add(bindingSet.getValue(bindingName).stringValue());
                        personPredicateObjectSetMap.put(bindingName, valuesSet);
                        if ("5518082".equals(personId)) {
                            System.out.println("put " + bindingName + " " +  valuesSet);
                        }
                    }
                }

            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }
}
