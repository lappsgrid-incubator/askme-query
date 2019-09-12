package org.lappsgrid.askme.query

import org.lappsgrid.askme.core.api.Query
import org.lappsgrid.askme.core.api.QueryProcessor


/**
 * Returns the input string unchanged.
 */
class IdentityProcessor implements QueryProcessor {

    Query transform(Query query) {
        query.query = query.question
        query.terms = query.question.tokenize(' ')
        return query
    }
}
