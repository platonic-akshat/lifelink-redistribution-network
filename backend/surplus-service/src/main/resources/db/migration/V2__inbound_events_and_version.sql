CREATE TABLE inbound_event (
    event_id     UUID PRIMARY KEY,
    topic        VARCHAR(100) NOT NULL,
    processed_at TIMESTAMPTZ  NOT NULL
);

ALTER TABLE surplus ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
