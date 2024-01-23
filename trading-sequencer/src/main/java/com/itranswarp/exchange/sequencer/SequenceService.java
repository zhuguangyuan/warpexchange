package com.itranswarp.exchange.sequencer;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import com.itranswarp.exchange.message.event.AbstractEvent;
import com.itranswarp.exchange.messaging.MessageConsumer;
import com.itranswarp.exchange.messaging.MessageProducer;
import com.itranswarp.exchange.messaging.MessageTypes;
import com.itranswarp.exchange.messaging.Messaging;
import com.itranswarp.exchange.messaging.MessagingFactory;
import com.itranswarp.exchange.support.LoggerSupport;

/**
 * Sequence events.
 * 消费来自kafka的消息
 * 定序后存储到db 然后往下游发送
 *
 * 注意，任何时刻只能有一个定序实例在工作，如果要保证高可用，可以采用锁竞争的方式
 * 获取到锁的就是工作实例，否则就是备份实例
 * 这个方法在解决matrix engine 单实例问题也可以应用
 */
@Component
public class SequenceService extends LoggerSupport implements CommonErrorHandler {

    private static final String GROUP_ID = "SequencerGroup";

    @Autowired
    private SequenceHandler sequenceHandler;

    @Autowired
    private MessagingFactory messagingFactory;

    @Autowired
    private MessageTypes messageTypes;

    private MessageProducer<AbstractEvent> messageProducer;

    private AtomicLong sequence;
    private Thread jobThread;
    private boolean running;

    @PostConstruct
    public void init() {
        Thread thread = new Thread(() -> {
            logger.info("start sequence job...");
            // TODO: try get global DB lock:
            // while (!hasLock()) { sleep(10000); }
            this.messageProducer = this.messagingFactory.createMessageProducer(Messaging.Topic.TRADE,
                    AbstractEvent.class);

            // find max event id:
            this.sequence = new AtomicLong(this.sequenceHandler.getMaxSequenceId());

            // init consumer:
            logger.info("create message consumer for {}...", getClass().getName());
            // share same group id:
            MessageConsumer consumer = this.messagingFactory.createBatchMessageListener(Messaging.Topic.SEQUENCE,
                    GROUP_ID, this::processMessages, this);
            // start running:
            this.running = true;
            while (running) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            // close message consumer:
            logger.info("close message consumer for {}...", getClass().getName());
            consumer.stop();
            System.exit(1);
        });
        this.jobThread = thread;
        this.jobThread.start();
    }

    @PreDestroy
    public void shutdown() {
        logger.info("shutdown sequence service...");
        running = false;
        if (jobThread != null) {
            jobThread.interrupt();
            try {
                jobThread.join(5000);
            } catch (InterruptedException e) {
                logger.error("interrupt job thread failed", e);
            }
            jobThread = null;
        }
    }

    private boolean crash = false;

    /**
     * Message consumer error handler
     */
    @Override
    public void handleBatch(Exception thrownException, ConsumerRecords<?, ?> data, Consumer<?, ?> consumer,
            MessageListenerContainer container, Runnable invokeListener) {
        logger.error("batch error!", thrownException);
        panic();
    }

    private void sendMessages(List<AbstractEvent> messages) {
        this.messageProducer.sendMessages(messages);
    }

    private synchronized void processMessages(List<AbstractEvent> messages) {
        if (!running || crash) {
            panic();
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("do sequence for {} messages...", messages.size());
        }
        long start = System.currentTimeMillis();
        List<AbstractEvent> sequenced = null;
        try {
            // 对上游来的消息，进行去重检测、生成对应的entity存储db, 然后拿到定序后的msgs
            sequenced = this.sequenceHandler.sequenceMessages(this.messageTypes, this.sequence, messages);
        } catch (Throwable e) {
            logger.error("exception when do sequence", e);
            shutdown();
            panic();
            throw new Error(e);
        }

        if (logger.isInfoEnabled()) {
            long end = System.currentTimeMillis();
            logger.info("sequenced {} messages in {} ms. current sequence id: {}", messages.size(), (end - start),
                    this.sequence.get());
        }
        sendMessages(sequenced);
    }

    private void panic() {
        this.crash = true;
        this.running = false;
        System.exit(1);
    }
}
