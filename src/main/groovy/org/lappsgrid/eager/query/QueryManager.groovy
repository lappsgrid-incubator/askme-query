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
        String test = "This is a test query"
        PostOffice po = new PostOffice(EXCHANGE, HOST)
        QueueManager queryMaster = new QueueManager(EXCHANGE,QUERY_MBOX,HOST,QUERY_Q, new QueryFactory(),1)
        int id = 1
        Message message = new Message()
                .body(test)
                .route(QUERY_MBOX)
                .set("id", "msg$id")
        po.send(message)

    }









    /**


    static final String BOX = 'QueryManager'
    TaskQueue query_queue = new TaskQueue("question_queue",'rabbitmq.lappsgrid.org',true,true)
    PostOffice po = new PostOffice('askme.query','rabbitmq.lappsgrid.org')


    void run() {
        MailBox box = new MailBox('askme.prototype', BOX, 'rabbitmq.lappsgrid.org') {
            void recv(String question) {
                logger.trace("Received a question: {}", question)
                Consumer w = new QueryWorker(query_queue)
                query_queue.register(w)
                //query_queue.send(Serializer.toJson(question))
                query_queue.send(question)
            }
        }
    }






    void run(){
        TaskQueue question_queue = new TaskQueue("question_queue",'rabbitmq.lappsgrid.org',true,true)

        Consumer consumer = new DefaultConsumer() {

        }
        question_queue.register(consumer)


        logger.info "Staring the QueryManager"
        question_queue.send('Hello')
        logger.info("Message sent")


        MailBox box = new MailBox('askme.prototype', BOX, 'rabbitmq.lappsgrid.org'){
            void recv(String question){
                logger.trace("Received a question: {}", question)
                question_queue.send(question)
                logger.trace("Question sent")

            }
        }
        **/


    static void main(String[] args) {
        new QueryManager().run()
    }
}
