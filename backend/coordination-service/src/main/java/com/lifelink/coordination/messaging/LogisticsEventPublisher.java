package com.lifelink.coordination.messaging;

import com.lifelink.coordination.config.KafkaTopics;
import com.lifelink.coordination.domain.Logistics;
import com.lifelink.coordination.domain.MatchProposal;
import com.lifelink.coordination.repository.MatchProposalRepository;
import com.lifelink.events.model.LogisticsEvent;
import com.lifelink.outbox.OutboxService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class LogisticsEventPublisher {

    private final OutboxService outboxService;
    private final MatchProposalRepository proposalRepository;

    public LogisticsEventPublisher(OutboxService outboxService,
                                    MatchProposalRepository proposalRepository) {
        this.outboxService = outboxService;
        this.proposalRepository = proposalRepository;
    }

    public void publish(Logistics logistics) {
        MatchProposal proposal = proposalRepository.findById(logistics.getMatchId())
                .orElseThrow(() -> new IllegalStateException("Missing match proposal for logistics " + logistics.getMatchId()));
        UUID eventId = UUID.randomUUID();
        LogisticsEvent event = new LogisticsEvent(
                eventId,
                logistics.getMatchId(),
                proposal.getNeedId(),
                proposal.getSurplusIds(),
                logistics.getStatus(),
                logistics.getNotes(),
                Instant.now()
        );
        outboxService.enqueue(
                KafkaTopics.LOGISTICS_EVENTS,
                logistics.getMatchId().toString(),
                event,
                eventId,
                LogisticsEvent.class.getSimpleName(),
                "Logistics",
                logistics.getMatchId());
    }
}
