package org.lappsgrid.eager.query

import org.lappsgrid.eager.mining.api.Query
import org.lappsgrid.rabbitmq.topic.MailBox
import org.lappsgrid.rabbitmq.tasks.TaskQueue
import org.lappsgrid.rabbitmq.tasks.Worker

import groovy.util.logging.Slf4j

@Slf4j("logger")
class QueryWorker extends Worker {
    static final String TASK_QUEUE_NAME = "question_queue"
    //Should this be private?
    static final TaskQueue question_queue = new TaskQueue(TASK_QUEUE_NAME,'rabbitmq.lappsgrid.org')


    QueryWorker(){
        super(question_queue)
    }



    String process(String question){
        SimpleQueryProcessor queryProcessor = new SimpleQueryProcessor()
        GetSolrDocuments documentGetter = new GetSolrDocuments()
        logger.trace("Received a question: {}", question)
        Query query = queryProcessor.transform(question)

        //need env for solr config, temp not working
        //documentGetter.answer(query)
        //next, send along documents to NLP

        //temp for testing
        return question

    }

    @Override
    void work(String question) {
        logger.info("Starting the QueryWorker, answering question: {}", question)
        String answer = process(question)
        logger.info("Answered question: {}", question)
        logger.info("Answer: {}", answer)
    }

    /**
    void run(){

        MailBox box = new MailBox('askme.prototype', BOX, 'rabbitmq.lappsgrid.org'){
            void recv(String question){

                SimpleQueryProcessor queryProcessor = new SimpleQueryProcessor()
                GetSolrDocuments documentGetter = new GetSolrDocuments()
                logger.trace("Received a question: {}", question)
                Query query = queryProcessor.transform(question)

                //need env for solr config, temp not working
                documentGetter.answer(query)
                //next, send along documents to NLP

            }
        }
    }

    static void main(String[] args) {
        new QueryWorker().run()
    }
    **/
}
