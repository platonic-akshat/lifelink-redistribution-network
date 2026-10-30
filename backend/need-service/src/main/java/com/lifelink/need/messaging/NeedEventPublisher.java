package com.lifelink.need.messaging;

import com.lifelink.events.geo.GeoPoint;
import com.lifelink.events.model.NeedEvent;
import com.lifelink.need.config.KafkaTopics;
import com.lifelink.need.domain.Need;
import com.lifelink.outbox.OutboxService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class NeedEventPublisher {

    private final OutboxService outboxService;

    public NeedEventPublisher(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    public void publish(Need need) {
        UUID eventId = UUID.randomUUID();
        NeedEvent event = new NeedEvent(
                eventId,
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
        outboxService.enqueue(
                KafkaTopics.NEED_EVENTS,
                need.getId().toString(),
                event,
                eventId,
                NeedEvent.class.getSimpleName(),
                "Need",
                need.getId());
    }
}
