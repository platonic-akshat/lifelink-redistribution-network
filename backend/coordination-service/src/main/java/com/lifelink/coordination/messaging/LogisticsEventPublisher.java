package com.lifelink.coordination.messaging;

import com.lifelink.coordination.config.KafkaTopics;
import com.lifelink.coordination.domain.Logistics;
import com.lifelink.events.model.LogisticsEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class LogisticsEventPublisher {

    private final KafkaTemplate<String, LogisticsEvent> kafkaTemplate;

    public LogisticsEventPublisher(KafkaTemplate<String, LogisticsEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Logistics logistics) {
        LogisticsEvent event = new LogisticsEvent(
                UUID.randomUUID(),
                logistics.getMatchId(),
                logistics.getStatus(),
                logistics.getNotes(),
                Instant.now()
        );
        // Keyed by matchId so all events for one match's logistics lifecycle
        // land on the same partition and are consumed in order.
        kafkaTemplate.send(KafkaTopics.LOGISTICS_EVENTS, logistics.getMatchId().toString(), event);
    }
}
