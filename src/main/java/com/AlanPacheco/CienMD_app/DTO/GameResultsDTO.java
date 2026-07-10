package com.AlanPacheco.CienMD_app.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameResultsDTO {
    private Long gameId;
    private String status;
    private int team1Score;
    private int team2Score;
    private List<ParticipantDTO> participants;
    private String winner;
}
