package com.lifelink.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxRelay(OutboxEventRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${lifelink.outbox.relay-delay-ms:1000}")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> batch = repository.findPending(PageRequest.of(0, 50));
        for (OutboxEvent event : batch) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getMessageKey(), event.getPayload()).get();
                event.markPublished(Instant.now());
            } catch (Exception ex) {
                log.warn("Outbox publish failed for {}: {}", event.getEventId(), ex.getMessage());
                event.markFailedAttempt(ex.getMessage());
            }
        }
    }
}
