package org.lappsgrid.askme.query

//import org.lappsgrid.eager.mining.core.json.Serializer
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.rabbitmq.Message
//import org.lappsgrid.rabbitmq.example.factory.IWorkerFactory
import org.lappsgrid.rabbitmq.tasks.TaskQueue
import org.lappsgrid.rabbitmq.tasks.Worker
import org.lappsgrid.rabbitmq.topic.MessageBox
import org.lappsgrid.rabbitmq.topic.PostOffice
import groovy.util.logging.Slf4j


@Slf4j("logger")
/**
 * Taken from org.lappsgrid.rabbitmq Distributed Task Example
 */

class QueueManager extends MessageBox {
    TaskQueue queue
    PostOffice po
    List<Worker> workers

    /**
     *
     * @param exchange the RabbitMQ message exchange.
     * @param address  the name of our mailbox.
     * @param host     the address of the RabbitMQ server
     * @param qName    the name of the TaskQueue managed by this QueueManager
     * @param factory  factory used to create workers
     * @param size     the number of workers to create
     */
    QueueManager(String exchange, String address, String host, String qName, QueryFactory factory, int size = 1) {
        super(exchange, address, host)
        queue = new TaskQueue(qName, host)
        po = new PostOffice(exchange, host)
        workers = [ ]
        size.times {
            workers.add(factory.create(po, queue))
        }
        workers.each { queue.register(it) }
    }

    /**
     *
     * @param exchange the RabbitMQ message exchange.
     * @param address  the name of our mailbox.
     * @param host     the address of the RabbitMQ server
     * @param qName    the name of the TaskQueue managed by this QueueManager
     * @param theWorkers the list of workers
     */
    QueueManager(String exchange, String address, String host, String qName, List<Worker> theWorkers) {
        super(exchange, address, host)
        queue = new TaskQueue(qName, host)
        po = new PostOffice(exchange, host)
        workers = [ ]
        theWorkers.each {
            workers.add(it)
        }
        workers.each { queue.register(it) }
    }

    @Override
    void close() {
        super.close()
        workers*.close()
        queue.close()
        po.close()
    }

    @Override
    void recv(Message message) {
        if(checkMessage(message)) {
            if (message.getCommand() == 'EXIT' || message.getCommand() == 'QUIT') {
                shutdown()
            } else {
                queue.send(Serializer.toJson(message))
            }
        }
    }
    void shutdown(){
        logger.info('Received shutdown message, terminating Query service')
        close()
        logger.info('Query service terminated')
        System.exit(0)
    }
    boolean checkMessage(Message message) {
        if (!(message.getId() && message.getBody())) {
            Map error_check = [:]
            error_check.origin = "Query"
            error_check.messageId = message.getId()
            if (message.getId() == '') {
                logger.info('ERROR: Message is missing Id')
                error_check.Id = 'MISSING'
            }
            if (message.getBody() == '') {
                logger.info('ERROR: Message has empty body')
                error_check.body = 'MISSING'
            }
            logger.info('Notifying Web service of error, message terminated')
            Message error_message = new Message()
            error_message.setCommand('ERROR')
            error_message.setBody(error_check)
            error_message.route('web.mailbox')
            po.send(error_message)
            return false
        }
        return true
    }
}
