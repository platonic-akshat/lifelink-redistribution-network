package com.lifelink.coordination.web;

import com.lifelink.coordination.domain.MatchProposal;
import com.lifelink.coordination.domain.MatchProposalStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MatchProposalResponse(
        UUID matchId,
        UUID needId,
        List<UUID> surplusIds,
        double score,
        double distanceKm,
        double hoursToExpiry,
        MatchProposalStatus status,
        UUID decidedBy,
        String reason,
        Instant proposedAt,
        Instant decidedAt
) {
    public static MatchProposalResponse from(MatchProposal p) {
        return new MatchProposalResponse(p.getMatchId(), p.getNeedId(), p.getSurplusIds(), p.getScore(),
                p.getDistanceKm(), p.getHoursToExpiry(), p.getStatus(), p.getDecidedBy(), p.getReason(),
                p.getProposedAt(), p.getDecidedAt());
    }
}
