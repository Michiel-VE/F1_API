-- This migration script drops the 'user_profile' and 'user' tables.

-- Drop the 'user_profile' table first to avoid foreign key constraint issues,
-- as it references the 'user' table.
DROP TABLE IF EXISTS f1_api.user_profile CASCADE;

-- Now, drop the 'user' table.
DROP TABLE IF EXISTS f1_api.user CASCADE;