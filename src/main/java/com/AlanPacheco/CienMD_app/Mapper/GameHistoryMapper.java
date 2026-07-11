package com.AlanPacheco.CienMD_app.Mapper;

import com.AlanPacheco.CienMD_app.DTO.GameHistoryDTO;
import com.AlanPacheco.CienMD_app.Entity.Game;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface GameHistoryMapper {

    @Mapping(target = "date", expression = "java(formatDate(game))")
    @Mapping(target = "status", expression = "java(game.getStatus().name())")
    @Mapping(target = "winner", expression = "java(resolveWinner(game))")
    @Mapping(target = "totalRounds", source = "roundsPlayed")
    com.AlanPacheco.CienMD_app.DTO.GameHistoryDTO toDTO(Game game);

    default String formatDate(Game game) {
        if (game.getDate() == null) return "";
        return game.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    default String resolveWinner(Game game) {
        if (game.getTeam1Score() > game.getTeam2Score()) return "Equipo 1";
        if (game.getTeam2Score() > game.getTeam1Score()) return "Equipo 2";
        return "Empate";
    }
}
