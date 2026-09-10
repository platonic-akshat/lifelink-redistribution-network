package com.lifelink.need.config;

import com.lifelink.events.kafka.EventObjectMapper;
import com.lifelink.events.model.NeedEvent;
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
    public ProducerFactory<String, NeedEvent> needEventProducerFactory() {
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
                ProducerConfig.ACKS_CONFIG, "all",
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true
        );
        DefaultKafkaProducerFactory<String, NeedEvent> factory = new DefaultKafkaProducerFactory<>(props);
        factory.setValueSerializer(new JsonSerializer<>(EventObjectMapper.create()));
        return factory;
    }

    @Bean
    public KafkaTemplate<String, NeedEvent> needEventKafkaTemplate(
            ProducerFactory<String, NeedEvent> needEventProducerFactory) {
        return new KafkaTemplate<>(needEventProducerFactory);
    }
}
