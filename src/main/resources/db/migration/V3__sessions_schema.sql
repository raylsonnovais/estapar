CREATE TABLE parking_sessions
(
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    license_plate     VARCHAR(8)     NOT NULL,
    state             VARCHAR(10)    NOT NULL,
    entry_time        DATETIME(3)    NOT NULL,
    spot_id           BIGINT         NULL,
    sector_id         VARCHAR(10)    NULL,
    pricing_multiplier DECIMAL(4, 2) NULL,
    exit_time         DATETIME(3)    NULL,
    total_charged     DECIMAL(10, 2) NULL,
    PRIMARY KEY (id),
    INDEX idx_sessions_plate_state (license_plate, state)
);

CREATE TABLE processed_events
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    license_plate   VARCHAR(8)  NOT NULL,
    event_type      VARCHAR(10) NOT NULL,
    event_timestamp DATETIME(3) NULL,
    processed_at    DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_processed_event (license_plate, event_type, event_timestamp)
);
