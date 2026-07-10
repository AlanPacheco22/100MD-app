package com.AlanPacheco.CienMD_app.Service;

import com.AlanPacheco.CienMD_app.DTO.GameHistoryDTO;
import com.AlanPacheco.CienMD_app.DTO.PlayerStatsDTO;
import com.AlanPacheco.CienMD_app.Entity.Game;
import com.AlanPacheco.CienMD_app.Entity.GameRound;
import com.AlanPacheco.CienMD_app.Entity.Participant;
import com.AlanPacheco.CienMD_app.Repository.GameRepository;
import com.AlanPacheco.CienMD_app.Repository.GameRoundRepository;
import com.AlanPacheco.CienMD_app.Repository.ParticipantRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatsService {

    private final GameRepository gameRepository;
    private final GameRoundRepository gameRoundRepository;
    private final ParticipantRepository participantRepository;

    public StatsService(GameRepository gameRepository, GameRoundRepository gameRoundRepository,
                        ParticipantRepository participantRepository) {
        this.gameRepository = gameRepository;
        this.gameRoundRepository = gameRoundRepository;
        this.participantRepository = participantRepository;
    }

    public List<GameHistoryDTO> getGameHistory() {
        return gameRepository.findAllByOrderByDateDesc().stream()
                .map(this::toHistoryDTO)
                .toList();
    }

    public List<PlayerStatsDTO> getPlayerStats() {
        List<Participant> allParticipants = participantRepository.findAll();
        Map<String, PlayerStatsDTO> statsMap = new LinkedHashMap<>();

        for (Participant p : allParticipants) {
            String name = p.getName() != null ? p.getName() : "Anónimo";
            PlayerStatsDTO s = statsMap.computeIfAbsent(name, k -> {
                PlayerStatsDTO dto = new PlayerStatsDTO();
                dto.setPlayerName(k);
                return dto;
            });
            s.setGamesPlayed(s.getGamesPlayed() + 1);
            int score = gameRoundRepository.sumScoreByParticipantId(p.getId());
            s.setTotalScore(s.getTotalScore() + score);
            List<GameRound> rounds = gameRoundRepository.findByParticipantId(p.getId());
            long correct = rounds.stream().filter(GameRound::isCorrect).count();
            long wrong = rounds.size() - correct;
            s.setCorrectAnswers((int) (s.getCorrectAnswers() + correct));
            s.setWrongAnswers((int) (s.getWrongAnswers() + wrong));
        }

        statsMap.values().forEach(s -> {
            s.setAverageScore(s.getGamesPlayed() > 0
                    ? (double) Math.round((double) s.getTotalScore() / s.getGamesPlayed() * 100) / 100
                    : 0);
        });

        return new ArrayList<>(statsMap.values());
    }

    private GameHistoryDTO toHistoryDTO(Game game) {
        GameHistoryDTO dto = new GameHistoryDTO();
        dto.setId(game.getId());
        dto.setDate(game.getDate() != null ? game.getDate().toString() : "");
        dto.setStatus(game.getStatus().toString());
        dto.setTeam1Score(game.getTeam1Score());
        dto.setTeam2Score(game.getTeam2Score());
        dto.setTotalRounds(game.getRoundsPlayed());
        if (game.getTeam1Score() > game.getTeam2Score()) dto.setWinner("Equipo 1");
        else if (game.getTeam2Score() > game.getTeam1Score()) dto.setWinner("Equipo 2");
        else dto.setWinner("Empate");
        return dto;
    }
}
