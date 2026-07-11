package com.AlanPacheco.CienMD_app.Service;

import com.AlanPacheco.CienMD_app.DTO.GameHistoryDTO;
import com.AlanPacheco.CienMD_app.DTO.PlayerStatsDTO;
import com.AlanPacheco.CienMD_app.Entity.Game;
import com.AlanPacheco.CienMD_app.Entity.GameRound;
import com.AlanPacheco.CienMD_app.Entity.Participant;
import com.AlanPacheco.CienMD_app.Mapper.GameHistoryMapper;
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
    private final GameHistoryMapper gameHistoryMapper;

    public StatsService(GameRepository gameRepository, GameRoundRepository gameRoundRepository,
                        ParticipantRepository participantRepository, GameHistoryMapper gameHistoryMapper) {
        this.gameRepository = gameRepository;
        this.gameRoundRepository = gameRoundRepository;
        this.participantRepository = participantRepository;
        this.gameHistoryMapper = gameHistoryMapper;
    }

    public List<GameHistoryDTO> getGameHistory() {
        return gameRepository.findAllByOrderByDateDesc().stream()
                .map(gameHistoryMapper::toDTO)
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
}
