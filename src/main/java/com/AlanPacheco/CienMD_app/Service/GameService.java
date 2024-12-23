package com.AlanPacheco.CienMD_app.Service;


import com.AlanPacheco.CienMD_app.DTO.RoundDTO;
import com.AlanPacheco.CienMD_app.Entity.Game;
import com.AlanPacheco.CienMD_app.Entity.GameRound;
import com.AlanPacheco.CienMD_app.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class GameService {

    @Autowired
    private GameRoundRepository gameRoundRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private GameQuestionRepository gameQuestionRepository;

    @Autowired
    private GameRepository gameRepository;

    // Registrar la respuesta de un participante en una ronda
    public GameRound registerAnswer(Long gameId, RoundDTO roundDTO) {
        var participant = participantRepository.findById(roundDTO.getParticipantId())
                .orElseThrow(() -> new RuntimeException("Participante no encontrado con ID: " + roundDTO.getParticipantId()));

        var gameQuestion = gameQuestionRepository.findById(roundDTO.getGameQuestionId())
                .orElseThrow(() -> new RuntimeException("Pregunta no encontrada en el juego"));

        var answer = answerRepository.findByQuestionIdAndTextIgnoreCase(gameQuestion.getQuestion().getId(), roundDTO.getAnswerText());

        int score = 0;
        if (answer != null) {
            score = answer.getScore() * roundDTO.getRoundMultiplier();
        }

        // Crear y guardar el registro de la ronda
        GameRound gameRound = new GameRound();
        Game game = gameRepository.getReferenceById(gameId);
        gameRound.setGame(game);
        gameRound.setParticipant(participant);
        gameRound.setGameQuestion(gameQuestion);
        gameRound.setAnswerText(roundDTO.getAnswerText());
        gameRound.setScore(score);
        gameRound.setMultiplier(roundDTO.getRoundMultiplier());

        return gameRoundRepository.save(gameRound);
    }

    // Obtener puntuación acumulada por participante
    public int getParticipantScore(Long participantId) {
        return gameRoundRepository.sumScoreByParticipantId(participantId);
    }

    // Obtener resultados finales del juego
    public String getFinalResults(Long gameId) {
        var participants = participantRepository.findByGameId(gameId);
        StringBuilder results = new StringBuilder("Resultados finales:\n");

        for (var participant : participants) {
            int totalScore = getParticipantScore(participant.getId());
            results.append(participant.getName()).append(": ").append(totalScore).append(" puntos\n");
        }

        return results.toString();
    }
}
