CREATE TABLE sectors
(
    id           VARCHAR(10)    NOT NULL,
    base_price   DECIMAL(10, 2) NOT NULL,
    max_capacity INT            NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE spots
(
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    sector_id   VARCHAR(10) NOT NULL,
    lat         DOUBLE      NOT NULL,
    lng         DOUBLE      NOT NULL,
    is_occupied BOOLEAN     NOT NULL DEFAULT FALSE,
    version     INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_spots_location (lat, lng),
    CONSTRAINT fk_spots_sector FOREIGN KEY (sector_id) REFERENCES sectors (id)
);
