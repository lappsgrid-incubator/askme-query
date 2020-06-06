package org.lappsgrid.askme.query.elasticsearch

import org.elasticsearch.index.query.Operator
import org.lappsgrid.askme.core.api.Query
import org.lappsgrid.askme.core.api.QueryProcessor
import org.lappsgrid.askme.query.StopWords

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 *
 */
class ESQueryProcessor implements QueryProcessor {

    final StopWords stopwords = new StopWords()
    final Map<String,Float> fields = [
            title: 5.0,
            abstract: 2.0,
            body: 1.0
    ]

    @Override
    Query transform(Query query) {
        String question = query.question.trim()
        if (question.endsWith('?')) {
            question = question.substring(0, question.length()-1)
        }
        String[] tokens = question.trim().toLowerCase().split('\\s+')
        query.terms = removeStopWords(tokens)
        String queryString = query.terms.join(" ")
        query.query = simpleQueryStringQuery(queryString)
                .fields(fields)
                .defaultOperator(Operator.AND)
                .toString()
//        query.query = matchQuery('body', query.question)
//                .operator(Operator.AND)
//                .toString()
        return query
    }

    Query _transform(Query query) {
        //TODO normalize contractions and remove punctuation.
//        query.query = query.terms.collect { '(body:' + it + ' OR abstract:' + it + ')' }.join(' AND ')

        query.query = query.terms.collect{ 'body:' + it}.join(' AND ')
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

    static void main(String[] args) {
        Query q = new Query("When did the coronavirus first appear in New York City.")
        println new ESQueryProcessor().transform(q).query
    }

}