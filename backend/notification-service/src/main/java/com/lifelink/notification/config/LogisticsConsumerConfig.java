package com.lifelink.notification.config;

import com.lifelink.events.kafka.EventObjectMapper;
import com.lifelink.events.model.LogisticsEvent;
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
public class LogisticsConsumerConfig {

    @Bean
    public ConsumerFactory<String, LogisticsEvent> logisticsConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        JsonDeserializer<LogisticsEvent> deserializer =
                new JsonDeserializer<>(LogisticsEvent.class, EventObjectMapper.create());
        deserializer.addTrustedPackages("com.lifelink.events.model");
        deserializer.setRemoveTypeHeaders(false);
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "notification-service",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LogisticsEvent> logisticsListenerFactory(
            ConsumerFactory<String, LogisticsEvent> logisticsConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, LogisticsEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(logisticsConsumerFactory);
        return factory;
    }
}
