package org.lappsgrid.askme.query

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.lappsgrid.askme.core.Configuration
import org.lappsgrid.askme.core.api.AskmeMessage
import org.lappsgrid.askme.core.api.Packet
import org.lappsgrid.askme.core.api.Query
import org.lappsgrid.askme.core.api.QueryProcessor
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.RabbitMQ
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

@CompileStatic
@Slf4j("logger")
class Main {
    static final Configuration config = new Configuration()

    final PostOffice po = new PostOffice(config.EXCHANGE, config.HOST)
    final SimpleQueryProcessor processor = new SimpleQueryProcessor()
    MailBox box

    void run() {
        Object lock = new Object()
        box = new MailBox(config.EXCHANGE, 'query.mailbox', config.HOST) {
            @Override
            void recv(String s) {
                logger.info("Message received.")
                AskmeMessage message = Serializer.parse(s, AskmeMessage)
                String id = message.getId()
                String command = message.getCommand()
                if(command == 'EXIT' || command == 'QUIT'){
                    logger.info('Received shutdown message, terminating Query service')
                    synchronized(lock) { lock.notify() }
                }
                else if(command == 'PING') {
                    logger.info('Received PING message from and sending response back to {}', message.route[0])
                    Message response = new Message()
//                    response.setBody('PONG')
                    response.setCommand('PONG')
                    response.setRoute(message.route)
                    logger.info('Response PONG sent to {}', response.route[0])
                    Main.this.po.send(response)
                } else {
                    logger.info("Received Message {}, processing question", id)
                    String destination = message.route[0] ?: 'the void'
                    Packet packet = message.body
                    packet.query = processor.transform(packet.query)
                    //message.set("query", Serializer.toJson(q))
                    message.body = packet
                    Main.this.po.send(message)
                    logger.info('Processed question {} sent to {}', id, destination)
                }
            }
        }
        synchronized(lock) { lock.wait() }
        box.close()
        po.close()
        logger.info('Query service terminated')

    }

    static void main(String[] args) {
        logger.info('Starting Query service')
        Thread.start {
            new Main().run()
        }

    }

}

