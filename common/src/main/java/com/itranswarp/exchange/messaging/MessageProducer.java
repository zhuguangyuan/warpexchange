package com.itranswarp.exchange.messaging;

import java.util.List;

import com.itranswarp.exchange.message.AbstractMessage;

/**
 * 实际实现时，是调用KafkaRestTemplate.send来发送消息
 * @param <T>
 */
@FunctionalInterface
public interface MessageProducer<T extends AbstractMessage> {

    void sendMessage(T message);

    default void sendMessages(List<T> messages) {
        for (T message : messages) {
            sendMessage(message);
        }
    }
}
