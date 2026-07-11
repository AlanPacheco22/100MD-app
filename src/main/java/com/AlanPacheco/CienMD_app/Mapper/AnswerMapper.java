package com.AlanPacheco.CienMD_app.Mapper;

import com.AlanPacheco.CienMD_app.DTO.AnswerDTO;
import com.AlanPacheco.CienMD_app.Entity.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

    @Mapping(target = "revealed", ignore = true)
    AnswerDTO toDTO(Answer answer);
}
