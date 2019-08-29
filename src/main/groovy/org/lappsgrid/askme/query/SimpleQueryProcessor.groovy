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

    Query transform(String question) {
        //TODO normalize contractions and remove punctuation.
        String[] tokens = question.trim().toLowerCase().split('\\s+')
        List<String> terms = removeStopWords(tokens)
        String query = terms.collect { 'body:' + it }.join(' AND ')

        return new Query()
                .query(query)
                .question(question)
                .terms(terms);
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