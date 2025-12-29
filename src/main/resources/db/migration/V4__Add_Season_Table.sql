CREATE TABLE f1_api.season
(
    id          UUID PRIMARY KEY,
    season_name VARCHAR(255)             NOT NULL UNIQUE,
    created_at  timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at  timestamp with time zone NOT NULL DEFAULT NOW()
);
