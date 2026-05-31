DROP TABLE IF EXISTS f1_api.season_prediction CASCADE;

CREATE TABLE f1_api.pools (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    invite_code VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE f1_api.pool_members (
    pool_id UUID NOT NULL,
    user_id UUID NOT NULL,
    PRIMARY KEY (pool_id, user_id),
    CONSTRAINT fk_pool_members_pool FOREIGN KEY (pool_id) REFERENCES f1_api.pools(id) ON DELETE CASCADE,
    CONSTRAINT fk_pool_members_user FOREIGN KEY (user_id) REFERENCES f1_api.users(id) ON DELETE CASCADE
);

CREATE TABLE f1_api.team_predictions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    pool_id UUID NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_predictions_user FOREIGN KEY (user_id) REFERENCES f1_api.users(id) ON DELETE CASCADE,
    CONSTRAINT fk_predictions_pool FOREIGN KEY (pool_id) REFERENCES f1_api.pools(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_pool_prediction UNIQUE (user_id, pool_id)
);

CREATE TABLE f1_api.team_prediction_teams (
    prediction_id UUID NOT NULL,
    team_id UUID NOT NULL,
    prediction_order INT NOT NULL,
    PRIMARY KEY (prediction_id, prediction_order),
    CONSTRAINT fk_pred_teams_prediction FOREIGN KEY (prediction_id) REFERENCES f1_api.team_predictions(id) ON DELETE CASCADE,
    CONSTRAINT fk_pred_teams_team FOREIGN KEY (team_id) REFERENCES f1_api.team(id) ON DELETE CASCADE
);