package org.lappsgrid.askme.query.elasticsearch

import org.elasticsearch.index.query.Operator
import org.lappsgrid.askme.core.api.Query
import org.lappsgrid.askme.core.api.QueryProcessor

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 *
 */
class ESQueryProcessor implements QueryProcessor {

    @Override
    Query transform(String question) {
        return matchQuery('contents', question)
                .operator(Operator.AND)
                .toString()
    }

    static void main(String[] args) {
        println new ESQueryProcessor().transform("Why did the quick brown fox jump over the lazy dog.")
    }

}