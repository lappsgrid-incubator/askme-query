package org.lappsgrid.askme.query

import org.lappsgrid.eager.mining.api.Query


//import org.lappsgrid.eager.mining.core.json.Serializer
import org.lappsgrid.serialization.Serializer

import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.tasks.TaskQueue
import org.lappsgrid.rabbitmq.tasks.Worker
import org.lappsgrid.rabbitmq.topic.PostOffice
import java.util.concurrent.CountDownLatch
import groovy.util.logging.Slf4j


@Slf4j("logger")
class QueryWorker extends Worker{
    int id
    CountDownLatch latch
    PostOffice po

    QueryWorker(int id, CountDownLatch latch, PostOffice po, TaskQueue queue){
        super(queue)
        this.id = id
        this.po = po
        this.latch = latch
    }

    Query process(String question){
        SimpleQueryProcessor queryProcessor = new SimpleQueryProcessor()
        logger.trace("Processing question: {}", question)
        Query query = queryProcessor.transform(question)
        return query

    }

    @Override
    void work(String json){
        Message question = Serializer.parse(json, Message)
        logger.info("Starting a QueryWorker for question {}", question.getId())
        Query query = process(question.body.toString())
        question.setBody(query)
        question.setRoute(['web.mailbox'])
        question.setCommand('query')
        po.send(question)
        logger.info('Processed question, query sent back to web')
    }
}
