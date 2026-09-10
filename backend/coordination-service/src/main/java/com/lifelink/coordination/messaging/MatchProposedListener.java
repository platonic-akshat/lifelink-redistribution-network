package com.lifelink.coordination.messaging;

import com.lifelink.coordination.domain.MatchProposal;
import com.lifelink.coordination.domain.MatchProposalStatus;
import com.lifelink.coordination.repository.MatchProposalRepository;
import com.lifelink.events.model.MatchProposedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Consumes matching-service's proposed matches and persists them for
 * coordinator review. Upserts by matchId: a re-delivered/refreshed proposal
 * for a matchId that's already known just updates the scoring fields rather
 * than failing on the duplicate primary key.
 */
@Component
public class MatchProposedListener {

    private final MatchProposalRepository repository;

    public MatchProposedListener(MatchProposalRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "match.proposed", containerFactory = "matchProposedListenerFactory")
    @Transactional
    public void onMatchProposed(MatchProposedEvent event) {
        repository.findById(event.matchId()).ifPresentOrElse(
                existing -> existing.refresh(event.score(), event.distanceKm(), event.hoursToExpiry()),
                () -> repository.save(new MatchProposal(
                        event.matchId(),
                        event.needId(),
                        event.surplusIds(),
                        event.score(),
                        event.distanceKm(),
                        event.hoursToExpiry(),
                        MatchProposalStatus.PENDING,
                        event.occurredAt() != null ? event.occurredAt() : Instant.now()
                ))
        );
    }
}
