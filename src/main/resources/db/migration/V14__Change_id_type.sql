-- Add a new column for UUID
ALTER TABLE f1_api.users ADD COLUMN id_new UUID DEFAULT gen_random_uuid();

-- Set the new `id_new` column as the primary key
ALTER TABLE f1_api.users DROP CONSTRAINT users_pkey;  -- Drop the existing primary key constraint
ALTER TABLE f1_api.users ADD PRIMARY KEY (id_new);    -- Add primary key on the new UUID column

-- Update the foreign key constraints if necessary, e.g., on other tables
-- (Optional step depending on your schema and relationships)

-- Drop the old `id` column (after ensuring no data loss)
ALTER TABLE f1_api.users DROP COLUMN id;

-- Rename the new UUID column to `id`
ALTER TABLE f1_api.users RENAME COLUMN id_new TO id;

-- Recreate the indexes if necessary
-- (The old indexes may still work, but it might be a good idea to recreate them for consistency)

CREATE INDEX IF NOT EXISTS idx_users_email ON f1_api.users (email);
CREATE INDEX IF NOT EXISTS idx_users_name ON f1_api.users (name);

-- Optionally, you can add any other constraints if needed
-- For example, if you want to ensure the UUID is unique in other contexts:
-- CREATE UNIQUE INDEX idx_users_id ON users (id);
