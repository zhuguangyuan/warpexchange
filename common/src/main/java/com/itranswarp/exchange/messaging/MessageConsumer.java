package com.itranswarp.exchange.messaging;

/**
 * 消息消费者
 * 实际上调用 listener 来监听消息
 * todo 怎么提交的？
 */
@FunctionalInterface
public interface MessageConsumer {

    void stop();

}
