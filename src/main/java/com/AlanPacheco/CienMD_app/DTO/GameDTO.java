package com.AlanPacheco.CienMD_app.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameDTO {
    private Long id;
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
}
