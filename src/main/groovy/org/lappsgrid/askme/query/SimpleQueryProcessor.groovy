package org.lappsgrid.askme.query

import groovy.util.logging.Slf4j
import org.lappsgrid.askme.core.api.Query
import org.lappsgrid.askme.core.api.QueryProcessor


/**
 * Removes stop words and creates a conjunction of the remaining words.
 */
@Slf4j("logger")
class SimpleQueryProcessor implements QueryProcessor {


    final StopWords stopwords = new StopWords()

    Query transform(Query query) {
        //TODO normalize contractions and remove punctuation.
        String question = query.question.trim()
        if (question.endsWith('?')) {
            question = question.substring(0, question.length()-1)
        }
        String[] tokens = question.trim().toLowerCase().split('\\s+')
        query.terms = removeStopWords(tokens)
        query.query = query.terms.collect { 'body:' + it }.join(' AND ')

        query
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