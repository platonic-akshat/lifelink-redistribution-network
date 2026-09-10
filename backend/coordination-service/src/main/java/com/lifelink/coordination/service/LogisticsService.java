package com.lifelink.coordination.service;

import com.lifelink.coordination.domain.Logistics;
import com.lifelink.coordination.messaging.LogisticsEventPublisher;
import com.lifelink.coordination.repository.LogisticsRepository;
import com.lifelink.events.model.LogisticsStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class LogisticsService {

    private final LogisticsRepository repository;
    private final LogisticsEventPublisher eventPublisher;

    public LogisticsService(LogisticsRepository repository, LogisticsEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    public Logistics getOrThrow(UUID matchId) {
        return repository.findById(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Logistics not found for match: " + matchId));
    }

    @Transactional
    public Logistics markPickedUp(UUID matchId, String notes) {
        return updateStatus(matchId, LogisticsStatus.PICKED_UP, notes);
    }

    @Transactional
    public Logistics markDelivered(UUID matchId, String notes) {
        return updateStatus(matchId, LogisticsStatus.DELIVERED, notes);
    }

    @Transactional
    public Logistics markFailed(UUID matchId, String notes) {
        return updateStatus(matchId, LogisticsStatus.FAILED, notes);
    }

    private Logistics updateStatus(UUID matchId, LogisticsStatus status, String notes) {
        Logistics logistics = getOrThrow(matchId);
        logistics.update(status, notes, Instant.now());
        eventPublisher.publish(logistics);
        return logistics;
    }
}
