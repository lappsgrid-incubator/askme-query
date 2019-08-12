package org.lappsgrid.askme.query

import groovy.util.logging.Slf4j
import org.lappsgrid.rabbitmq.topic.PostOffice



@Slf4j("logger")
class QueryManager {
    static final String HOST = "rabbitmq.lappsgrid.org"
    static final String EXCHANGE = "org.lappsgrid.query"
    static final String QUERY_Q = "query.q"
    static final String QUERY_MBOX = "query.mailbox"



    void run(Object lock){
        logger.debug("Starting the query task queue.")
        PostOffice po = new PostOffice(EXCHANGE, HOST)
        QueueManager queryMaster = new QueueManager(EXCHANGE,QUERY_MBOX,HOST,QUERY_Q, new QueryFactory(),1, lock)
        logger.debug("query task queue started, awaiting question.")

        synchronized(lock) { lock.wait() }
        po.close()
        queryMaster.close()
        logger.info("Query service terminated")
        System.exit(0)
    }



    static void main(String[] args) {
        Object lock = new Object()
        Thread.start {
            new QueryManager().run(lock)
        }

    }
}
