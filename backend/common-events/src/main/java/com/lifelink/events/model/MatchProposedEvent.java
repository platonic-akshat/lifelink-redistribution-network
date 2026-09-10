package com.lifelink.events.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Published by matching-service to topic {@code match.proposed} when it
 * finds a candidate surplus-to-need pairing. Never auto-committed — a
 * coordinator in coordination-service must approve or reject it.
 */
public record MatchProposedEvent(
        UUID eventId,
        UUID matchId,
        UUID needId,
        List<UUID> surplusIds,
        double score,
        double distanceKm,
        double hoursToExpiry,
        Instant occurredAt
) {
}
