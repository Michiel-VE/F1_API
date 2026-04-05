-- 1. Ensure UUID extension is available
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Create the status lookup table
CREATE TABLE f1_api.race_status (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 3. Populate possible race events
INSERT INTO f1_api.race_status (name) VALUES 
('SCHEDULED'),
('COMPLETED'),
('DELAYED'),
('POSTPONED'),
('CANCELLED'),
('INTERRUPTED'),
('SHORTENED'),
('PROVISIONAL'),
('RECHEDULED');

-- 4. Add the new columns to the race table
ALTER TABLE f1_api.race 
    ADD COLUMN extra_info TEXT,
    ADD COLUMN status_id UUID;

-- 5. Update existing data based on race_end_date
-- If the race end date is before today, it's marked as COMPLETED
UPDATE f1_api.race
SET status_id = (SELECT id FROM f1_api.race_status WHERE name = 'COMPLETED')
WHERE race_end_date < CURRENT_DATE;

-- If the race end date is today or in the future, it's marked as SCHEDULED
UPDATE f1_api.race
SET status_id = (SELECT id FROM f1_api.race_status WHERE name = 'SCHEDULED')
WHERE race_end_date >= CURRENT_DATE;

-- 6. Constraints: Ensure every race has a status and link the foreign key
ALTER TABLE f1_api.race 
    ALTER COLUMN status_id SET NOT NULL;

ALTER TABLE f1_api.race
    ADD CONSTRAINT fk_race_status 
    FOREIGN KEY (status_id) 
    REFERENCES f1_api.race_status(id);