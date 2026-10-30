package com.lifelink.events.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Published by coordination-service to topic {@code match.decisions} when a
 * coordinator approves or rejects a proposed match.
 */
public record MatchDecisionEvent(
        UUID eventId,
        UUID matchId,
        UUID needId,
        List<UUID> surplusIds,
        MatchDecision decision,
        UUID decidedBy,
        String reason,
        Instant occurredAt
) {
}
