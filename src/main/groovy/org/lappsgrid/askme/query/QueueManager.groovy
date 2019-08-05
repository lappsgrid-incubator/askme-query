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
        if(message.getBody() == 'EXIT'){
            shutdown()
        }
        else {
            queue.send(Serializer.toJson(message))
        }
    }
    void shutdown(){
        logger.info('Received shutdown message, terminating askme-query')
        po.close()
        logger.info('askme-query terminated')
        System.exit(0)
    }
}
