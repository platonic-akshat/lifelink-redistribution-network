package com.lifelink.events.model;

import com.lifelink.events.geo.GeoPoint;

import java.time.Instant;
import java.util.UUID;

/**
 * Published by need-service to topic {@code need.events} on every state
 * change of a need posting.
 */
public record NeedEvent(
        UUID eventId,
        UUID needId,
        NeedStatus status,
        Category category,
        String subType,
        double quantity,
        String unit,
        Urgency urgency,
        GeoPoint location,
        String geohash5,
        UUID requesterId,
        Instant occurredAt
) {
}
