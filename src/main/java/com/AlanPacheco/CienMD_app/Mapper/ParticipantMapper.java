package com.AlanPacheco.CienMD_app.Mapper;

import com.AlanPacheco.CienMD_app.DTO.ParticipantDTO;
import com.AlanPacheco.CienMD_app.Entity.Participant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParticipantMapper {

    @Mapping(target = "score", ignore = true)
    ParticipantDTO toDTO(Participant participant);
}
