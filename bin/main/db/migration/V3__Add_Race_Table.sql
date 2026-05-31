-- Create the race table to store race information
CREATE TABLE f1_api.race
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    country VARCHAR(255) NOT NULL UNIQUE,
    race_start_date DATE NOT NULL,
    race_end_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Add an index for the race name for faster lookups
CREATE INDEX idx_race_name ON f1_api.race (name);

-- Add an index for the country for faster lookups
CREATE INDEX idx_race_country ON f1_api.race (country);
