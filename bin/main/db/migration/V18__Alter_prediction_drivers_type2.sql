-- Drop the constraint (if it exists) related to JSONB checks
ALTER TABLE f1_api.prediction
    DROP CONSTRAINT IF EXISTS prediction_predicted_drivers_check;

-- Now change the column type to TEXT (if not already done)
ALTER TABLE f1_api.prediction
    ALTER COLUMN predicted_drivers TYPE TEXT;
