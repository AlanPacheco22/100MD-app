package com.AlanPacheco.CienMD_app.Mapper;

import com.AlanPacheco.CienMD_app.DTO.GameQuestionDTO;
import com.AlanPacheco.CienMD_app.DTO.AnswerDTO;
import com.AlanPacheco.CienMD_app.Entity.Answer;
import com.AlanPacheco.CienMD_app.Entity.GameQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface GameQuestionMapper {

    @Mapping(target = "questionText", expression = "java(gameQuestion.getQuestion().getText())")
    @Mapping(target = "answers", ignore = true)
    GameQuestionDTO toDTO(GameQuestion gameQuestion);

    default GameQuestionDTO toDTOWithRevealed(GameQuestion gameQuestion, Set<Long> revealedIds) {
        GameQuestionDTO dto = toDTO(gameQuestion);
        List<Answer> answers = gameQuestion.getQuestion().getAnswers();
        List<AnswerDTO> answerDTOs = (answers != null ? answers : List.<Answer>of()).stream()
                .map(answer -> {
                    AnswerDTO answerDTO = new AnswerDTO();
                    answerDTO.setId(answer.getId());
                    answerDTO.setText(answer.getText());
                    answerDTO.setScore(answer.getScore());
                    answerDTO.setRevealed(revealedIds.contains(answer.getId()));
                    return answerDTO;
                })
                .toList();
        dto.setAnswers(answerDTOs);
        return dto;
    }
}
