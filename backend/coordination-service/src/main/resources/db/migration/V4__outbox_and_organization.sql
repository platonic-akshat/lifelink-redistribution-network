CREATE TABLE outbox_event (
    event_id         UUID PRIMARY KEY,
    aggregate_type   VARCHAR(50)  NOT NULL,
    aggregate_id     UUID         NOT NULL,
    event_type       VARCHAR(100) NOT NULL,
    topic            VARCHAR(100) NOT NULL,
    message_key      VARCHAR(200) NOT NULL,
    payload          TEXT         NOT NULL,
    schema_version   INT          NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    publish_attempts INT          NOT NULL DEFAULT 0,
    last_error       VARCHAR(2000),
    created_at       TIMESTAMPTZ  NOT NULL,
    published_at     TIMESTAMPTZ
);

CREATE INDEX idx_outbox_event_status_created ON outbox_event (status, created_at);

CREATE TABLE organization (
    id                   UUID PRIMARY KEY,
    name                 VARCHAR(200) NOT NULL,
    verification_status  VARCHAR(20)  NOT NULL,
    updated_at           TIMESTAMPTZ  NOT NULL
);

INSERT INTO organization (id, name, verification_status, updated_at) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Demo Provider Org', 'VERIFIED', NOW()),
    ('22222222-2222-2222-2222-222222222222', 'Demo NGO Org', 'VERIFIED', NOW());
