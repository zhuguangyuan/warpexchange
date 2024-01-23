package com.itranswarp.exchange.message.event;

import org.springframework.lang.Nullable;

import com.itranswarp.exchange.message.AbstractMessage;

/**
 * 定序系统
 * 接收可选uniqueId的消息，定序完成后，设置sequenceId和previousId 再发送给下游
 * 消息的接收和发送无需事务，但是持久化阶段要用事务，所以定义一个SequenceHandler
 */
public class AbstractEvent extends AbstractMessage {

    /**
     * Message id, set after sequenced.
     */
    public long sequenceId;

    /**
     * Previous message sequence id.
     */
    public long previousId;

    /**
     * Unique ID or null if not set.
     */
    @Nullable
    public String uniqueId;
}
