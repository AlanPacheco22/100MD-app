package com.AlanPacheco.CienMD_app.Mapper;

import com.AlanPacheco.CienMD_app.DTO.FastMoneyAnswerDTO;
import com.AlanPacheco.CienMD_app.Entity.FastMoneyRound;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FastMoneyAnswerMapper {

    @Mapping(target = "question", expression = "java(fastMoneyRound.getQuestion().getText())")
    @Mapping(target = "answer", source = "answerText")
    @Mapping(target = "correct", source = "correct")
    FastMoneyAnswerDTO toDTO(FastMoneyRound fastMoneyRound);
}
