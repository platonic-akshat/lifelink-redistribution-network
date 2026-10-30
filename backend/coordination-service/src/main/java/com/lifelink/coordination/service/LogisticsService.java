package com.lifelink.coordination.service;

import com.lifelink.coordination.domain.Logistics;
import com.lifelink.coordination.domain.MatchProposal;
import com.lifelink.coordination.messaging.LogisticsEventPublisher;
import com.lifelink.coordination.repository.AllocatedSurplusRepository;
import com.lifelink.coordination.repository.LogisticsRepository;
import com.lifelink.coordination.repository.MatchProposalRepository;
import com.lifelink.events.model.LogisticsStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

@Service
public class LogisticsService {

    private final LogisticsRepository repository;
    private final MatchProposalRepository matchProposalRepository;
    private final AllocatedSurplusRepository allocatedSurplusRepository;
    private final LogisticsEventPublisher eventPublisher;

    public LogisticsService(LogisticsRepository repository,
                            MatchProposalRepository matchProposalRepository,
                            AllocatedSurplusRepository allocatedSurplusRepository,
                            LogisticsEventPublisher eventPublisher) {
        this.repository = repository;
        this.matchProposalRepository = matchProposalRepository;
        this.allocatedSurplusRepository = allocatedSurplusRepository;
        this.eventPublisher = eventPublisher;
    }

    public Logistics getOrThrow(UUID matchId) {
        return repository.findById(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Logistics not found for match: " + matchId));
    }

    public Page<Logistics> list(LogisticsStatus status, Pageable pageable) {
        if (status != null) {
            return repository.findByStatus(status, pageable);
        }
        return repository.findAllByOrderByUpdatedAtDesc(pageable);
    }

    @Transactional
    public Logistics markPickedUp(UUID matchId, String notes) {
        return updateStatus(matchId, LogisticsStatus.PICKED_UP, notes, false);
    }

    @Transactional
    public Logistics markDelivered(UUID matchId, String notes) {
        return updateStatus(matchId, LogisticsStatus.DELIVERED, notes, false);
    }

    @Transactional
    public Logistics markFailed(UUID matchId, String notes) {
        return updateStatus(matchId, LogisticsStatus.FAILED, notes, true);
    }

    private Logistics updateStatus(UUID matchId, LogisticsStatus status, String notes, boolean releaseAllocation) {
        Logistics logistics = getOrThrow(matchId);
        logistics.update(status, notes, Instant.now());
        if (releaseAllocation) {
            releaseAllocatedSurplus(matchId);
        }
        eventPublisher.publish(logistics);
        return logistics;
    }

    private void releaseAllocatedSurplus(UUID matchId) {
        MatchProposal proposal = matchProposalRepository.findById(matchId).orElse(null);
        if (proposal == null) {
            return;
        }
        for (UUID surplusId : proposal.getSurplusIds()) {
            allocatedSurplusRepository.deleteById(surplusId);
        }
    }
}
