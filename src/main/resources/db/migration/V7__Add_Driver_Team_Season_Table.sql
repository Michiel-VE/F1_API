CREATE TABLE f1_api.driver_team_season
(
    id         UUID PRIMARY KEY,
    driver_id  UUID                     NOT NULL,
    team_id    UUID                     NOT NULL,
    season_id  UUID                     NOT NULL,
    points     INTEGER                  NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT NOW(),
    updated_at timestamp with time zone NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_driver FOREIGN KEY (driver_id) REFERENCES f1_api.driver (id),
    CONSTRAINT fk_team FOREIGN KEY (team_id) REFERENCES f1_api.team (id),
    CONSTRAINT fk_season FOREIGN KEY (season_id) REFERENCES f1_api.season (id),
    CONSTRAINT uq_driver_team_season UNIQUE (driver_id, team_id, season_id)
);
