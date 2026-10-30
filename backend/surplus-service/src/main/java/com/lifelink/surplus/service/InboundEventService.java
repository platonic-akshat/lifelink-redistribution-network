package com.lifelink.surplus.service;

import com.lifelink.surplus.domain.InboundEvent;
import com.lifelink.surplus.repository.InboundEventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class InboundEventService {

    private final InboundEventRepository repository;

    public InboundEventService(InboundEventRepository repository) {
        this.repository = repository;
    }

    /**
     * @return true if this is the first time we've seen {@code eventId}
     */
    @Transactional
    public boolean markProcessed(UUID eventId, String topic) {
        try {
            repository.save(new InboundEvent(eventId, topic, Instant.now()));
            return true;
        } catch (DataIntegrityViolationException duplicate) {
            return false;
        }
    }
}
