DROP TABLE IF EXISTS links;

CREATE TABLE links
(
    id              BIGSERIAL       PRIMARY KEY,
    chat_id         BIGINT          NOT NULL,
    hosting_name    VARCHAR(50)     NOT NULL,
    raw_link        TEXT            NOT NULL,
    hosting_id      VARCHAR(100)    NOT NULL,
    title           TEXT            NOT NULL,
    views           BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (chat_id, hosting_name, hosting_id)
);

DROP TABLE IF EXISTS pagination_state;

CREATE TABLE pagination_state (
    chat_id             BIGINT      NOT NULL,
    message_id          BIGINT      NOT NULL,
    first_seen_id       BIGINT,
    last_seen_id        BIGINT,
    created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (chat_id, message_id)
);