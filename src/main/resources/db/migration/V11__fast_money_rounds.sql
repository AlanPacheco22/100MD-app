CREATE TABLE fast_money_rounds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text VARCHAR(300),
    points INT NOT NULL DEFAULT 0,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    player_number INT NOT NULL,
    time_spent_ms BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_fmr_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    CONSTRAINT fk_fmr_participant FOREIGN KEY (participant_id) REFERENCES participants(id),
    CONSTRAINT fk_fmr_question FOREIGN KEY (question_id) REFERENCES questions(id)
);

CREATE INDEX idx_fmr_game ON fast_money_rounds(game_id);
CREATE INDEX idx_fmr_participant ON fast_money_rounds(participant_id);
CREATE INDEX idx_fmr_player ON fast_money_rounds(game_id, player_number);
