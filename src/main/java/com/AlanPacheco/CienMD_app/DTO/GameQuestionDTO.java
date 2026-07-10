package com.AlanPacheco.CienMD_app.DTO;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameQuestionDTO {
    private Long id;
    private String questionText;
    private List<AnswerDTO> answers;
}
