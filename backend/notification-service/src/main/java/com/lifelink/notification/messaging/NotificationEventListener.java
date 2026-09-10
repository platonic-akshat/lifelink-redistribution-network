package com.lifelink.notification.messaging;

import com.lifelink.events.model.LogisticsEvent;
import com.lifelink.events.model.MatchDecisionEvent;
import com.lifelink.events.model.MatchProposedEvent;
import com.lifelink.notification.config.KafkaTopics;
import com.lifelink.notification.notification.NotificationSender;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Fans out notifications for the three events notification-service consumes.
 * Stateless — no persistence, idempotency is left to Kafka consumer-group
 * offsets (see ARCHITECTURE.md §4.5).
 */
@Component
public class NotificationEventListener {

    private final NotificationSender notificationSender;

    public NotificationEventListener(NotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }

    @KafkaListener(topics = KafkaTopics.MATCH_PROPOSED, containerFactory = "matchProposedListenerFactory")
    public void onMatchProposed(MatchProposedEvent event) {
        // recipientId = needId is a stand-in: in production this would be
        // resolved to the need's requesterId via need-service.
        String subject = "Candidate match found for your need";
        String body = String.format(Locale.ROOT,
                "A candidate match (matchId=%s) has been proposed for your need, "
                        + "score=%.2f, distanceKm=%.1f, hoursToExpiry=%.1f. "
                        + "A coordinator will review it shortly.",
                event.matchId(), event.score(), event.distanceKm(), event.hoursToExpiry());
        notificationSender.send(event.needId(), subject, body);
    }

    @KafkaListener(topics = KafkaTopics.MATCH_DECISIONS, containerFactory = "matchDecisionListenerFactory")
    public void onMatchDecision(MatchDecisionEvent event) {
        // recipientId = needId is a stand-in: in production this would also
        // notify the surplus provider(s) once needId/surplusIds are resolved
        // to actual account contacts via need-service/surplus-service.
        String subject = switch (event.decision()) {
            case APPROVED -> "Your match was approved";
            case REJECTED -> "Your match was rejected";
        };
        String body = String.format(Locale.ROOT,
                "Match %s was %s by coordinator %s.%s",
                event.matchId(),
                event.decision().name().toLowerCase(Locale.ROOT),
                event.decidedBy(),
                event.reason() != null && !event.reason().isBlank() ? " Reason: " + event.reason() : "");
        notificationSender.send(event.needId(), subject, body);
    }

    @KafkaListener(topics = KafkaTopics.LOGISTICS_EVENTS, containerFactory = "logisticsListenerFactory")
    public void onLogisticsEvent(LogisticsEvent event) {
        // recipientId = matchId is a stand-in: in production this would be
        // resolved to both the requester and provider contacts for the match
        // via coordination-service.
        String subject = "Logistics update: " + event.status();
        String body = switch (event.status()) {
            case SCHEDULED -> "Pickup has been scheduled for match " + event.matchId() + ".";
            case PICKED_UP -> "Items for match " + event.matchId() + " have been picked up.";
            case DELIVERED -> "Items for match " + event.matchId() + " have been delivered.";
            case FAILED -> "Logistics for match " + event.matchId() + " failed.";
        };
        if (event.notes() != null && !event.notes().isBlank()) {
            body = body + " Notes: " + event.notes();
        }
        notificationSender.send(event.matchId(), subject, body);
    }
}
