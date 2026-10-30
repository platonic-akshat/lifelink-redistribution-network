package com.lifelink.events.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Published by coordination-service to topic {@code logistics.events} as an
 * approved match's pickup/delivery moves through its lifecycle.
 */
public record LogisticsEvent(
        UUID eventId,
        UUID matchId,
        UUID needId,
        List<UUID> surplusIds,
        LogisticsStatus status,
        String notes,
        Instant occurredAt
) {
}
