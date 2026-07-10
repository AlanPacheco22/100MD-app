package com.AlanPacheco.CienMD_app.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionDTO {
    private Long id;
    private String text;
    private List<AnswerDTO> answers;
}
