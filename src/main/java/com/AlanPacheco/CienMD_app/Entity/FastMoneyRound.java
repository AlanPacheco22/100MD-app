package com.AlanPacheco.CienMD_app.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fast_money_rounds")
@Getter
@Setter
public class FastMoneyRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answer_text", length = 300)
    private String answerText;

    @Column(name = "points", nullable = false)
    private int points = 0;

    @Column(name = "is_correct", nullable = false)
    private boolean correct = false;

    @Column(name = "player_number", nullable = false)
    private int playerNumber;

    @Column(name = "time_spent_ms", nullable = false)
    private long timeSpentMs = 0;
}
