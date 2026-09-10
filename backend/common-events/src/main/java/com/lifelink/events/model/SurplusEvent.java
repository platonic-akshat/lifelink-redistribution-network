package com.lifelink.events.model;

import com.lifelink.events.geo.GeoPoint;

import java.time.Instant;
import java.util.UUID;

/**
 * Published by surplus-service to topic {@code surplus.events} on every
 * state change of a surplus posting.
 */
public record SurplusEvent(
        UUID eventId,
        UUID surplusId,
        SurplusStatus status,
        Category category,
        String subType,
        double quantity,
        String unit,
        GeoPoint location,
        String geohash5,
        Instant expiresAt,
        UUID providerId,
        Instant occurredAt
) {
}
