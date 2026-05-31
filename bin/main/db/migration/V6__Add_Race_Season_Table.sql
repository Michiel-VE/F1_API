CREATE TABLE f1_api.race_season
(
    id         UUID PRIMARY KEY,
    race_id    UUID                     NOT NULL,
    season_id  UUID                     NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_race FOREIGN KEY (race_id) REFERENCES f1_api.race (id),
    CONSTRAINT fk_season FOREIGN KEY (season_id) REFERENCES f1_api.season (id),
    CONSTRAINT uq_race_season UNIQUE (race_id, season_id)
);
