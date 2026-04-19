DROP TABLE IF EXISTS links;

CREATE TABLE links
(
    id           BIGSERIAL PRIMARY KEY,
    hosting_name VARCHAR(50)  NOT NULL,
    raw_link     TEXT         NOT NULL,
    hosting_id   VARCHAR(100) NOT NULL,
    title        TEXT         NOT NULL,
    views        BIGINT       NOT NULL DEFAULT 0,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (hosting_name, hosting_id)
);