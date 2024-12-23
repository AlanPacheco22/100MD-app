package com.AlanPacheco.CienMD_app.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoundDTO {
    private Long participantId;
    private Long gameQuestionId;
    private String answerText;
    private int roundMultiplier;
}
