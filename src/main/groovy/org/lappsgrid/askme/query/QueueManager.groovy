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
    Object lock

    /**
     *
     * @param exchange the RabbitMQ message exchange.
     * @param address  the name of our mailbox.
     * @param host     the address of the RabbitMQ server
     * @param qName    the name of the TaskQueue managed by this QueueManager
     * @param factory  factory used to create workers
     * @param size     the number of workers to create
     */
    QueueManager(String exchange, String address, String host, String qName, QueryFactory factory, int size = 1, Object l) {
        super(exchange, address, host)
        queue = new TaskQueue(qName, host)
        po = new PostOffice(exchange, host)
        workers = [ ]
        size.times {
            workers.add(factory.create(po, queue))
        }
        workers.each { queue.register(it) }
        lock = l
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
        logger.info("Queue, workers, PostOffice, and super shut down")
    }

    @Override
    void recv(Message message) {
        if(message.getCommand() == 'EXIT' || message.getCommand() == 'QUIT'){
            shutdown(lock)
        }
         else {

            queue.send(Serializer.toJson(message))
        }

    }

    void shutdown(Object lock){
        logger.info('Received shutdown message, terminating Query service')
        synchronized(lock) { lock.notify() }
    }




    //currently not used
    boolean checkMessage(Message message) {
        if (!message.getBody()) {
            Map error_check = [:]
            error_check.origin = "Query"
            error_check.messageId = message.getId()

            if (message.getBody() == '') {
                logger.info('ERROR: Message {} has empty body', message.getId())
                error_check.body = 'MISSING'
            }
            logger.info('Notifying Web service of error')
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
