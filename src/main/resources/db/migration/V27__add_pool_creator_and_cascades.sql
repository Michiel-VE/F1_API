-- 1. Clear out pre-existing test records to avoid NOT NULL violations on the new column
DELETE FROM f1_api.pool_members;
DELETE FROM f1_api.team_prediction_teams;
DELETE FROM f1_api.team_predictions;
DELETE FROM f1_api.pools;

-- 2. Add creator column safely now that the table is clear
ALTER TABLE f1_api.pools 
ADD COLUMN creator_id UUID NOT NULL,
ADD CONSTRAINT fk_pools_creator FOREIGN KEY (creator_id) REFERENCES f1_api.users(id) ON DELETE CASCADE;