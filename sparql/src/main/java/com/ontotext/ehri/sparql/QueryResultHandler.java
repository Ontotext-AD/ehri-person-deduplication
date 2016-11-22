package com.ontotext.ehri.sparql;

import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

import java.util.List;

public abstract class QueryResultHandler implements TupleQueryResultHandler {
    @Override
    public void handleBoolean(boolean b) throws QueryResultHandlerException {

    }

    @Override
    public void handleLinks(List<String> list) throws QueryResultHandlerException {

    }

    @Override
    public void startQueryResult(List<String> list) throws TupleQueryResultHandlerException {

    }

    @Override
    public void endQueryResult() throws TupleQueryResultHandlerException {

    }
}
