package org.lappsgrid.askme.query

import groovy.transform.CompileStatic
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.lappsgrid.askme.core.Configuration
import org.lappsgrid.askme.core.api.AskmeMessage
import org.lappsgrid.askme.core.api.Packet
import org.lappsgrid.askme.core.concurrent.Signal
import org.lappsgrid.askme.core.metrics.Tags
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.RabbitMQ
import org.lappsgrid.rabbitmq.topic.MailBox
import org.lappsgrid.rabbitmq.topic.PostOffice
import org.lappsgrid.serialization.Serializer
import groovy.util.logging.Slf4j
import io.micrometer.core.instrument.binder.logging.LogbackMetrics

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

    final PostOffice po
    final SimpleQueryProcessor processor
    MailBox box

    final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    Counter queriesProccessed
    Counter messagesReceived
    Timer timer

    Main() {

        logger.info("Host    : {}", config.HOST)
        logger.info("Exchange: {}", config.EXCHANGE)
        logger.info("Address : {}", config.QUERY_MBOX)
        po = new PostOffice(config.EXCHANGE, config.HOST)
        processor = new SimpleQueryProcessor()
        init()
    }

    void init() {
        new ClassLoaderMetrics().bindTo(registry)
        new JvmMemoryMetrics().bindTo(registry)
        new JvmGcMetrics().bindTo(registry)
        new ProcessorMetrics().bindTo(registry)
        new JvmThreadMetrics().bindTo(registry)
//        new LogbackMetrics().bindTo(registry)

        queriesProccessed = registry.counter("queries_count", "service", Tags.QUERY)
        messagesReceived = registry.counter("messages_count", "service", Tags.QUERY)
        timer = registry.timer("queries_timer", "service", Tags.QUERY)
    }

    void run() {
        logger.info("Service started")
        Signal exitSignal = new Signal()
        box = new MailBox(config.EXCHANGE, config.QUERY_MBOX, config.HOST) {
            @Override
            void recv(String s) {
                messagesReceived.increment()
                logger.trace("Message received.")
                logger.trace("Message: {}", s)
                AskmeMessage message = Serializer.parse(s, AskmeMessage)
                String id = message.getId()
                String command = message.getCommand()
                if(command == 'EXIT' || command == 'QUIT'){
                    logger.info('Received shutdown message, terminating Query service')
                    exitSignal.send()
                }
                else if(command == 'PING') {
                    logger.info('Received PING message from and sending response back to {}', message.route[0])
                    message.setCommand('PONG')
                    logger.info('Response PONG sent to {}', message.route[0])
                    Main.this.po.send(message)
                } else if (command == 'METRICS') {
                    Message response = new Message()
                    response.id = message.id
                    response.setCommand('ok')
                    response.body(registry.scrape())
                    response.route = message.route
                    logger.debug('Metrics sent to {}', response.route[0])
                    Main.this.po.send(response)
                } else {
                    queriesProccessed.increment()
                    logger.info("Received Message {}, processing question", id)
                    String destination = message.route[0] ?: 'the void'
                    Packet packet = (Packet) message.body
                    packet.query = timer.recordCallable { processor.transform(packet.query) }
                    //message.set("query", Serializer.toJson(q))
                    message.body = packet
                    Main.this.po.send(message)
                    logger.info('Processed question {} sent to {}', id, destination)
                }
            }
        }
        exitSignal.await()
        logger.info("Service terminating")
        box.close()
        po.close()
        logger.info('Service terminated')

    }

    static void main(String[] args) {
//        System.setProperty("RABBIT_USERNAME", "rabbit")
//        System.setProperty("RABBIT_PASSWORD", "rabbit")
        new Main().run()
    }

}

