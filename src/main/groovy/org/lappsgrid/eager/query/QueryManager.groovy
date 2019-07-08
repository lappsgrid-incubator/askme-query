package org.lappsgrid.eager.query

import com.rabbitmq.client.Consumer
import com.rabbitmq.client.DefaultConsumer
import org.lappsgrid.eager.mining.api.Query
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.tasks.TaskQueue
import org.lappsgrid.rabbitmq.topic.MailBox
import groovy.util.logging.Slf4j
import org.lappsgrid.rabbitmq.topic.MessageBox
import org.lappsgrid.rabbitmq.topic.PostOffice
import org.lappsgrid.rabbitmq.SimpleConsumer
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.eager.query.QueryWorker


@Slf4j("logger")
class QueryManager {
    static final String HOST = "rabbitmq.lappsgrid.org"
    static final String EXCHANGE = "org.lappsgrid.query"
    static final String QUERY_Q = "query.q"
    static final String QUERY_MBOX = "query.mailbox"



    void run(){
        logger.debug("Starting the query task queue.")
        PostOffice po = new PostOffice(EXCHANGE, HOST)
        QueueManager queryMaster = new QueueManager(EXCHANGE,QUERY_MBOX,HOST,QUERY_Q, new QueryFactory(),1)
        logger.debug("query task queue started, awaiting question.")
    }



    static void main(String[] args) {
        new QueryManager().run()
    }
}
