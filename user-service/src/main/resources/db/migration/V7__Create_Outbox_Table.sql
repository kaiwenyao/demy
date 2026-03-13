CREATE TABLE outbox (
    id            BIGINT        NOT NULL PRIMARY KEY,
    exchange      VARCHAR(100)  NOT NULL,
    routing_key   VARCHAR(100)  NOT NULL,
    payload       TEXT          NOT NULL,
    status        VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    retry_count   INT           NOT NULL DEFAULT 0,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at       TIMESTAMP     NULL,
    last_retry_at TIMESTAMP     NULL
);

