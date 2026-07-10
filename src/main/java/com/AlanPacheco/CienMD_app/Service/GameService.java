package com.AlanPacheco.CienMD_app.Service;

import com.AlanPacheco.CienMD_app.DTO.AnswerDTO;
import com.AlanPacheco.CienMD_app.DTO.CreateGameDTO;
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
import com.AlanPacheco.CienMD_app.Config.GameEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final GameRoundRepository gameRoundRepository;
    private final GameQuestionRepository gameQuestionRepository;
    private final AnswerRepository answerRepository;
    private final ParticipantRepository participantRepository;
    private final QuestionRepository questionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public GameService(GameRepository gameRepository, GameRoundRepository gameRoundRepository,
                       GameQuestionRepository gameQuestionRepository, AnswerRepository answerRepository,
                       ParticipantRepository participantRepository, QuestionRepository questionRepository,
                       ApplicationEventPublisher eventPublisher) {
        this.gameRepository = gameRepository;
        this.gameRoundRepository = gameRoundRepository;
        this.gameQuestionRepository = gameQuestionRepository;
        this.answerRepository = answerRepository;
        this.participantRepository = participantRepository;
        this.questionRepository = questionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public GameDTO createNewGame(CreateGameDTO config) {
        System.out.println("DEBUG: createNewGame() - Iniciando creacion de nueva partida");
        long questionCount = questionRepository.count();
        System.out.println("DEBUG: createNewGame() - Total preguntas disponibles: " + questionCount);
        if (questionCount < config.getTotalRounds()) {
            System.out.println("DEBUG: createNewGame() - ERROR: preguntas insuficientes: " + questionCount);
            throw new InsufficientQuestionsException(
                    "Se necesitan al menos " + config.getTotalRounds() + " preguntas. Hay: " + questionCount);
        }

        int[] multipliers = config.getMultipliers();
        if (multipliers == null || multipliers.length == 0) {
            multipliers = new int[]{1, 1, 2};
        }

        Game game = new Game();
        game.setDate(LocalDateTime.now());
        game.setStatus(GameStatus.NOT_STARTED);
        game.setCurrentRoundStatus(GameRoundStatus.NOT_STARTED);
        game.setTeam1Score(0);
        game.setTeam2Score(0);
        game.setTeam1Errors(0);
        game.setTeam2Errors(0);
        game.setCurrentRoundPoints(0);
        game.setRoundsPlayed(0);
        game.setTotalRounds(config.getTotalRounds());
        game.setTeamSize(config.getTeamSize());
        game.setCurrentMultiplier(multipliers[0]);
        game.setControllingTeam(null);
        game = gameRepository.save(game);
        System.out.println("DEBUG: createNewGame() - Partida creada con ID: " + game.getId());

        List<Question> allQuestions = questionRepository.findAll();
        Collections.shuffle(allQuestions);
        List<Question> selected = allQuestions.subList(0, config.getTotalRounds());
        System.out.println("DEBUG: createNewGame() - Seleccionadas " + config.getTotalRounds() + " preguntas para la partida");

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

        game = gameRepository.save(game);
        eventPublisher.publishEvent(new GameEvent("GAME_CREATED", game.getId()));
        System.out.println("DEBUG: createNewGame() - Partida " + game.getId() + " creada exitosamente");
        return mapToGameDTO(game);
    }

    public GameDTO getGameById(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));
        return mapToGameDTO(game);
    }

    public GameQuestionDTO getGameQuestion(Long gameId, Long questionId) {
        GameQuestion gameQuestion = gameQuestionRepository.findById(questionId)
                .orElseThrow(() -> new GameQuestionNotFoundException("Pregunta no encontrada: " + questionId));
        return mapToGameQuestionDTO(gameQuestion, gameId);
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
                eventPublisher.publishEvent(new GameEvent("GAME_FINISHED", gameId));
                return mapToGameDTO(game);
            }
        }

        int newRound = game.getRoundsPlayed() + 1;
        game.setRoundsPlayed(newRound);

        int[] multipliers = getMultipliersForGame(game);
        int multiplierIndex = Math.min(newRound - 1, multipliers.length - 1);
        game.setCurrentMultiplier(multipliers[multiplierIndex]);

        int controllingTeam = (newRound % 2 == 1) ? 1 : 2;
        game.setControllingTeam(controllingTeam);

        game.setStatus(GameStatus.IN_PROGRESS);
        game.setCurrentRoundStatus(
                controllingTeam == 1 ? GameRoundStatus.TURN_PLAYER1 : GameRoundStatus.TURN_PLAYER2
        );
        game.setTeam1Errors(0);
        game.setTeam2Errors(0);
        game.setCurrentRoundPoints(0);
        gameRepository.save(game);
        eventPublisher.publishEvent(new GameEvent("ROUND_STARTED", gameId));
        System.out.println("DEBUG: startNextRound() - Ronda " + newRound + " iniciada. Equipo control: " + controllingTeam + ", Multiplicador: " + game.getCurrentMultiplier());

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
            handleCorrectAnswer(game, points, participant.getTeam());
            eventPublisher.publishEvent(new GameEvent("ANSWER_CORRECT", gameId));
        } else {
            round.setScore(0);
            round.setCorrect(false);
            handleIncorrectAnswer(game, participant.getTeam());
            eventPublisher.publishEvent(new GameEvent("ANSWER_WRONG", gameId));
        }

        gameRoundRepository.save(round);
        gameRepository.save(game);

        return mapToGameQuestionDTO(gameQuestion, gameId);
    }

    private void handleCorrectAnswer(Game game, int points, int team) {
        if (game.getCurrentRoundStatus() == GameRoundStatus.STEAL_ATTEMPT) {
            if (team == 2) {
                game.setTeam2Score(game.getTeam2Score() + game.getCurrentRoundPoints() + points);
                game.setCurrentRoundPoints(0);
                game.setCurrentRoundStatus(GameRoundStatus.FINISHED);
            } else {
                game.setCurrentRoundPoints(game.getCurrentRoundPoints() + points);
            }
        } else {
            game.setCurrentRoundPoints(game.getCurrentRoundPoints() + points);
        }
    }

    private void handleIncorrectAnswer(Game game, int team) {
        if (game.getCurrentRoundStatus() == GameRoundStatus.STEAL_ATTEMPT) {
            if (team == 1) {
                game.setTeam2Score(game.getTeam2Score() + game.getCurrentRoundPoints());
            } else {
                game.setTeam1Score(game.getTeam1Score() + game.getCurrentRoundPoints());
            }
            game.setCurrentRoundPoints(0);
            game.setCurrentRoundStatus(GameRoundStatus.FINISHED);
        } else if (game.getCurrentRoundStatus() == GameRoundStatus.TURN_PLAYER1) {
            game.setTeam1Errors(game.getTeam1Errors() + 1);
            if (game.getTeam1Errors() >= 3) {
                game.setCurrentRoundStatus(GameRoundStatus.STEAL_ATTEMPT);
            }
        } else if (game.getCurrentRoundStatus() == GameRoundStatus.TURN_PLAYER2) {
            game.setTeam2Errors(game.getTeam2Errors() + 1);
            if (game.getTeam2Errors() >= 3) {
                game.setCurrentRoundStatus(GameRoundStatus.STEAL_ATTEMPT);
            }
        }
    }

    @Transactional
    public GameDTO passTurn(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getCurrentRoundStatus() != GameRoundStatus.TURN_PLAYER1 &&
                game.getCurrentRoundStatus() != GameRoundStatus.TURN_PLAYER2) {
            throw new IllegalStateException("Solo se puede pasar el turno durante un turno activo");
        }

        game.setCurrentRoundStatus(GameRoundStatus.STEAL_ATTEMPT);
        gameRepository.save(game);
        eventPublisher.publishEvent(new GameEvent("TURN_PASSED", gameId));
        return mapToGameDTO(game);
    }

    @Transactional
    public GameDTO endRound(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getCurrentRoundStatus() == GameRoundStatus.FINISHED) {
            throw new IllegalStateException("La ronda ya terminó");
        }

        if (game.getControllingTeam() != null) {
            if (game.getControllingTeam() == 1) {
                game.setTeam1Score(game.getTeam1Score() + game.getCurrentRoundPoints());
            } else {
                game.setTeam2Score(game.getTeam2Score() + game.getCurrentRoundPoints());
            }
        }

        game.setCurrentRoundStatus(GameRoundStatus.FINISHED);
        game.setCurrentRoundPoints(0);
        gameRepository.save(game);
        eventPublisher.publishEvent(new GameEvent("ROUND_ENDED", gameId));

        return mapToGameDTO(game);
    }

    @Transactional
    public GameQuestionDTO revealAnswer(Long gameId, Long answerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getCurrentGameQuestion() == null) {
            throw new IllegalStateException("No hay pregunta activa en esta partida");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Respuesta no encontrada: " + answerId));

        GameQuestion gameQuestion = game.getCurrentGameQuestion();
        GameQuestionDTO dto = mapToGameQuestionDTO(gameQuestion, gameId);

        for (AnswerDTO ans : dto.getAnswers()) {
            if (ans.getId().equals(answerId)) {
                ans.setRevealed(true);
            }
        }

        return dto;
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

        long teamCount = participantRepository.findByGameId(gameId).stream()
                .filter(p -> p.getTeam() == dto.getTeam())
                .count();
        if (teamCount >= game.getTeamSize()) {
            throw new IllegalStateException("El equipo " + dto.getTeam() + " ya tiene " + game.getTeamSize() + " miembros (máximo)");
        }

        Participant participant = new Participant();
        participant.setGame(game);
        participant.setName(dto.getName());
        participant.setTeam(dto.getTeam());
        participant.setMemberOrder((int) teamCount);
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

    private GameDTO mapToGameDTO(Game game) {
        GameDTO dto = new GameDTO();
        dto.setId(game.getId());
        dto.setDate(game.getDate());
        dto.setStatus(game.getStatus().toString());
        dto.setCurrentRoundStatus(game.getCurrentRoundStatus().toString());
        dto.setTeam1Score(game.getTeam1Score());
        dto.setTeam2Score(game.getTeam2Score());
        dto.setTeam1Errors(game.getTeam1Errors());
        dto.setTeam2Errors(game.getTeam2Errors());
        dto.setCurrentRoundPoints(game.getCurrentRoundPoints());
        dto.setRoundsPlayed(game.getRoundsPlayed());
        dto.setTotalRounds(game.getTotalRounds());
        dto.setTeamSize(game.getTeamSize());
        dto.setCurrentMultiplier(game.getCurrentMultiplier());
        dto.setControllingTeam(game.getControllingTeam());
        if (game.getCurrentGameQuestion() != null) {
            dto.setCurrentQuestionId(game.getCurrentGameQuestion().getId());
            dto.setGameQuestionText(game.getCurrentGameQuestion().getQuestion().getText());
            dto.setCurrentAnswers(getCurrentAnswersForQuestion(game));
        }
        if (game.getStatus() == GameStatus.FINISHED) {
            if (game.getTeam1Score() > game.getTeam2Score()) {
                dto.setWinner("team1");
            } else if (game.getTeam2Score() > game.getTeam1Score()) {
                dto.setWinner("team2");
            } else {
                dto.setWinner("draw");
            }
        }
        return dto;
    }

    private List<AnswerDTO> getCurrentAnswersForQuestion(Game game) {
        if (game.getCurrentGameQuestion() == null) {
            return List.of();
        }
        return mapToGameQuestionDTO(game.getCurrentGameQuestion(), game.getId()).getAnswers();
    }

    private GameQuestionDTO mapToGameQuestionDTO(GameQuestion gameQuestion, Long gameId) {
        GameQuestionDTO dto = new GameQuestionDTO();
        dto.setId(gameQuestion.getId());
        dto.setQuestionText(gameQuestion.getQuestion().getText());

        Set<Long> revealedIds = new HashSet<>();
        List<GameRound> rounds = gameRoundRepository.findByGameIdAndGameQuestionId(gameId, gameQuestion.getId());
        for (GameRound r : rounds) {
            if (r.isCorrect()) {
                Answer answered = answerRepository.findByQuestionIdAndTextIgnoreCase(
                        gameQuestion.getQuestion().getId(), r.getAnswerText());
                if (answered != null) {
                    revealedIds.add(answered.getId());
                }
            }
        }

        List<AnswerDTO> answerDTOs = gameQuestion.getQuestion().getAnswers().stream()
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

    private int[] getMultipliersForGame(Game game) {
        int totalRounds = game.getTotalRounds();
        int[] multipliers = new int[totalRounds];
        for (int i = 0; i < totalRounds; i++) {
            if (i < totalRounds - 2) {
                multipliers[i] = 1;
            } else if (i == totalRounds - 2) {
                multipliers[i] = 2;
            } else {
                multipliers[i] = 3;
            }
        }
        return multipliers;
    }
}
