package com.lifelink.surplus.messaging;

import com.lifelink.events.model.LogisticsEvent;
import com.lifelink.events.model.LogisticsStatus;
import com.lifelink.events.model.MatchDecision;
import com.lifelink.events.model.MatchDecisionEvent;
import com.lifelink.surplus.config.KafkaTopics;
import com.lifelink.surplus.service.InboundEventService;
import com.lifelink.surplus.service.SurplusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class SurplusLifecycleListener {

    private static final Logger log = LoggerFactory.getLogger(SurplusLifecycleListener.class);

    private final InboundEventService inboundEventService;
    private final SurplusService surplusService;

    public SurplusLifecycleListener(InboundEventService inboundEventService, SurplusService surplusService) {
        this.inboundEventService = inboundEventService;
        this.surplusService = surplusService;
    }

    @KafkaListener(topics = KafkaTopics.MATCH_DECISIONS, containerFactory = "matchDecisionListenerFactory")
    @Transactional
    public void onMatchDecision(MatchDecisionEvent event) {
        if (!inboundEventService.markProcessed(event.eventId(), KafkaTopics.MATCH_DECISIONS)) {
            return;
        }
        if (event.decision() != MatchDecision.APPROVED) {
            return;
        }
        List<UUID> surplusIds = event.surplusIds() != null ? event.surplusIds() : List.of();
        for (UUID surplusId : surplusIds) {
            try {
                surplusService.applyMatchApproved(surplusId);
            } catch (IllegalStateException ex) {
                log.warn("Could not reserve surplus {} for match {}: {}", surplusId, event.matchId(), ex.getMessage());
            }
        }
    }

    @KafkaListener(topics = KafkaTopics.LOGISTICS_EVENTS, containerFactory = "logisticsListenerFactory")
    @Transactional
    public void onLogistics(LogisticsEvent event) {
        if (!inboundEventService.markProcessed(event.eventId(), KafkaTopics.LOGISTICS_EVENTS)) {
            return;
        }
        List<UUID> surplusIds = event.surplusIds() != null ? event.surplusIds() : List.of();
        for (UUID surplusId : surplusIds) {
            switch (event.status()) {
                case DELIVERED -> surplusService.applyLogisticsDelivered(surplusId);
                case FAILED -> surplusService.applyLogisticsFailed(surplusId);
                default -> {
                    // SCHEDULED / PICKED_UP do not change surplus inventory state
                }
            }
        }
    }
}
