package com.AlanPacheco.CienMD_app.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FastMoneyDTO {

    private Long gameId;
    private String status;
    private int player1Id;
    private int player2Id;
    private String player1Name;
    private String player2Name;
    private int player1TotalPoints;
    private int player2TotalPoints;
    private int combinedTotal;
    private boolean wonBonus;
    private int bonusTarget;
    private List<FastMoneyAnswerDTO> player1Answers;
    private List<FastMoneyAnswerDTO> player2Answers;
    private List<String> questions;
}
