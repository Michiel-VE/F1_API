-- Add version column for optimistic locking to the 'user' table
ALTER TABLE f1_api.user ADD COLUMN version BIGINT;

-- Optional: Set default value for version column (default 0)
UPDATE f1_api.user SET version = 0;

-- Optional: Make the version column not nullable if desired
ALTER TABLE f1_api.user ALTER COLUMN version SET NOT NULL;

-- Ensure the version column is included in your entity mapping for optimistic locking
