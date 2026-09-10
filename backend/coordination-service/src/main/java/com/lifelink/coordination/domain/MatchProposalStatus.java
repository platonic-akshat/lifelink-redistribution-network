package com.lifelink.coordination.domain;

/**
 * Local review-workflow status for a MatchProposal, distinct from the
 * common-events {@code MatchDecision} enum (which only models the outcome
 * once a decision is made, not the pending state before one exists).
 */
public enum MatchProposalStatus {
    PENDING,
    APPROVED,
    REJECTED
}
