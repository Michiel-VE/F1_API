-- Create the driver table to store driver information
CREATE TABLE f1_api.driver
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    permanent_number INTEGER NOT NULL UNIQUE,
    driver_code VARCHAR(3) NOT NULL UNIQUE,
    birthday DATE NOT NULL,
    country VARCHAR(255) NOT NULL,
    country_code VARCHAR(3) NOT NULL,
    team VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Index for faster lookups on permanent_number and driver_code
CREATE INDEX idx_driver_permanent_number ON f1_api.driver (permanent_number);
CREATE INDEX idx_driver_driver_code ON f1_api.driver (driver_code);
