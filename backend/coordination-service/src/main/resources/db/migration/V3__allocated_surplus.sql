CREATE TABLE allocated_surplus (
    surplus_id UUID PRIMARY KEY,
    match_id   UUID NOT NULL REFERENCES match_proposal (match_id)
);

CREATE INDEX idx_allocated_surplus_match_id ON allocated_surplus (match_id);
