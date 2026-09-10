CREATE TABLE need (
    id              UUID PRIMARY KEY,
    category        VARCHAR(20)  NOT NULL,
    sub_type        VARCHAR(100) NOT NULL,
    quantity        DOUBLE PRECISION NOT NULL,
    unit            VARCHAR(20)  NOT NULL,
    urgency         VARCHAR(20)  NOT NULL,
    latitude        DOUBLE PRECISION NOT NULL,
    longitude       DOUBLE PRECISION NOT NULL,
    geohash5        VARCHAR(12)  NOT NULL,
    requester_id    UUID         NOT NULL,
    requester_name  VARCHAR(200) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_need_geohash5 ON need (geohash5);
CREATE INDEX idx_need_status ON need (status);
CREATE INDEX idx_need_requester ON need (requester_id);
CREATE INDEX idx_need_urgency ON need (urgency);
