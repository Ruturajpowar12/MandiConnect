-- MandiConnect schema for XAMPP / MariaDB / MySQL 8.
-- The admin account is NOT seeded here. UserService.ensureAdmin() creates it at startup.

CREATE DATABASE IF NOT EXISTS mandiconnect
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE mandiconnect;

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(120)  NOT NULL,
    email       VARCHAR(180)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    phone       VARCHAR(30),
    location    VARCHAR(120),
    role        VARCHAR(20)   NOT NULL,
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS lots (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    lot_code        VARCHAR(32)   NOT NULL UNIQUE,
    crop_name       VARCHAR(120)  NOT NULL,
    quantity        DOUBLE        NOT NULL,
    unit            VARCHAR(20)   NOT NULL,
    quality_grade   VARCHAR(10)   NOT NULL,
    location        VARCHAR(120),
    expected_price  DOUBLE        NOT NULL,
    description     TEXT,
    farmer_id       BIGINT        NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'AVAILABLE',
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lot_farmer FOREIGN KEY (farmer_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bids (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    lot_id      BIGINT        NOT NULL,
    trader_id   BIGINT        NOT NULL,
    amount      DOUBLE        NOT NULL,
    status      VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    bid_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bid_lot FOREIGN KEY (lot_id) REFERENCES lots(id) ON DELETE CASCADE,
    CONSTRAINT fk_bid_trader FOREIGN KEY (trader_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS auctions (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    lot_id          BIGINT        NOT NULL UNIQUE,
    start_time      DATETIME      NOT NULL,
    end_time        DATETIME      NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    winner_bid_id   BIGINT        NULL,
    CONSTRAINT fk_auction_lot FOREIGN KEY (lot_id) REFERENCES lots(id) ON DELETE CASCADE
) ENGINE=InnoDB;
