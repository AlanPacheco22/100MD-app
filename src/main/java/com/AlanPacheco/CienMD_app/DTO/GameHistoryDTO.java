package com.AlanPacheco.CienMD_app.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameHistoryDTO {
    private Long id;
    private String date;
    private String status;
    private int team1Score;
    private int team2Score;
    private String winner;
    private int totalRounds;
}
