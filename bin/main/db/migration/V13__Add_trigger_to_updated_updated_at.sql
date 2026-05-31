-- 1. Create a function that updates the updated_at column
CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Apply the trigger to each table

-- team
DROP TRIGGER IF EXISTS trg_set_updated_at_team ON f1_api.team;
CREATE TRIGGER trg_set_updated_at_team
    BEFORE UPDATE ON f1_api.team
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- race_season
DROP TRIGGER IF EXISTS trg_set_updated_at_race_season ON f1_api.race_season;
CREATE TRIGGER trg_set_updated_at_race_season
    BEFORE UPDATE ON f1_api.race_season
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- season
DROP TRIGGER IF EXISTS trg_set_updated_at_season ON f1_api.season;
CREATE TRIGGER trg_set_updated_at_season
    BEFORE UPDATE ON f1_api.season
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- driver_team_season
DROP TRIGGER IF EXISTS trg_set_updated_at_driver_team_season ON f1_api.driver_team_season;
CREATE TRIGGER trg_set_updated_at_driver_team_season
    BEFORE UPDATE ON f1_api.driver_team_season
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- driver
DROP TRIGGER IF EXISTS trg_set_updated_at_driver ON f1_api.driver;
CREATE TRIGGER trg_set_updated_at_driver
    BEFORE UPDATE ON f1_api.driver
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- race
DROP TRIGGER IF EXISTS trg_set_updated_at_race ON f1_api.race;
CREATE TRIGGER trg_set_updated_at_race
    BEFORE UPDATE ON f1_api.race
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_set_updated_at_user ON f1_api.users;
CREATE TRIGGER trg_set_updated_at_user
    BEFORE UPDATE ON f1_api.users
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
