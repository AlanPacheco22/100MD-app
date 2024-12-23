package com.AlanPacheco.CienMD_app.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameUpdateDTO {
    private String message;
    private Long gameId;
    private int currentScore;
    private int roundNumber;
}
