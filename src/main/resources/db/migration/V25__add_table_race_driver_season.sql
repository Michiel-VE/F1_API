-- V{next_version}__add_driver_race_result.sql

CREATE TABLE driver_race_result (
    id               UUID                     NOT NULL DEFAULT gen_random_uuid(),
    driver_id        UUID                     NOT NULL,
    race_id          UUID                     NOT NULL,
    season_id        UUID                     NOT NULL,
    points           NUMERIC(5, 2)            NOT NULL DEFAULT 0,
    status           VARCHAR(50),                      -- e.g. 'Finished', 'DNF', 'DSQ', 'DNS'
    laps_completed   INTEGER,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT pk_driver_race_result
        PRIMARY KEY (id),

    CONSTRAINT uq_driver_race_result_driver_race_season
        UNIQUE (driver_id, race_id, season_id),

    CONSTRAINT fk_driver_race_result_driver
        FOREIGN KEY (driver_id) REFERENCES driver (id) ON DELETE RESTRICT,

    CONSTRAINT fk_driver_race_result_race
        FOREIGN KEY (race_id)   REFERENCES race (id)   ON DELETE RESTRICT,

    CONSTRAINT fk_driver_race_result_season
        FOREIGN KEY (season_id) REFERENCES season (id) ON DELETE RESTRICT
);

-- Indexes for the most common query patterns
CREATE INDEX idx_drr_driver_season  ON driver_race_result (driver_id, season_id);
CREATE INDEX idx_drr_race_season    ON driver_race_result (race_id, season_id);
CREATE INDEX idx_drr_season         ON driver_race_result (season_id);