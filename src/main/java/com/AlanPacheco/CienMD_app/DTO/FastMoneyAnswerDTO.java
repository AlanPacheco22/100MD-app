package com.AlanPacheco.CienMD_app.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FastMoneyAnswerDTO {

    private String question;
    private String answer;
    private int points;
    private boolean correct;
}
