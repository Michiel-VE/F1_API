CREATE TABLE f1_api.prediction
(
    id                UUID PRIMARY KEY                     DEFAULT gen_random_uuid(),

    user_id           UUID                        NOT NULL,
    race_id           UUID                        NOT NULL,

    predicted_drivers JSONB                       NOT NULL CHECK (
                jsonb_typeof(predicted_drivers) = 'array' AND
                jsonb_array_length(predicted_drivers) = 10
        ),

    points            INTEGER,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES f1_api.race (id) ON DELETE CASCADE,
    CONSTRAINT fk_race FOREIGN KEY (race_id) REFERENCES f1_api.race (id) ON DELETE CASCADE,
    CONSTRAINT unique_user_race UNIQUE (user_id, race_id)
);
