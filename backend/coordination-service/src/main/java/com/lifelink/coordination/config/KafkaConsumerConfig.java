package com.lifelink.coordination.config;

import com.lifelink.events.kafka.EventObjectMapper;
import com.lifelink.events.model.MatchProposedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, MatchProposedEvent> matchProposedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        JsonDeserializer<MatchProposedEvent> deserializer =
                new JsonDeserializer<>(MatchProposedEvent.class, EventObjectMapper.create());
        deserializer.addTrustedPackages("com.lifelink.events.model");
        deserializer.setRemoveTypeHeaders(false);
        // match.proposed is written by matching-service's Kafka Streams topology,
        // which by default copies the __TypeId__ header over from whichever
        // input record (NeedEvent or SurplusEvent) triggered the match. Ignoring
        // type headers forces deserialization to always use the type given
        // above instead of trusting that stale header.
        deserializer.ignoreTypeHeaders();
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "coordination-service",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MatchProposedEvent> matchProposedListenerFactory(
            ConsumerFactory<String, MatchProposedEvent> matchProposedConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, MatchProposedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(matchProposedConsumerFactory);
        return factory;
    }
}
