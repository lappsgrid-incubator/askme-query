package org.lappsgrid.askme.query

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

