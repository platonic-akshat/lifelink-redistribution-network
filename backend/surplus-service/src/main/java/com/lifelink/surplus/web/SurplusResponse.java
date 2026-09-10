package com.lifelink.surplus.web;

import com.lifelink.events.model.Category;
import com.lifelink.events.model.SurplusStatus;
import com.lifelink.surplus.domain.Surplus;

import java.time.Instant;
import java.util.UUID;

public record SurplusResponse(
        UUID id,
        Category category,
        String subType,
        double quantity,
        String unit,
        double latitude,
        double longitude,
        Instant expiresAt,
        UUID providerId,
        String providerName,
        SurplusStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static SurplusResponse from(Surplus s) {
        return new SurplusResponse(s.getId(), s.getCategory(), s.getSubType(), s.getQuantity(), s.getUnit(),
                s.getLatitude(), s.getLongitude(), s.getExpiresAt(), s.getProviderId(), s.getProviderName(),
                s.getStatus(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
