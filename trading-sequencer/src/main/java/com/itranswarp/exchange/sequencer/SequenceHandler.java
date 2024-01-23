package com.itranswarp.exchange.sequencer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.itranswarp.exchange.message.event.AbstractEvent;
import com.itranswarp.exchange.messaging.MessageTypes;
import com.itranswarp.exchange.model.trade.EventEntity;
import com.itranswarp.exchange.model.trade.UniqueEventEntity;
import com.itranswarp.exchange.support.AbstractDbService;

/**
 * Process events as batch.
 */
@Component
@Transactional(rollbackFor = Throwable.class) // 该类中所有方法纳入事务管理
public class SequenceHandler extends AbstractDbService {

    private long lastTimestamp = 0;

    /**
     * Set sequence for each message, persist into database as batch.
     * 1. 对每一个消息，如果有uniqueId 则去重(内存去重本批次、db去重所有)
     * 2. 对当前msg 添加sequenceId + previousId
     * 3. 对该消息，生成对应的EventEntity 落库保存，防止后续推送过程中有丢失，下游服务可以通过db查询到丢失的消息
     * @return Sequenced messages.
     */
    public List<AbstractEvent> sequenceMessages(final MessageTypes messageTypes,
                                                final AtomicLong sequence,
                                                final List<AbstractEvent> messages) throws Exception {
        final long t = System.currentTimeMillis();
        if (t < this.lastTimestamp) {
            logger.warn("[Sequence] current time {} is turned back from {}!", t, this.lastTimestamp);
        } else {
            this.lastTimestamp = t;
        }

        // 用于过滤历史消息中重复的
        List<UniqueEventEntity> uniqueEntityList = new ArrayList<>();
        // 用于过滤这一批的消息中重复的
        Set<String> uniqueKeys = new HashSet<>();

        List<AbstractEvent> sequencedMessages = new ArrayList<>(messages.size());
        List<EventEntity> events = new ArrayList<>(messages.size());

        for (AbstractEvent message : messages) {
            UniqueEventEntity uniqueEventEntity = null;
            final String uniqueId = message.uniqueId;
            // check uniqueId:
            if (uniqueId != null) {
                if (uniqueKeys.contains(uniqueId) || db.fetch(UniqueEventEntity.class, uniqueId) != null) {
                    logger.warn("ignore processed unique message: {}", message);
                    continue;
                }
                uniqueEventEntity = new UniqueEventEntity();
                uniqueEventEntity.uniqueId = uniqueId;
                uniqueEventEntity.createdAt = message.createdAt;
                uniqueEntityList.add(uniqueEventEntity);
                uniqueKeys.add(uniqueId);
                logger.info("unique event {} sequenced.", uniqueId);
            }

            final long previousId = sequence.get();
            final long currentId = sequence.incrementAndGet();

            // 接收到的来自上游的消息，是不包含这几个属性的，需要此处定序化后进行设置，供下游使用
            // 先设置message的sequenceId / previouseId / createdAt，再序列化并落库:
            message.sequenceId = currentId;
            message.previousId = previousId;
            message.createdAt = this.lastTimestamp;

            // 如果此消息关联了UniqueEvent，给UniqueEvent加上相同的sequenceId：
            if (uniqueEventEntity != null) {
                uniqueEventEntity.sequenceId = message.sequenceId;
            }

            // create AbstractEvent and save to db later:
            EventEntity event = new EventEntity();
            event.previousId = previousId;
            event.sequenceId = currentId;
            event.data = messageTypes.serialize(message);
            event.createdAt = this.lastTimestamp; // same as message.createdAt
            events.add(event);

            // will send later:
            sequencedMessages.add(message);
        }

        if (!uniqueEntityList.isEmpty()) {
            db.insert(uniqueEntityList);
        }
        db.insert(events);
        return sequencedMessages;
    }

    public long getMaxSequenceId() {
        EventEntity last = db.from(EventEntity.class).orderBy("sequenceId").desc().first();
        if (last == null) {
            logger.info("no max sequenceId found. set max sequenceId = 0.");
            return 0;
        }
        this.lastTimestamp = last.createdAt;
        logger.info("find max sequenceId = {}, last timestamp = {}", last.sequenceId, this.lastTimestamp);
        return last.sequenceId;
    }
}
