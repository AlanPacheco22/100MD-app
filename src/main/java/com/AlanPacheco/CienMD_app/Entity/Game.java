package com.AlanPacheco.CienMD_app.Entity;

import com.AlanPacheco.CienMD_app.Converter.IntArrayConverter;
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
    @Column(name = "current_round_status")
    private GameRoundStatus currentRoundStatus;

    @Column(name = "team1_score", nullable = false)
    private int team1Score;

    @Column(name = "team2_score", nullable = false)
    private int team2Score;

    @Column(name = "team1_errors", nullable = false)
    private int team1Errors;

    @Column(name = "team2_errors", nullable = false)
    private int team2Errors;

    @Column(name = "current_round_points", nullable = false)
    private int currentRoundPoints = 0;

    @Column(name = "rounds_played", nullable = false)
    private int roundsPlayed = 0;

    @Column(name = "total_rounds", nullable = false)
    private int totalRounds = 5;

    @Column(name = "target_score", nullable = false)
    private int targetScore = 300;

    @Column(name = "team_size", nullable = false)
    private int teamSize = 5;

    @Column(name = "current_multiplier", nullable = false)
    private int currentMultiplier = 1;

    @Column(name = "controlling_team")
    private Integer controllingTeam;

    @Column(name = "current_turn_index", nullable = false)
    private int currentTurnIndex = 0;

    @Column(name = "face_off_player1")
    private Long faceOffPlayer1;

    @Column(name = "face_off_player2")
    private Long faceOffPlayer2;

    @Column(name = "timer_enabled", nullable = false)
    private boolean timerEnabled = true;

    @Column(name = "turn_time_limit", nullable = false)
    private int turnTimeLimit = 10;

    @Column(name = "round_multipliers")
    @Convert(converter = IntArrayConverter.class)
    private int[] roundMultipliers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_game_question_id")
    private GameQuestion currentGameQuestion;
}
