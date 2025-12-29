CREATE TABLE f1_api.team
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255)             NOT NULL UNIQUE,
    short_name VARCHAR(255)             NOT NULL UNIQUE,
    country    VARCHAR(255)             NOT NULL,
    base       VARCHAR(255)             NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NOT NULL DEFAULT NOW()
);
