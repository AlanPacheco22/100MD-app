package com.AlanPacheco.CienMD_app.Entity;

import com.AlanPacheco.CienMD_app.Enum.GameRoundStatus;
import com.AlanPacheco.CienMD_app.Enum.GameStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
@Getter
@Setter
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Participant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<GameQuestion> gameQuestions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @Enumerated(EnumType.STRING)
    private GameRoundStatus currentRoundStatus;

    @Column(nullable = false)
    private int team1Score;

    @Column(nullable = false)
    private int team2Score;

    @Column(nullable = false)
    private int team1Errors;

    @Column(nullable = false)
    private int team2Errors;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_game_question_id")
    private GameQuestion currentGameQuestion;
}
