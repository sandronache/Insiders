CREATE TABLE subreddits
(
    id              UUID         NOT NULL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    display_name    VARCHAR(100) NOT NULL,
    description     TEXT,
    icon_url        VARCHAR(200),
    created_at      TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL
);
