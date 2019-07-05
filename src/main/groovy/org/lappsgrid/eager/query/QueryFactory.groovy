package org.lappsgrid.eager.query

import org.lappsgrid.eager.query.QueryWorker
import org.lappsgrid.rabbitmq.tasks.TaskQueue
import org.lappsgrid.rabbitmq.topic.PostOffice

import java.util.concurrent.CountDownLatch

class QueryFactory{

    int id = 0
    CountDownLatch latch

    QueryFactory(CountDownLatch latch) {
        this.latch = latch
    }


    QueryWorker create(PostOffice po, TaskQueue queue) {
        return new QueryWorker(++id, latch, po, queue)
    }
}

