package com.lifelink.surplus.messaging;

import com.lifelink.events.geo.GeoPoint;
import com.lifelink.events.model.SurplusEvent;
import com.lifelink.outbox.OutboxService;
import com.lifelink.surplus.config.KafkaTopics;
import com.lifelink.surplus.domain.Surplus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class SurplusEventPublisher {

    private final OutboxService outboxService;

    public SurplusEventPublisher(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    public void publish(Surplus surplus) {
        UUID eventId = UUID.randomUUID();
        SurplusEvent event = new SurplusEvent(
                eventId,
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
        outboxService.enqueue(
                KafkaTopics.SURPLUS_EVENTS,
                surplus.getId().toString(),
                event,
                eventId,
                SurplusEvent.class.getSimpleName(),
                "Surplus",
                surplus.getId());
    }
}
