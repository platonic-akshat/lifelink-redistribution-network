package com.lifelink.notification.config;

import com.lifelink.events.kafka.EventObjectMapper;
import com.lifelink.events.model.MatchDecisionEvent;
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
public class MatchDecisionConsumerConfig {

    @Bean
    public ConsumerFactory<String, MatchDecisionEvent> matchDecisionConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        JsonDeserializer<MatchDecisionEvent> deserializer =
                new JsonDeserializer<>(MatchDecisionEvent.class, EventObjectMapper.create());
        deserializer.addTrustedPackages("com.lifelink.events.model");
        deserializer.setRemoveTypeHeaders(false);
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "notification-service",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MatchDecisionEvent> matchDecisionListenerFactory(
            ConsumerFactory<String, MatchDecisionEvent> matchDecisionConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, MatchDecisionEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(matchDecisionConsumerFactory);
        return factory;
    }
}
