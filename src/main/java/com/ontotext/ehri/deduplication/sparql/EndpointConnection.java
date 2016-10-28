package com.ontotext.ehri.deduplication.sparql;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

public class EndpointConnection {

    private RepositoryConnection connection;
    private HTTPRepository repository;

    public void open() {
        SPARQLEndpointConfig config = new SPARQLEndpointConfig();
        this.repository = new HTTPRepository(config.repositoryURL);
        this.repository.setUsernameAndPassword(config.username, config.password);
        this.connection = null;
        try {
            this.connection = repository.getConnection();
        } catch (RepositoryException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void close() {
        try {
            connection.close();
            repository.shutDown();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    public TupleQuery prepareSPARQLTupleQuery(String query) {
        TupleQuery tupleQuery = null;
        try {
            tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
        } catch (RepositoryException | MalformedQueryException e) {
            e.printStackTrace();
        }
        return tupleQuery;
    }
}
