-- First, add columns as nullable
ALTER TABLE animals ADD COLUMN IF NOT EXISTS required_food int4;
ALTER TABLE games ADD COLUMN IF NOT EXISTS is_game_over boolean;

-- Set default values for existing records
UPDATE animals SET required_food = 1 WHERE required_food IS NULL;
UPDATE games SET is_game_over = false WHERE is_game_over IS NULL;

-- Make columns NOT NULL after setting default values
ALTER TABLE animals ALTER COLUMN required_food SET NOT NULL;
ALTER TABLE games ALTER COLUMN is_game_over SET NOT NULL;

-- Clean up invalid foreign key references
DELETE FROM animals WHERE player_id NOT IN (SELECT id FROM players);
DELETE FROM moves WHERE player_id NOT IN (SELECT id FROM players);

-- Add foreign key constraints
ALTER TABLE animals ADD CONSTRAINT FK4tdh7uopvicam38vbk32i2o2y 
    FOREIGN KEY (player_id) REFERENCES players(id);

ALTER TABLE moves ADD CONSTRAINT FK45c3rvgvnemff12dy4d0u9xma 
    FOREIGN KEY (player_id) REFERENCES players(id); 