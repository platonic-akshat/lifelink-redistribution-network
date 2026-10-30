package com.lifelink.coordination.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
public class DltReplayService {

    private final String bootstrapServers;

    public DltReplayService(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public ReplayResult replay(String dltTopic, int partition, long offset) {
        TopicPartition topicPartition = new TopicPartition(dltTopic, partition);
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "lifelink-dlt-replay-" + System.nanoTime());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.assign(List.of(topicPartition));
            consumer.seek(topicPartition, offset);
            ConsumerRecord<String, byte[]> record = consumer.poll(Duration.ofSeconds(5)).records(topicPartition)
                    .stream()
                    .filter(r -> r.offset() == offset)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No record at " + dltTopic + "-" + partition + "@" + offset));

            String originalTopic = headerValue(record, KafkaHeaders.DLT_ORIGINAL_TOPIC);
            if (originalTopic == null || originalTopic.isBlank()) {
                originalTopic = dltTopic.endsWith(".DLT") ? dltTopic.substring(0, dltTopic.length() - 4) : dltTopic;
            }

            ProducerFactory<String, byte[]> producerFactory = new DefaultKafkaProducerFactory<>(Map.of(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName(),
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName()));
            KafkaTemplate<String, byte[]> template = new KafkaTemplate<>(producerFactory);
            template.send(new ProducerRecord<>(originalTopic, record.partition(), record.key(), record.value()));
            return new ReplayResult(dltTopic, partition, offset, originalTopic);
        }
    }

    private static String headerValue(ConsumerRecord<String, byte[]> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        if (header == null) {
            return null;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }

    public record ReplayResult(String dltTopic, int partition, long offset, String republishedToTopic) {
    }
}
