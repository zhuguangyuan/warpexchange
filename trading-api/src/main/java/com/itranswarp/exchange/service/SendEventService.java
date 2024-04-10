package com.itranswarp.exchange.service;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itranswarp.exchange.message.event.AbstractEvent;
import com.itranswarp.exchange.messaging.MessageProducer;
import com.itranswarp.exchange.messaging.Messaging;
import com.itranswarp.exchange.messaging.MessagingFactory;

/**
 * API 的消息发送服务
 * 主要用于 将下单、撤单、转账消息 发送到定序服务器，完成消息的定序
 */
@Component
public class SendEventService {

    @Autowired
    private MessagingFactory messagingFactory;

    private MessageProducer<AbstractEvent> messageProducer;

    @PostConstruct
    public void init() {
        this.messageProducer = messagingFactory.createMessageProducer(Messaging.Topic.SEQUENCE, AbstractEvent.class);
    }

    public void sendMessage(AbstractEvent message) {
        this.messageProducer.sendMessage(message);
    }
}
