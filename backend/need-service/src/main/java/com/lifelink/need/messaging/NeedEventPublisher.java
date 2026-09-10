package com.lifelink.need.messaging;

import com.lifelink.events.geo.GeoPoint;
import com.lifelink.events.model.NeedEvent;
import com.lifelink.need.config.KafkaTopics;
import com.lifelink.need.domain.Need;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class NeedEventPublisher {

    private final KafkaTemplate<String, NeedEvent> kafkaTemplate;

    public NeedEventPublisher(KafkaTemplate<String, NeedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Need need) {
        NeedEvent event = new NeedEvent(
                UUID.randomUUID(),
                need.getId(),
                need.getStatus(),
                need.getCategory(),
                need.getSubType(),
                need.getQuantity(),
                need.getUnit(),
                need.getUrgency(),
                new GeoPoint(need.getLatitude(), need.getLongitude()),
                need.getGeohash5(),
                need.getRequesterId(),
                Instant.now()
        );
        kafkaTemplate.send(KafkaTopics.NEED_EVENTS, need.getId().toString(), event);
    }
}
