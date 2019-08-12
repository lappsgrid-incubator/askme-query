package org.lappsgrid.askme.query

import org.lappsgrid.eager.mining.api.Query
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.topic.MailBox
import org.lappsgrid.rabbitmq.topic.PostOffice
import org.lappsgrid.serialization.Serializer
import groovy.util.logging.Slf4j


@Slf4j("logger")
class Main {
    static final String MBOX = 'query.mailbox'
    static final String HOST = "rabbitmq.lappsgrid.org"
    static final String EXCHANGE = "org.lappsgrid.query"
    static final String WEB_MBOX = 'web.mailbox'
    static final PostOffice po = new PostOffice(EXCHANGE,HOST)
    MailBox box

    void run(Object lock) {
        box = new MailBox(EXCHANGE,MBOX,HOST) {
            @Override
            void recv(String s) {
                Message question = Serializer.parse(s, Message)
                String id = question.getId()
                String command = question.getCommand()
                if(command == 'EXIT' || command == 'QUIT'){
                    logger.info('Received shutdown message')
                    synchronized(lock) { lock.notify() }
                }
                else {
                    logger.info("Received Message {}, processing question", id)
                    Query query = process(question.body.toString())
                    Message response = new Message()
                    response.setBody(query)
                    response.setRoute([WEB_MBOX])
                    response.setCommand('query')
                    response.setId(id)
                    po.send(response)
                    logger.info('Processed question {} sent back to web', id)
                }
            }
        }
        synchronized(lock) { lock.wait() }
        logger.info('Shutting down Query service')
        box.close()
        po.close()
        logger.info('Query service terminated')

    }
    Query process(String question){
        SimpleQueryProcessor queryProcessor = new SimpleQueryProcessor()
        logger.trace("Processing question: {}", question)
        Query query = queryProcessor.transform(question)
        return query

    }
    static void main(String[] args) {
        Object lock = new Object()
        Thread.start {
            new Main().run(lock)
        }

    }

}

