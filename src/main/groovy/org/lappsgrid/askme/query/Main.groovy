package org.lappsgrid.askme.query

import org.lappsgrid.askme.core.api.Query
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.topic.MailBox
import org.lappsgrid.rabbitmq.topic.PostOffice
import org.lappsgrid.serialization.Serializer
import groovy.util.logging.Slf4j

/**
 * TODO:
 * 1) Update imports to phase out eager (waiting on askme-core pom)
 * 2) Add exceptions / case statements to recv method?
 * 3) Update the non-taskqueue Query version
 * 4) Best way to remove punctuation and stopwords?
 */

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
                Message message = Serializer.parse(s, Message)
                String id = message.getId()
                String command = message.getCommand()

                if(command == 'EXIT' || command == 'QUIT'){
                    logger.info('Received shutdown message, terminating Query service')
                    synchronized(lock) { lock.notify() }
                }
                else if(command == 'PING') {
                    String origin = message.getBody()
                    logger.info('Received PING message from and sending response back to {}', origin)
                    Message response = new Message()
                    response.setBody(MBOX)
                    response.setCommand('PONG')
                    response.setRoute([origin])
                    po.send(response)
                    logger.info('Response PONG sent to {}', origin)
                } else {
                    logger.info("Received Message {}, processing question", id)
                    Query query = process(message.body.toString())
                    Map params = message.getParameters()
                    Message response = new Message()
                    response.setBody(query)
                    response.setRoute([WEB_MBOX])
                    response.setCommand('query')
                    response.setId(id)
                    response.setParameters(params)
                    //askme-web needs time to start MailBox before response, otherwise response is lost
                    sleep(500)
                    po.send(response)
                    logger.info('Processed question {} sent back to web', id)
                }
            }
        }
        synchronized(lock) { lock.wait() }
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
        logger.info('Starting Query service')
        Object lock = new Object()
        Thread.start {
            new Main().run(lock)
        }

    }

}

