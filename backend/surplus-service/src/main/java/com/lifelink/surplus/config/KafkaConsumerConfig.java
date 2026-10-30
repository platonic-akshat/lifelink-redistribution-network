package com.lifelink.surplus.config;

import com.lifelink.events.kafka.EventObjectMapper;
import com.lifelink.events.model.LogisticsEvent;
import com.lifelink.events.model.MatchDecisionEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, MatchDecisionEvent> matchDecisionConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        JsonDeserializer<MatchDecisionEvent> deserializer =
                new JsonDeserializer<>(MatchDecisionEvent.class, EventObjectMapper.create());
        deserializer.addTrustedPackages("com.lifelink.events.model");
        deserializer.ignoreTypeHeaders();
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "surplus-service",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MatchDecisionEvent> matchDecisionListenerFactory(
            ConsumerFactory<String, MatchDecisionEvent> matchDecisionConsumerFactory,
            DefaultErrorHandler kafkaDeadLetterErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, MatchDecisionEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(matchDecisionConsumerFactory);
        factory.setCommonErrorHandler(kafkaDeadLetterErrorHandler);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, LogisticsEvent> logisticsConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        JsonDeserializer<LogisticsEvent> deserializer =
                new JsonDeserializer<>(LogisticsEvent.class, EventObjectMapper.create());
        deserializer.addTrustedPackages("com.lifelink.events.model");
        deserializer.ignoreTypeHeaders();
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "surplus-service",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LogisticsEvent> logisticsListenerFactory(
            ConsumerFactory<String, LogisticsEvent> logisticsConsumerFactory,
            DefaultErrorHandler kafkaDeadLetterErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, LogisticsEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(logisticsConsumerFactory);
        factory.setCommonErrorHandler(kafkaDeadLetterErrorHandler);
        return factory;
    }
}
