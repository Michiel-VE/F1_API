CREATE TABLE f1_api.season_prediction (
    id UUID PRIMARY KEY NOT NULL,
    user_id UUID NOT NULL,
    predicted_teams TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES f1_api.users(id) ON DELETE CASCADE
);

-- Index for faster lookups by user
CREATE INDEX idx_season_prediction_user_id ON f1_api.season_prediction(user_id);