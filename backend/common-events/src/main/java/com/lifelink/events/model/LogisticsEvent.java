package com.lifelink.events.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Published by coordination-service to topic {@code logistics.events} as an
 * approved match's pickup/delivery moves through its lifecycle.
 */
public record LogisticsEvent(
        UUID eventId,
        UUID matchId,
        LogisticsStatus status,
        String notes,
        Instant occurredAt
) {
}
