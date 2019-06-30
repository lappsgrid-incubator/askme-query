package org.lappsgrid.eager.query

import org.lappsgrid.eager.mining.api.Query
import org.lappsgrid.eager.mining.api.QueryProcessor
import groovy.util.logging.Slf4j
import org.lappsgrid.rabbitmq.topic.MailBox
import org.lappsgrid.eager.query.GetSolrDocuments

/**
 * Removes stop words and creates a conjunction of the remaining words.
 */
@Slf4j("logger")
class SimpleQueryProcessor implements QueryProcessor {
    static final String BOX = 'query receiver'


    StopWords stopwords = new StopWords()

    Query transform(String question) {
        String[] tokens = question.trim().toLowerCase().split('\\W+')
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
    void run(){
        logger.info "Staring the query receiver"
        //print("Starting the query receiver")
        MailBox box = new MailBox('askme.prototype', BOX, 'rabbitmq.lappsgrid.org'){
            void recv(String question){

                GetSolrDocuments retrieve = new GetSolrDocuments()
                logger.trace("Received a question: {}", question)
                Query query = transform(question)
                retrieve.answer(query)
            }
        }

        //Not sure how to have Web communicate the latch with query
        //block()
        //sleep(10000)
        //logger.debug "query receiver shutting down."
        //box.close()
        //logger.info "query receiver terminated."
    }

    static void main(String[] args) {
        new SimpleQueryProcessor().run()
    }

}