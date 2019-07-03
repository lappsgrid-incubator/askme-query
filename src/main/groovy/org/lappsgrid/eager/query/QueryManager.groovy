package org.lappsgrid.eager.query

import org.lappsgrid.eager.mining.api.Query
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.tasks.TaskQueue
import org.lappsgrid.rabbitmq.topic.MailBox
import groovy.util.logging.Slf4j
import org.lappsgrid.rabbitmq.topic.PostOffice



@Slf4j("logger")
class QueryManager {
    static final String BOX = 'QueryManager'

    //Should this be private?
    static final String TASK_QUEUE_NAME = "question_queue"
    TaskQueue question_queue = new TaskQueue(TASK_QUEUE_NAME,'rabbitmq.lappsgrid.org')


    void run(){
        logger.info "Staring the QueryManager"


        MailBox box = new MailBox('askme.prototype', BOX, 'rabbitmq.lappsgrid.org'){
            void recv(String question){
                logger.trace("Received a question: {}", question)
                logger.trace("Adding question to question_queue")

                question_queue.
                question_queue.send('hello')
                logger.trace("Question sent")

            }
        }
    }

    static void main(String[] args) {
        new QueryManager().run()
    }
}
