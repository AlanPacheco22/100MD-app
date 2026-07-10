CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role ENUM('ADMIN', 'PLAYER') NOT NULL DEFAULT 'PLAYER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    text VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    text VARCHAR(300) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

CREATE TABLE games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'FINISHED') NOT NULL DEFAULT 'NOT_STARTED',
    current_round_status ENUM('NOT_STARTED', 'TURN_PLAYER1', 'TURN_PLAYER2', 'FINISHED') NOT NULL DEFAULT 'NOT_STARTED',
    team1_score INT NOT NULL DEFAULT 0,
    team2_score INT NOT NULL DEFAULT 0,
    team1_errors INT NOT NULL DEFAULT 0,
    team2_errors INT NOT NULL DEFAULT 0,
    current_game_question_id BIGINT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_game_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE game_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_gq_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    CONSTRAINT fk_gq_question FOREIGN KEY (question_id) REFERENCES questions(id)
);

CREATE TABLE participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id BIGINT NOT NULL,
    user_id BIGINT,
    name VARCHAR(100) NOT NULL,
    team INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_participant_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    CONSTRAINT fk_participant_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE game_rounds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    game_question_id BIGINT NOT NULL,
    answer_text VARCHAR(300),
    score INT NOT NULL DEFAULT 0,
    multiplier INT NOT NULL DEFAULT 1,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gr_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    CONSTRAINT fk_gr_participant FOREIGN KEY (participant_id) REFERENCES participants(id),
    CONSTRAINT fk_gr_game_question FOREIGN KEY (game_question_id) REFERENCES game_questions(id)
);

CREATE TABLE game_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
);

ALTER TABLE games ADD CONSTRAINT fk_game_current_question
    FOREIGN KEY (current_game_question_id) REFERENCES game_questions(id);

CREATE INDEX idx_answers_question ON answers(question_id);
CREATE INDEX idx_game_questions_game ON game_questions(game_id);
CREATE INDEX idx_participants_game ON participants(game_id);
CREATE INDEX idx_game_rounds_game ON game_rounds(game_id);
CREATE INDEX idx_game_rounds_participant ON game_rounds(participant_id);
CREATE INDEX idx_game_log_game ON game_log(game_id);
CREATE INDEX idx_games_status ON games(status);
CREATE INDEX idx_game_questions_order ON game_questions(game_id, question_order);
