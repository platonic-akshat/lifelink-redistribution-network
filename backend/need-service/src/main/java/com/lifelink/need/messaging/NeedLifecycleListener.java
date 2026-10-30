package com.lifelink.need.messaging;

import com.lifelink.events.model.LogisticsEvent;
import com.lifelink.events.model.MatchDecision;
import com.lifelink.events.model.MatchDecisionEvent;
import com.lifelink.need.config.KafkaTopics;
import com.lifelink.need.service.InboundEventService;
import com.lifelink.need.service.NeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NeedLifecycleListener {

    private static final Logger log = LoggerFactory.getLogger(NeedLifecycleListener.class);

    private final InboundEventService inboundEventService;
    private final NeedService needService;

    public NeedLifecycleListener(InboundEventService inboundEventService, NeedService needService) {
        this.inboundEventService = inboundEventService;
        this.needService = needService;
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
        try {
            needService.applyMatchApproved(event.needId());
        } catch (IllegalStateException ex) {
            log.warn("Could not mark need {} matched for {}: {}", event.needId(), event.matchId(), ex.getMessage());
        }
    }

    @KafkaListener(topics = KafkaTopics.LOGISTICS_EVENTS, containerFactory = "logisticsListenerFactory")
    @Transactional
    public void onLogistics(LogisticsEvent event) {
        if (!inboundEventService.markProcessed(event.eventId(), KafkaTopics.LOGISTICS_EVENTS)) {
            return;
        }
        if (event.needId() == null) {
            return;
        }
        switch (event.status()) {
            case DELIVERED -> needService.applyLogisticsDelivered(event.needId());
            case FAILED -> needService.applyLogisticsFailed(event.needId());
            default -> {
            }
        }
    }
}
