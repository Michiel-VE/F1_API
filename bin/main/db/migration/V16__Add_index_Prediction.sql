-- Step 1: Drop incorrect foreign key constraint
ALTER TABLE f1_api.prediction
    DROP CONSTRAINT IF EXISTS fk_user;

-- Step 2: Add correct foreign key constraint
ALTER TABLE f1_api.prediction
    ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES f1_api.users (id) ON DELETE CASCADE;

-- Step 3: Add index on race_id
CREATE INDEX IF NOT EXISTS idx_prediction_race_id
    ON f1_api.prediction (race_id);

-- Step 4: Add index on user_id
CREATE INDEX IF NOT EXISTS idx_prediction_user_id
    ON f1_api.prediction (user_id);

-- Step 5: Add GIN index on predicted_drivers for JSONB querying
CREATE INDEX IF NOT EXISTS idx_prediction_drivers_jsonb
    ON f1_api.prediction USING GIN (predicted_drivers);
