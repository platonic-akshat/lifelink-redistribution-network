CREATE TABLE match_proposal (
    match_id         UUID PRIMARY KEY,
    need_id          UUID NOT NULL,
    score            DOUBLE PRECISION NOT NULL,
    distance_km      DOUBLE PRECISION NOT NULL,
    hours_to_expiry  DOUBLE PRECISION NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    decided_by       UUID,
    reason           VARCHAR(500),
    proposed_at      TIMESTAMPTZ NOT NULL,
    decided_at       TIMESTAMPTZ
);

CREATE INDEX idx_match_proposal_status ON match_proposal (status);

-- Element-collection join table backing MatchProposal.surplusIds.
CREATE TABLE match_proposal_surplus_items (
    match_id    UUID NOT NULL REFERENCES match_proposal (match_id),
    surplus_id  UUID NOT NULL
);

CREATE INDEX idx_match_proposal_surplus_items_match_id ON match_proposal_surplus_items (match_id);

CREATE TABLE logistics (
    match_id    UUID PRIMARY KEY REFERENCES match_proposal (match_id),
    status      VARCHAR(20) NOT NULL,
    notes       VARCHAR(1000),
    updated_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_logistics_status ON logistics (status);
