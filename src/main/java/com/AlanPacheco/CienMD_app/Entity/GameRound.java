package com.AlanPacheco.CienMD_app.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class GameRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne
    @JoinColumn(name = "game_question_id", nullable = false)
    private GameQuestion gameQuestion;

    @Column(nullable = false)
    private String answerText;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int multiplier;
}
