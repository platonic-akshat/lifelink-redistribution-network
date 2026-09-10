CREATE TABLE surplus (
    id              UUID PRIMARY KEY,
    category        VARCHAR(20)  NOT NULL,
    sub_type        VARCHAR(100) NOT NULL,
    quantity        DOUBLE PRECISION NOT NULL,
    unit            VARCHAR(20)  NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    geohash5        VARCHAR(12)  NOT NULL,
    expires_at      TIMESTAMPTZ  NOT NULL,
    provider_id     UUID         NOT NULL,
    provider_name   VARCHAR(200) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_surplus_geohash5 ON surplus (geohash5);
CREATE INDEX idx_surplus_status ON surplus (status);
CREATE INDEX idx_surplus_provider ON surplus (provider_id);
CREATE INDEX idx_surplus_expires_at ON surplus (expires_at);
