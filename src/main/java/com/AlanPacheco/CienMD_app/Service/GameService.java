package com.AlanPacheco.CienMD_app.Service;

import com.AlanPacheco.CienMD_app.DTO.AnswerDTO;
import com.AlanPacheco.CienMD_app.DTO.CreateParticipantDTO;
import com.AlanPacheco.CienMD_app.DTO.GameDTO;
import com.AlanPacheco.CienMD_app.DTO.GameQuestionDTO;
import com.AlanPacheco.CienMD_app.DTO.GameResultsDTO;
import com.AlanPacheco.CienMD_app.DTO.ParticipantDTO;
import com.AlanPacheco.CienMD_app.DTO.RoundDTO;
import com.AlanPacheco.CienMD_app.Entity.Answer;
import com.AlanPacheco.CienMD_app.Entity.Game;
import com.AlanPacheco.CienMD_app.Entity.GameQuestion;
import com.AlanPacheco.CienMD_app.Entity.GameRound;
import com.AlanPacheco.CienMD_app.Entity.Participant;
import com.AlanPacheco.CienMD_app.Entity.Question;
import com.AlanPacheco.CienMD_app.Enum.GameRoundStatus;
import com.AlanPacheco.CienMD_app.Enum.GameStatus;
import com.AlanPacheco.CienMD_app.Exception.GameAlreadyFinishedException;
import com.AlanPacheco.CienMD_app.Exception.GameNotFoundException;
import com.AlanPacheco.CienMD_app.Exception.GameQuestionNotFoundException;
import com.AlanPacheco.CienMD_app.Exception.InsufficientQuestionsException;
import com.AlanPacheco.CienMD_app.Exception.ParticipantNotFoundException;
import com.AlanPacheco.CienMD_app.Repository.AnswerRepository;
import com.AlanPacheco.CienMD_app.Repository.GameQuestionRepository;
import com.AlanPacheco.CienMD_app.Repository.GameRepository;
import com.AlanPacheco.CienMD_app.Repository.GameRoundRepository;
import com.AlanPacheco.CienMD_app.Repository.ParticipantRepository;
import com.AlanPacheco.CienMD_app.Repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GameRoundRepository gameRoundRepository;
    private final GameQuestionRepository gameQuestionRepository;
    private final AnswerRepository answerRepository;
    private final ParticipantRepository participantRepository;
    private final QuestionRepository questionRepository;

    public GameService(GameRepository gameRepository, GameRoundRepository gameRoundRepository,
                       GameQuestionRepository gameQuestionRepository, AnswerRepository answerRepository,
                       ParticipantRepository participantRepository, QuestionRepository questionRepository) {
        this.gameRepository = gameRepository;
        this.gameRoundRepository = gameRoundRepository;
        this.gameQuestionRepository = gameQuestionRepository;
        this.answerRepository = answerRepository;
        this.participantRepository = participantRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public GameDTO createNewGame() {
        long questionCount = questionRepository.count();
        if (questionCount < 3) {
            throw new InsufficientQuestionsException(
                    "Se necesitan al menos 3 preguntas. Hay: " + questionCount);
        }

        Game game = new Game();
        game.setDate(LocalDateTime.now());
        game.setStatus(GameStatus.NOT_STARTED);
        game.setCurrentRoundStatus(GameRoundStatus.NOT_STARTED);
        game.setTeam1Score(0);
        game.setTeam2Score(0);
        game.setTeam1Errors(0);
        game.setTeam2Errors(0);
        game = gameRepository.save(game);

        List<Question> allQuestions = questionRepository.findAll();
        Collections.shuffle(allQuestions);
        List<Question> selected = allQuestions.subList(0, 3);

        List<GameQuestion> gameQuestions = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++) {
            GameQuestion gq = new GameQuestion();
            gq.setGame(game);
            gq.setQuestion(selected.get(i));
            gq.setQuestionOrder(i);
            gameQuestions.add(gq);
        }
        gameQuestionRepository.saveAll(gameQuestions);
        game.setGameQuestions(gameQuestions);

        return mapToGameDTO(gameRepository.save(game));
    }

    public GameDTO getGameById(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));
        return mapToGameDTO(game);
    }

    @Transactional
    public GameDTO startNextRound(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getStatus() == GameStatus.FINISHED) {
            throw new GameAlreadyFinishedException("La partida ya terminó");
        }

        if (game.getCurrentRoundStatus() != GameRoundStatus.NOT_STARTED &&
                game.getCurrentRoundStatus() != GameRoundStatus.FINISHED) {
            throw new IllegalStateException("La ronda actual no ha terminado");
        }

        List<GameQuestion> gameQuestions = game.getGameQuestions();

        if (game.getCurrentGameQuestion() == null) {
            game.setCurrentGameQuestion(gameQuestions.get(0));
        } else {
            int currentIndex = -1;
            for (int i = 0; i < gameQuestions.size(); i++) {
                if (gameQuestions.get(i).getId().equals(game.getCurrentGameQuestion().getId())) {
                    currentIndex = i;
                    break;
                }
            }
            if (currentIndex + 1 < gameQuestions.size()) {
                game.setCurrentGameQuestion(gameQuestions.get(currentIndex + 1));
            } else {
                game.setStatus(GameStatus.FINISHED);
                game.setCurrentRoundStatus(GameRoundStatus.FINISHED);
                gameRepository.save(game);
                throw new IllegalStateException("Ya no hay más preguntas. La partida ha terminado.");
            }
        }

        game.setStatus(GameStatus.IN_PROGRESS);
        game.setCurrentRoundStatus(GameRoundStatus.TURN_PLAYER1);
        game.setTeam1Errors(0);
        game.setTeam2Errors(0);
        gameRepository.save(game);

        return mapToGameDTO(game);
    }

    @Transactional
    public GameQuestionDTO submitAnswer(Long gameId, RoundDTO roundDTO) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getStatus() == GameStatus.FINISHED) {
            throw new GameAlreadyFinishedException("La partida ya terminó");
        }

        Participant participant = participantRepository.findById(roundDTO.getParticipantId())
                .orElseThrow(() -> new ParticipantNotFoundException(
                        "Participante no encontrado: " + roundDTO.getParticipantId()));

        GameQuestion gameQuestion = gameQuestionRepository.findById(roundDTO.getGameQuestionId())
                .orElseThrow(() -> new GameQuestionNotFoundException(
                        "Pregunta no encontrada en la partida"));

        Answer answer = answerRepository.findByQuestionIdAndTextIgnoreCase(
                gameQuestion.getQuestion().getId(), roundDTO.getAnswerText());

        int multiplier = roundDTO.getRoundMultiplier();
        if (multiplier <= 0) {
            multiplier = 1;
        }

        GameRound round = new GameRound();
        round.setGame(game);
        round.setParticipant(participant);
        round.setGameQuestion(gameQuestion);
        round.setAnswerText(roundDTO.getAnswerText());
        round.setMultiplier(multiplier);

        if (answer != null) {
            int points = answer.getScore() * multiplier;
            round.setScore(points);
            round.setCorrect(true);
            accumulatePoints(game, points, participant.getTeam());
        } else {
            round.setScore(0);
            round.setCorrect(false);
            incrementErrors(game, participant.getTeam());
        }

        gameRoundRepository.save(round);
        gameRepository.save(game);

        return mapToGameQuestionDTO(gameQuestion);
    }

    @Transactional
    public GameDTO endRound(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getCurrentRoundStatus() == GameRoundStatus.FINISHED) {
            throw new IllegalStateException("La ronda ya terminó");
        }

        game.setCurrentRoundStatus(GameRoundStatus.FINISHED);
        gameRepository.save(game);

        return mapToGameDTO(game);
    }

    public GameResultsDTO getFinalResults(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        List<Participant> participants = participantRepository.findByGameId(gameId);
        List<ParticipantDTO> participantDTOs = participants.stream()
                .map(p -> {
                    ParticipantDTO dto = new ParticipantDTO();
                    dto.setId(p.getId());
                    dto.setName(p.getName());
                    dto.setTeam(p.getTeam());
                    dto.setScore(gameRoundRepository.sumScoreByParticipantId(p.getId()));
                    return dto;
                })
                .toList();

        GameResultsDTO results = new GameResultsDTO();
        results.setGameId(gameId);
        results.setStatus(game.getStatus().toString());
        results.setTeam1Score(game.getTeam1Score());
        results.setTeam2Score(game.getTeam2Score());
        results.setParticipants(participantDTOs);

        if (game.getTeam1Score() > game.getTeam2Score()) {
            results.setWinner("team1");
        } else if (game.getTeam2Score() > game.getTeam1Score()) {
            results.setWinner("team2");
        } else {
            results.setWinner("draw");
        }

        return results;
    }

    public List<GameDTO> getAllGames() {
        return gameRepository.findAll().stream()
                .map(this::mapToGameDTO)
                .toList();
    }

    @Transactional
    public ParticipantDTO addParticipant(Long gameId, CreateParticipantDTO dto) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getStatus() != GameStatus.NOT_STARTED) {
            throw new IllegalStateException("No se pueden agregar participantes a una partida ya iniciada");
        }

        Participant participant = new Participant();
        participant.setGame(game);
        participant.setName(dto.getName());
        participant.setTeam(dto.getTeam());
        participant = participantRepository.save(participant);

        ParticipantDTO result = new ParticipantDTO();
        result.setId(participant.getId());
        result.setName(participant.getName());
        result.setTeam(participant.getTeam());
        result.setScore(0);
        return result;
    }

    public List<ParticipantDTO> getParticipants(Long gameId) {
        return participantRepository.findByGameId(gameId).stream()
                .map(p -> {
                    ParticipantDTO dto = new ParticipantDTO();
                    dto.setId(p.getId());
                    dto.setName(p.getName());
                    dto.setTeam(p.getTeam());
                    dto.setScore(gameRoundRepository.sumScoreByParticipantId(p.getId()));
                    return dto;
                })
                .toList();
    }

    private void accumulatePoints(Game game, int points, int team) {
        if (team == 1) {
            game.setTeam1Score(game.getTeam1Score() + points);
        } else {
            game.setTeam2Score(game.getTeam2Score() + points);
        }
    }

    private void incrementErrors(Game game, int team) {
        if (team == 1) {
            game.setTeam1Errors(game.getTeam1Errors() + 1);
            if (game.getTeam1Errors() >= 3) {
                game.setCurrentRoundStatus(GameRoundStatus.TURN_PLAYER2);
            }
        } else {
            game.setTeam2Errors(game.getTeam2Errors() + 1);
            if (game.getTeam2Errors() >= 1) {
                game.setCurrentRoundStatus(GameRoundStatus.FINISHED);
            }
        }
    }

    private GameDTO mapToGameDTO(Game game) {
        GameDTO dto = new GameDTO();
        dto.setId(game.getId());
        dto.setStatus(game.getStatus().toString());
        dto.setTeam1Score(game.getTeam1Score());
        dto.setTeam2Score(game.getTeam2Score());
        dto.setTeam1Errors(game.getTeam1Errors());
        dto.setTeam2Errors(game.getTeam2Errors());
        if (game.getCurrentGameQuestion() != null) {
            dto.setCurrentQuestionId(game.getCurrentGameQuestion().getId());
        }
        return dto;
    }

    private GameQuestionDTO mapToGameQuestionDTO(GameQuestion gameQuestion) {
        GameQuestionDTO dto = new GameQuestionDTO();
        dto.setId(gameQuestion.getId());
        dto.setQuestionText(gameQuestion.getQuestion().getText());

        List<AnswerDTO> answers = gameQuestion.getQuestion().getAnswers().stream()
                .map(answer -> {
                    AnswerDTO answerDTO = new AnswerDTO();
                    answerDTO.setId(answer.getId());
                    answerDTO.setText(answer.getText());
                    answerDTO.setScore(answer.getScore());
                    return answerDTO;
                })
                .toList();

        dto.setAnswers(answers);
        return dto;
    }
}
