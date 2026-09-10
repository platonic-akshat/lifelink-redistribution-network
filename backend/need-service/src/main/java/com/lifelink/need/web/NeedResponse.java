package com.lifelink.need.web;

import com.lifelink.events.model.Category;
import com.lifelink.events.model.NeedStatus;
import com.lifelink.events.model.Urgency;
import com.lifelink.need.domain.Need;

import java.time.Instant;
import java.util.UUID;

public record NeedResponse(
        UUID id,
        Category category,
        String subType,
        double quantity,
        String unit,
        Urgency urgency,
        double latitude,
        double longitude,
        UUID requesterId,
        String requesterName,
        NeedStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static NeedResponse from(Need n) {
        return new NeedResponse(n.getId(), n.getCategory(), n.getSubType(), n.getQuantity(), n.getUnit(),
                n.getUrgency(), n.getLatitude(), n.getLongitude(), n.getRequesterId(), n.getRequesterName(),
                n.getStatus(), n.getCreatedAt(), n.getUpdatedAt());
    }
}
