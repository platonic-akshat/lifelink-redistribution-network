package com.lifelink.surplus.messaging;

import com.lifelink.events.geo.GeoPoint;
import com.lifelink.events.model.SurplusEvent;
import com.lifelink.surplus.config.KafkaTopics;
import com.lifelink.surplus.domain.Surplus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class SurplusEventPublisher {

    private final KafkaTemplate<String, SurplusEvent> kafkaTemplate;

    public SurplusEventPublisher(KafkaTemplate<String, SurplusEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Surplus surplus) {
        SurplusEvent event = new SurplusEvent(
                UUID.randomUUID(),
                surplus.getId(),
                surplus.getStatus(),
                surplus.getCategory(),
                surplus.getSubType(),
                surplus.getQuantity(),
                surplus.getUnit(),
                new GeoPoint(surplus.getLatitude(), surplus.getLongitude()),
                surplus.getGeohash5(),
                surplus.getExpiresAt(),
                surplus.getProviderId(),
                Instant.now()
        );
        // Keyed by surplusId so all events for one posting land on the same
        // partition and are consumed in order.
        kafkaTemplate.send(KafkaTopics.SURPLUS_EVENTS, surplus.getId().toString(), event);
    }
}
