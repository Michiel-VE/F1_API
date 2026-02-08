ALTER TABLE f1_api.race DROP CONSTRAINT race_country_key;
ALTER TABLE f1_api.race ALTER COLUMN country TYPE TEXT;