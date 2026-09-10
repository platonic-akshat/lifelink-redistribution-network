package com.lifelink.coordination.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "match_proposal", indexes = {
        @Index(name = "idx_match_proposal_status", columnList = "status")
})
public class MatchProposal {

    @Id
    @Column(name = "match_id")
    private UUID matchId;

    @Column(name = "need_id", nullable = false)
    private UUID needId;

    // Stored as a separate element-collection join table (rather than a
    // comma-joined VARCHAR) so surplusIds stay individually queryable/typed.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "match_proposal_surplus_items", joinColumns = @JoinColumn(name = "match_id"))
    @Column(name = "surplus_id", nullable = false)
    private List<UUID> surplusIds = new ArrayList<>();

    @Column(nullable = false)
    private double score;

    @Column(name = "distance_km", nullable = false)
    private double distanceKm;

    @Column(name = "hours_to_expiry", nullable = false)
    private double hoursToExpiry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchProposalStatus status;

    @Column(name = "decided_by")
    private UUID decidedBy;

    @Column(length = 500)
    private String reason;

    @Column(name = "proposed_at", nullable = false)
    private Instant proposedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    protected MatchProposal() {
        // JPA
    }

    public MatchProposal(UUID matchId, UUID needId, List<UUID> surplusIds, double score, double distanceKm,
                          double hoursToExpiry, MatchProposalStatus status, Instant proposedAt) {
        this.matchId = matchId;
        this.needId = needId;
        this.surplusIds = new ArrayList<>(surplusIds);
        this.score = score;
        this.distanceKm = distanceKm;
        this.hoursToExpiry = hoursToExpiry;
        this.status = status;
        this.proposedAt = proposedAt;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public UUID getNeedId() {
        return needId;
    }

    public List<UUID> getSurplusIds() {
        return surplusIds;
    }

    public double getScore() {
        return score;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getHoursToExpiry() {
        return hoursToExpiry;
    }

    public MatchProposalStatus getStatus() {
        return status;
    }

    public UUID getDecidedBy() {
        return decidedBy;
    }

    public String getReason() {
        return reason;
    }

    public Instant getProposedAt() {
        return proposedAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void refresh(double score, double distanceKm, double hoursToExpiry) {
        this.score = score;
        this.distanceKm = distanceKm;
        this.hoursToExpiry = hoursToExpiry;
    }

    public void approve(UUID decidedBy, Instant decidedAt) {
        this.status = MatchProposalStatus.APPROVED;
        this.decidedBy = decidedBy;
        this.decidedAt = decidedAt;
    }

    public void reject(UUID decidedBy, String reason, Instant decidedAt) {
        this.status = MatchProposalStatus.REJECTED;
        this.decidedBy = decidedBy;
        this.reason = reason;
        this.decidedAt = decidedAt;
    }
}
