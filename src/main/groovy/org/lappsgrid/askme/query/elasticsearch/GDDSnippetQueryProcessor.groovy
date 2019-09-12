package org.lappsgrid.askme.query.elasticsearch

import org.lappsgrid.askme.core.api.Query
import org.lappsgrid.askme.core.api.QueryProcessor
import org.lappsgrid.askme.query.StopWords

/**
 *
 */
class GDDSnippetQueryProcessor implements QueryProcessor {
    boolean inclusive = false
    int limit = 10

    StopWords stopwords = new StopWords()

    Query transform(Query query) {
        String question = query.question
        String[] tokens = question.trim().split('\\W+')
        List<String> terms = removeStopWords(tokens)
        if (question.endsWith('?')) {
            question = question[0..-2]
        }
        String encoded = URLEncoder.encode(question, 'UTF-8')

        String queryString = "https://geodeepdive.org/api/snippets?limit=$limit&term=$encoded&clean"
        if (inclusive) {
            query += "&inclusive=true"
        }

        query.query = queryString
        query.terms = terms
        return query
    }

    List<String> removeStopWords(String[] tokens) {
        Closure filter = { List list, String word ->
            if (!stopwords.contains(word)) {
                list.add(word)
            }
            return list
        }
        return tokens.inject([], filter)
    }


}
