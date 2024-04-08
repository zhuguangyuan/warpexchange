package com.itranswarp.exchange.messaging;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@EnableKafka
@Configuration
public class MessagingConfiguration {

    final Logger logger = LoggerFactory.getLogger(getClass());

    Map<String, Object> producerConfigs(String bootstrapServers) {
        Map<String, Object> configs = new HashMap<>();
        /*
         * 发送成功保证
         * acks=all, retries = 3
         *
         * 发送成功且保证幂等且有序
         * enable.idempotence=true
         * // 无需设置 acks, 和 max.in.flight.requests.per.connection, kafka会自动设置
         * // 原理：kafka client保证，无需业务方重复发送，否则才会造成重复
         */
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return configs;
    }

    Map<String, Object> consumerConfigs(String bootstrapServers, int batchSize) {
        Map<String, Object> configs = new HashMap<>();
        // 默认情况下，消费者一次会poll条消息
        configs.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, Integer.valueOf(batchSize));
        //不管指定何值，如果已有消费记录offset,则从offset开始。否则从最新的开始(latest)消费, 或者从头开始(earliest)消费
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return configs;
    }

    @Bean
    public KafkaAdmin admin(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public ProducerFactory<String, String> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        logger.info("init kafka producer from bootstrap servers: {}", bootstrapServers);
        return new DefaultKafkaProducerFactory<>(producerConfigs(bootstrapServers));
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(@Autowired ProducerFactory<String, String> producerFactory) {
        logger.info("init kafka template...");
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.max-poll-records}") int batchSize) {
        logger.info("init kafka consumer from bootstrap servers: {}, batch-size: {}", bootstrapServers,
                Integer.valueOf(batchSize));
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(bootstrapServers, batchSize));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            @Autowired ConsumerFactory<String, String> consumerFactory) {
        logger.info("init concurrent kafka listener container factory...");
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        /*
         * 由于 setConcurrency 被设置为 1，在这种情况下，processMessages 方法将由单个线程处理。这个线程将负责从 Kafka 队列中拉取消息，
         * 并逐批地将它们传递给 processMessages 方法进行处理。
         *
         * 如果您希望增加并发处理能力，可以通过增加 setConcurrency 方法的值来实现。例如，如果您设置 setConcurrency(10)，
         * 那么将有 10 个线程并发地从 Kafka 队列中消费消息，并处理传入的批次。
         *
         * 请注意，增加并发性可能会提高消息处理的吞吐量，但也可能会增加系统复杂性，并可能需要更多的资源来管理这些并发线程。
         * 因此，在调整并发级别时，需要根据系统的具体需求和资源情况进行权衡。同时，确保 processMessages 方法是线程安全的，
         * 以便在多线程环境下正确地处理消息。
         */
        factory.setConcurrency(Integer.valueOf(1));

        factory.setBatchListener(Boolean.TRUE);
        return factory;
    }

    /**
     * List<ConsumerRecord<?,?>>records
     *
     */
}
