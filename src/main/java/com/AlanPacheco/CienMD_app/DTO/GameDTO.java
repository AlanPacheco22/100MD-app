package com.AlanPacheco.CienMD_app.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GameDTO {
    private Long id;
    private LocalDateTime date;
    private String status;
    private String currentRoundStatus;
    private int team1Score;
    private int team2Score;
    private int team1Errors;
    private int team2Errors;
    private int currentRoundPoints;
    private int roundsPlayed;
    private Long currentQuestionId;
    private int totalRounds;
    private int teamSize;
    private int currentMultiplier;
    private Integer controllingTeam;
    private int targetScore;
    private int[] roundMultipliers;
    private int currentTurnIndex;
    private boolean timerEnabled;
    private int turnTimeLimit;
    private String winner;
    private String gameQuestionText;
    private List<AnswerDTO> currentAnswers;
}
