package com.lifelink.coordination.messaging;

import com.lifelink.coordination.config.KafkaTopics;
import com.lifelink.coordination.domain.MatchProposal;
import com.lifelink.events.model.MatchDecision;
import com.lifelink.events.model.MatchDecisionEvent;
import com.lifelink.outbox.OutboxService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class MatchDecisionEventPublisher {

    private final OutboxService outboxService;

    public MatchDecisionEventPublisher(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    public void publish(MatchProposal proposal, MatchDecision decision) {
        UUID eventId = UUID.randomUUID();
        MatchDecisionEvent event = new MatchDecisionEvent(
                eventId,
                proposal.getMatchId(),
                proposal.getNeedId(),
                proposal.getSurplusIds(),
                decision,
                proposal.getDecidedBy(),
                proposal.getReason(),
                Instant.now()
        );
        outboxService.enqueue(
                KafkaTopics.MATCH_DECISIONS,
                proposal.getMatchId().toString(),
                event,
                eventId,
                MatchDecisionEvent.class.getSimpleName(),
                "MatchProposal",
                proposal.getMatchId());
    }
}
