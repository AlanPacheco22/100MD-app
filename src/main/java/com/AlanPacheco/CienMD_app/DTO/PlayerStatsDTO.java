package com.AlanPacheco.CienMD_app.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerStatsDTO {
    private String playerName;
    private int gamesPlayed;
    private int totalScore;
    private double averageScore;
    private int correctAnswers;
    private int wrongAnswers;
}
