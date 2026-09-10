package com.lifelink.coordination.web;

import com.lifelink.coordination.domain.Logistics;
import com.lifelink.events.model.LogisticsStatus;

import java.time.Instant;
import java.util.UUID;

public record LogisticsResponse(
        UUID matchId,
        LogisticsStatus status,
        String notes,
        Instant updatedAt
) {
    public static LogisticsResponse from(Logistics l) {
        return new LogisticsResponse(l.getMatchId(), l.getStatus(), l.getNotes(), l.getUpdatedAt());
    }
}
