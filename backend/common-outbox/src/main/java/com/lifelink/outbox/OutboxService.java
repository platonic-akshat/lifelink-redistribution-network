package com.lifelink.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifelink.events.kafka.EventObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OutboxService {

    private static final int SCHEMA_VERSION = 1;

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository repository) {
        this.repository = repository;
        this.objectMapper = EventObjectMapper.create();
    }

    @Transactional
    public void enqueue(String topic, String messageKey, Object payload, UUID eventId,
                        String eventType, String aggregateType, UUID aggregateId) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            repository.save(new OutboxEvent(
                    eventId,
                    aggregateType,
                    aggregateId,
                    eventType,
                    topic,
                    messageKey,
                    json,
                    SCHEMA_VERSION,
                    OutboxStatus.PENDING,
                    Instant.now()));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize outbox payload", ex);
        }
    }

    @Transactional
    public void requeue(UUID eventId) {
        OutboxEvent event = repository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Outbox event not found: " + eventId));
        event.requeue();
    }
}
