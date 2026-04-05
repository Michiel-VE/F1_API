UPDATE f1_api.race_status 
SET name = 'RESCHEDULED' 
WHERE name = 'RECHEDULED';

INSERT INTO f1_api.race_status (name)
SELECT 'RESCHEDULED'
WHERE NOT EXISTS (SELECT 1 FROM f1_api.race_status WHERE name = 'RESCHEDULED');