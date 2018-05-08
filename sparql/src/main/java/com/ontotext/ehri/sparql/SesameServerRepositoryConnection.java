package com.ontotext.ehri.sparql;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

/**
 * Manages connections to a repository that serves as a proxy for a remote repository on a Sesame server.
 */

public class SesameServerRepositoryConnection {

    private RepositoryConnection connection;
    private HTTPRepository repository;

	/**
     * Opens a connection
     */
    public void open() {
        SesameServerRepositoryConfiguration config = new SesameServerRepositoryConfiguration();
        this.repository = new HTTPRepository(config.repositoryURL);
        this.repository.setUsernameAndPassword(config.username, config.password);
        this.connection = null;
        try {
            this.connection = repository.getConnection();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the connection and shuts down the repository
     */
    public void close() {
        try {
            connection.close();
            repository.shutDown();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

	/**
     * Prepares a SPARQL query that produces sets of value tuples.
     * @param query SPARQL query
     * @return The tuple query to be evaluated or null if an error occurred
     */
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
