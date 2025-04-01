-- Create players table
CREATE TABLE players (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    game_id VARCHAR(255),
    score INTEGER NOT NULL DEFAULT 0,
    has_passed_development BOOLEAN NOT NULL DEFAULT false,
    has_passed_feeding BOOLEAN NOT NULL DEFAULT false,
    food_tokens INTEGER NOT NULL DEFAULT 0
);

-- Create games table
CREATE TABLE games (
    id VARCHAR(255) PRIMARY KEY,
    current_phase VARCHAR(50) NOT NULL,
    current_player_id BIGINT,
    is_game_over BOOLEAN NOT NULL DEFAULT false,
    active BOOLEAN NOT NULL DEFAULT true,
    food_tokens INTEGER NOT NULL DEFAULT 0,
    blue_food_tokens INTEGER NOT NULL DEFAULT 0,
    yellow_food_tokens INTEGER NOT NULL DEFAULT 0,
    current_player_index INTEGER NOT NULL DEFAULT 0,
    round INTEGER NOT NULL DEFAULT 0
);

-- Create cards table
CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    property VARCHAR(50),
    additional_points INTEGER NOT NULL DEFAULT 0,
    is_paired BOOLEAN NOT NULL DEFAULT false,
    description TEXT,
    player_id BIGINT,
    game_id VARCHAR(255),
    FOREIGN KEY (player_id) REFERENCES players(id),
    FOREIGN KEY (game_id) REFERENCES games(id)
);

-- Create animals table
CREATE TABLE animals (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    food_count INT NOT NULL DEFAULT 0,
    required_food INT NOT NULL DEFAULT 1,
    is_fed BOOLEAN NOT NULL DEFAULT false,
    active BOOLEAN NOT NULL DEFAULT true,
    has_used_fat_reserve BOOLEAN NOT NULL DEFAULT false,
    has_used_mimicry BOOLEAN NOT NULL DEFAULT false,
    has_used_tail_drop BOOLEAN NOT NULL DEFAULT false,
    has_used_piracy BOOLEAN NOT NULL DEFAULT false,
    has_used_symbiosis BOOLEAN NOT NULL DEFAULT false,
    has_used_cooperation BOOLEAN NOT NULL DEFAULT false,
    has_used_interaction BOOLEAN NOT NULL DEFAULT false,
    is_hibernating BOOLEAN NOT NULL DEFAULT false,
    can_be_attacked BOOLEAN NOT NULL DEFAULT true,
    last_attacked_animal_id BIGINT,
    player_id BIGINT,
    FOREIGN KEY (player_id) REFERENCES players(id),
    FOREIGN KEY (last_attacked_animal_id) REFERENCES animals(id)
);

-- Create animal_properties table (ManyToMany relationship between Animal and Card)
CREATE TABLE animal_properties (
    animal_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    PRIMARY KEY (animal_id, card_id),
    FOREIGN KEY (animal_id) REFERENCES animals(id),
    FOREIGN KEY (card_id) REFERENCES cards(id)
);

-- Create moves table
CREATE TABLE moves (
    id BIGSERIAL PRIMARY KEY,
    move_type VARCHAR(50) NOT NULL,
    player_id BIGINT NOT NULL,
    game_id VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    source_animal_id BIGINT,
    target_animal_id BIGINT,
    card_id BIGINT,
    FOREIGN KEY (player_id) REFERENCES players(id),
    FOREIGN KEY (game_id) REFERENCES games(id),
    FOREIGN KEY (source_animal_id) REFERENCES animals(id),
    FOREIGN KEY (target_animal_id) REFERENCES animals(id),
    FOREIGN KEY (card_id) REFERENCES cards(id)
);

-- Create player_hand table
CREATE TABLE player_hand (
    player_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    PRIMARY KEY (player_id, card_id),
    FOREIGN KEY (player_id) REFERENCES players(id),
    FOREIGN KEY (card_id) REFERENCES cards(id)
);

-- Create player_discard table
CREATE TABLE player_discard (
    player_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,
    PRIMARY KEY (player_id, card_id),
    FOREIGN KEY (player_id) REFERENCES players(id),
    FOREIGN KEY (card_id) REFERENCES cards(id)
); 