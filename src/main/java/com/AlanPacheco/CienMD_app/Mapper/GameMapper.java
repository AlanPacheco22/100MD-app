package com.AlanPacheco.CienMD_app.Mapper;

import com.AlanPacheco.CienMD_app.DTO.GameDTO;
import com.AlanPacheco.CienMD_app.Entity.Game;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GameMapper {

    @Mapping(target = "status", expression = "java(game.getStatus().name())")
    @Mapping(target = "currentRoundStatus", expression = "java(game.getCurrentRoundStatus().name())")
    @Mapping(target = "roundMultipliers", expression = "java(game.getRoundMultipliers())")
    @Mapping(target = "currentQuestionId", expression = "java(game.getCurrentGameQuestion() != null ? game.getCurrentGameQuestion().getId() : null)")
    @Mapping(target = "gameQuestionText", expression = "java(game.getCurrentGameQuestion() != null ? game.getCurrentGameQuestion().getQuestion().getText() : null)")
    @Mapping(target = "winner", ignore = true)
    @Mapping(target = "currentAnswers", ignore = true)
    GameDTO toDTO(Game game);
}
