package com.lifelink.coordination.config;

import com.lifelink.events.kafka.EventObjectMapper;
import com.lifelink.events.model.LogisticsEvent;
import com.lifelink.events.model.MatchDecisionEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, MatchDecisionEvent> matchDecisionEventProducerFactory() {
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true
        );
        DefaultKafkaProducerFactory<String, MatchDecisionEvent> factory = new DefaultKafkaProducerFactory<>(props);
        factory.setValueSerializer(new JsonSerializer<>(EventObjectMapper.create()));
        return factory;
    }

    @Bean
    public KafkaTemplate<String, MatchDecisionEvent> matchDecisionEventKafkaTemplate(
            ProducerFactory<String, MatchDecisionEvent> matchDecisionEventProducerFactory) {
        return new KafkaTemplate<>(matchDecisionEventProducerFactory);
    }

    @Bean
    public ProducerFactory<String, LogisticsEvent> logisticsEventProducerFactory() {
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true
        );
        DefaultKafkaProducerFactory<String, LogisticsEvent> factory = new DefaultKafkaProducerFactory<>(props);
        factory.setValueSerializer(new JsonSerializer<>(EventObjectMapper.create()));
        return factory;
    }

    @Bean
    public KafkaTemplate<String, LogisticsEvent> logisticsEventKafkaTemplate(
            ProducerFactory<String, LogisticsEvent> logisticsEventProducerFactory) {
        return new KafkaTemplate<>(logisticsEventProducerFactory);
    }
}
