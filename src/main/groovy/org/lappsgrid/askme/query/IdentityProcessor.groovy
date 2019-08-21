package org.lappsgrid.askme.query

import org.lappsgrid.askme.core.api.Query
import org.lappsgrid.askme.core.api.QueryProcessor


/**
 * Returns the input string unchanged.
 */
class IdentityProcessor implements QueryProcessor {

    Query transform(String question) {
        List<String> terms = question.tokenize(' ')
        return new Query(question, question, terms)
    }
}
