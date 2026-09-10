package com.lifelink.coordination.messaging;

import com.lifelink.coordination.config.KafkaTopics;
import com.lifelink.coordination.domain.MatchProposal;
import com.lifelink.events.model.MatchDecision;
import com.lifelink.events.model.MatchDecisionEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class MatchDecisionEventPublisher {

    private final KafkaTemplate<String, MatchDecisionEvent> kafkaTemplate;

    public MatchDecisionEventPublisher(KafkaTemplate<String, MatchDecisionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(MatchProposal proposal, MatchDecision decision) {
        MatchDecisionEvent event = new MatchDecisionEvent(
                UUID.randomUUID(),
                proposal.getMatchId(),
                proposal.getNeedId(),
                decision,
                proposal.getDecidedBy(),
                proposal.getReason(),
                Instant.now()
        );
        // Keyed by matchId so all decision/logistics events for one match land
        // on the same partition and are consumed in order.
        kafkaTemplate.send(KafkaTopics.MATCH_DECISIONS, proposal.getMatchId().toString(), event);
    }
}
