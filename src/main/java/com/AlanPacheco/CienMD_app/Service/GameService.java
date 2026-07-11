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
import com.AlanPacheco.CienMD_app.Mapper.GameMapper;
import com.AlanPacheco.CienMD_app.Mapper.GameQuestionMapper;
import com.AlanPacheco.CienMD_app.Mapper.ParticipantMapper;
import com.AlanPacheco.CienMD_app.Repository.AnswerRepository;
import com.AlanPacheco.CienMD_app.Repository.GameQuestionRepository;
import com.AlanPacheco.CienMD_app.Repository.GameRepository;
import com.AlanPacheco.CienMD_app.Repository.GameRoundRepository;
import com.AlanPacheco.CienMD_app.Repository.ParticipantRepository;
import com.AlanPacheco.CienMD_app.Repository.QuestionRepository;
import com.AlanPacheco.CienMD_app.Config.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final GameRepository gameRepository;
    private final GameRoundRepository gameRoundRepository;
    private final GameQuestionRepository gameQuestionRepository;
    private final AnswerRepository answerRepository;
    private final ParticipantRepository participantRepository;
    private final QuestionRepository questionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final GameMapper gameMapper;
    private final GameQuestionMapper gameQuestionMapper;
    private final ParticipantMapper participantMapper;

    public GameService(GameRepository gameRepository, GameRoundRepository gameRoundRepository,
                       GameQuestionRepository gameQuestionRepository, AnswerRepository answerRepository,
                       ParticipantRepository participantRepository, QuestionRepository questionRepository,
                       ApplicationEventPublisher eventPublisher,
                       GameMapper gameMapper, GameQuestionMapper gameQuestionMapper,
                       ParticipantMapper participantMapper) {
        this.gameRepository = gameRepository;
        this.gameRoundRepository = gameRoundRepository;
        this.gameQuestionRepository = gameQuestionRepository;
        this.answerRepository = answerRepository;
        this.participantRepository = participantRepository;
        this.questionRepository = questionRepository;
        this.eventPublisher = eventPublisher;
        this.gameMapper = gameMapper;
        this.gameQuestionMapper = gameQuestionMapper;
        this.participantMapper = participantMapper;
    }

    @Transactional
    public GameDTO createNewGame(CreateGameDTO config) {
        log.info("[createNewGame] Iniciando creacion de nueva partida. totalRounds={}, teamSize={}, targetScore={}", config.getTotalRounds(), config.getTeamSize(), config.getTargetScore());
        long questionCount = questionRepository.count();
        log.info("[createNewGame] Preguntas disponibles en BD: {}", questionCount);
        if (questionCount < config.getTotalRounds()) {
            log.warn("[createNewGame] Preguntas insuficientes: hay {}, se necesitan {}", questionCount, config.getTotalRounds());
            throw new InsufficientQuestionsException(
                    "Se necesitan al menos " + config.getTotalRounds() + " preguntas. Hay: " + questionCount);
        }

        int[] multipliers = config.getMultipliers();
        if (multipliers == null || multipliers.length == 0) {
            multipliers = new int[]{1, 1, 2, 2, 3};
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
        game.setTargetScore(config.getTargetScore());
        game.setTimerEnabled(config.isTimerEnabled());
        game.setTurnTimeLimit(config.getTurnTimeLimit());
        game.setCurrentMultiplier(multipliers[0]);
        game.setRoundMultipliers(multipliers);
        game.setControllingTeam(null);
        game = gameRepository.save(game);
        log.info("[createNewGame] Partida guardada con ID={}, status={}, targetScore={}", game.getId(), game.getStatus(), game.getTargetScore());

        List<Question> allQuestions = questionRepository.findAll();
        Collections.shuffle(allQuestions);
        List<Question> selected = allQuestions.subList(0, config.getTotalRounds());

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
        log.info("[createNewGame] Partida {} creada exitosamente con {} preguntas asignadas", game.getId(), gameQuestions.size());
        return toGameDTO(game);
    }

    public GameDTO getGameById(Long gameId) {
        log.debug("[getGameById] gameId={}", gameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));
        log.debug("[getGameById] gameId={}, status={}, roundStatus={}, controllingTeam={}, currentGameQuestionId={}",
                gameId, game.getStatus(), game.getCurrentRoundStatus(), game.getControllingTeam(),
                game.getCurrentGameQuestion() != null ? game.getCurrentGameQuestion().getId() : "null");
        return toGameDTO(game);
    }

    public GameQuestionDTO getGameQuestion(Long gameId, Long questionId) {
        log.info("[getGameQuestion] gameId={}, questionId={}", gameId, questionId);
        GameQuestion gameQuestion = gameQuestionRepository.findById(questionId)
                .orElseThrow(() -> new GameQuestionNotFoundException("Pregunta no encontrada: " + questionId));
        GameQuestionDTO dto = toGameQuestionDTO(gameQuestion, gameId);
        log.info("[getGameQuestion] Pregunta cargada: text='{}', answers={}", dto.getQuestionText(), dto.getAnswers() != null ? dto.getAnswers().size() : 0);
        return dto;
    }

    @Transactional
    public GameDTO startNextRound(Long gameId) {
        log.info("[startNextRound] INICIO - gameId={}", gameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));
        log.info("[startNextRound] Juego actual: status={}, roundStatus={}, roundsPlayed={}, totalRounds={}, currentGameQuestionId={}, controllingTeam={}",
                game.getStatus(), game.getCurrentRoundStatus(), game.getRoundsPlayed(), game.getTotalRounds(),
                game.getCurrentGameQuestion() != null ? game.getCurrentGameQuestion().getId() : "null",
                game.getControllingTeam());

        if (game.getStatus() == GameStatus.FINISHED) {
            log.warn("[startNextRound] Partida ya terminada, gameId={}", gameId);
            throw new GameAlreadyFinishedException("La partida ya terminó");
        }

        if (game.getCurrentRoundStatus() != GameRoundStatus.NOT_STARTED &&
                game.getCurrentRoundStatus() != GameRoundStatus.FINISHED) {
            log.warn("[startNextRound] Ronda actual no ha terminado: roundStatus={}, gameId={}", game.getCurrentRoundStatus(), gameId);
            throw new IllegalStateException("La ronda actual no ha terminado");
        }

        List<GameQuestion> gameQuestions = game.getGameQuestions();
        log.info("[startNextRound] Total gameQuestions: {}", gameQuestions.size());

        if (game.getCurrentGameQuestion() == null) {
            log.info("[startNextRound] currentGameQuestion es null, asignando primera pregunta (index=0)");
            game.setCurrentGameQuestion(gameQuestions.get(0));
        } else {
            int currentIndex = -1;
            for (int i = 0; i < gameQuestions.size(); i++) {
                if (gameQuestions.get(i).getId().equals(game.getCurrentGameQuestion().getId())) {
                    currentIndex = i;
                    break;
                }
            }
            log.info("[startNextRound] currentIndex={}, avanzando a siguiente pregunta", currentIndex);
            if (currentIndex + 1 < gameQuestions.size()) {
                game.setCurrentGameQuestion(gameQuestions.get(currentIndex + 1));
                log.info("[startNextRound] Nueva pregunta asignada: gameQuestionId={}", gameQuestions.get(currentIndex + 1).getId());
            } else {
                log.info("[startNextRound] No hay mas preguntas. Verificando si hay ganador o muerte subita. gameId={}", gameId);
                if (game.getTeam1Score() >= game.getTargetScore() || game.getTeam2Score() >= game.getTargetScore()) {
                    game.setStatus(GameStatus.FINISHED);
                    game.setCurrentRoundStatus(GameRoundStatus.FINISHED);
                    gameRepository.save(game);
                    eventPublisher.publishEvent(new GameEvent("GAME_FINISHED", gameId));
                    return toGameDTO(game);
                } else {
                    game.setStatus(GameStatus.SUDDEN_DEATH);
                    game.setCurrentRoundStatus(GameRoundStatus.SUDDEN_DEATH_FACE_OFF);
                    gameRepository.save(game);
                    eventPublisher.publishEvent(new GameEvent("SUDDEN_DEATH_STARTED", gameId));
                    log.info("[startNextRound] SUDDEN_DEATH activado! team1Score={}, team2Score={}", game.getTeam1Score(), game.getTeam2Score());
                    return toGameDTO(game);
                }
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
        game.setCurrentTurnIndex(0);
        gameRepository.save(game);
        eventPublisher.publishEvent(new GameEvent("ROUND_STARTED", gameId));
        log.info("[startNextRound] Ronda {} iniciada. controllingTeam={}, multiplier={}, status={}, roundStatus={}, questionId={}",
                newRound, controllingTeam, game.getCurrentMultiplier(), game.getStatus(), game.getCurrentRoundStatus(),
                game.getCurrentGameQuestion() != null ? game.getCurrentGameQuestion().getId() : "null");

        return toGameDTO(game);
    }

    @Transactional
    public GameQuestionDTO submitAnswer(Long gameId, RoundDTO roundDTO) {
        log.info("[submitAnswer] INICIO - gameId={}, participantId={}, gameQuestionId={}, answerText='{}', multiplier={}",
                gameId, roundDTO.getParticipantId(), roundDTO.getGameQuestionId(), roundDTO.getAnswerText(), roundDTO.getRoundMultiplier());

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));
        log.info("[submitAnswer] Game state: status={}, roundStatus={}, controllingTeam={}, team1Score={}, team2Score={}, team1Errors={}, team2Errors={}, roundPoints={}",
                game.getStatus(), game.getCurrentRoundStatus(), game.getControllingTeam(),
                game.getTeam1Score(), game.getTeam2Score(), game.getTeam1Errors(), game.getTeam2Errors(), game.getCurrentRoundPoints());

        if (game.getStatus() == GameStatus.FINISHED) {
            log.warn("[submitAnswer] Partida ya terminada, gameId={}", gameId);
            throw new GameAlreadyFinishedException("La partida ya terminó");
        }

        Participant participant = participantRepository.findById(roundDTO.getParticipantId())
                .orElseThrow(() -> new ParticipantNotFoundException(
                        "Participante no encontrado: " + roundDTO.getParticipantId()));
        log.info("[submitAnswer] Participante encontrado: id={}, name='{}', team={}", participant.getId(), participant.getName(), participant.getTeam());

        GameQuestion gameQuestion = gameQuestionRepository.findById(roundDTO.getGameQuestionId())
                .orElseThrow(() -> new GameQuestionNotFoundException(
                        "Pregunta no encontrada en la partida"));
        log.info("[submitAnswer] GameQuestion encontrada: id={}, questionText='{}'", gameQuestion.getId(), gameQuestion.getQuestion().getText());

        Answer answer = answerRepository.findByQuestionIdAndTextIgnoreCase(
                gameQuestion.getQuestion().getId(), roundDTO.getAnswerText());
        log.info("[submitAnswer] Busqueda de respuesta en BD: questionId={}, searchText='{}', encontrada={}",
                gameQuestion.getQuestion().getId(), roundDTO.getAnswerText(), answer != null);

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
            log.info("[submitAnswer] RESPUESTA CORRECTA! answerText='{}', baseScore={}, multiplier={}, totalPoints={}",
                    answer.getText(), answer.getScore(), multiplier, points);
            handleCorrectAnswer(game, points, participant.getTeam());
            eventPublisher.publishEvent(new GameEvent("ANSWER_CORRECT", gameId));
        } else {
            round.setScore(0);
            round.setCorrect(false);
            log.info("[submitAnswer] RESPUESTA INCORRECTA: '{}'", roundDTO.getAnswerText());
            handleIncorrectAnswer(game, participant.getTeam());
            eventPublisher.publishEvent(new GameEvent("ANSWER_WRONG", gameId));
        }

        log.info("[submitAnswer] Estado DESPUES de respuesta: team1Score={}, team2Score={}, team1Errors={}, team2Errors={}, roundStatus={}, roundPoints={}",
                game.getTeam1Score(), game.getTeam2Score(), game.getTeam1Errors(), game.getTeam2Errors(),
                game.getCurrentRoundStatus(), game.getCurrentRoundPoints());

        advanceToNextPlayer(game, participant.getTeam());

        gameRoundRepository.save(round);

        if (checkForEarlyWin(game)) {
            gameRepository.save(game);
            eventPublisher.publishEvent(new GameEvent("GAME_FINISHED", gameId));
        } else {
            gameRepository.save(game);
        }

        return toGameQuestionDTO(gameQuestion, gameId);
    }

    private void handleCorrectAnswer(Game game, int points, int team) {
        log.info("[handleCorrectAnswer] team={}, points={}, roundStatus={}, roundPoints={}", team, points, game.getCurrentRoundStatus(), game.getCurrentRoundPoints());
        if (game.getCurrentRoundStatus() == GameRoundStatus.STEAL_ATTEMPT) {
            int stealingTeam = (game.getControllingTeam() == 1) ? 2 : 1;
            if (team == stealingTeam) {
                int totalStolen = game.getCurrentRoundPoints() + points;
                if (stealingTeam == 1) {
                    game.setTeam1Score(game.getTeam1Score() + totalStolen);
                } else {
                    game.setTeam2Score(game.getTeam2Score() + totalStolen);
                }
                game.setCurrentRoundPoints(0);
                game.setCurrentRoundStatus(GameRoundStatus.FINISHED);
                log.info("[handleCorrectAnswer] Robo exitoso! Equipo {} roba {} puntos.", stealingTeam, totalStolen);
            } else {
                log.info("[handleCorrectAnswer] Equipo {} no es el equipo que roba (esperado {}). Ignorando.", team, stealingTeam);
            }
        } else {
            game.setCurrentRoundPoints(game.getCurrentRoundPoints() + points);
            log.info("[handleCorrectAnswer] Equipo {} suma {} puntos en ronda. roundPoints={}", team, points, game.getCurrentRoundPoints());
        }
    }

    private void handleIncorrectAnswer(Game game, int team) {
        log.info("[handleIncorrectAnswer] team={}, roundStatus={}, team1Errors={}, team2Errors={}", team, game.getCurrentRoundStatus(), game.getTeam1Errors(), game.getTeam2Errors());
        if (game.getCurrentRoundStatus() == GameRoundStatus.STEAL_ATTEMPT) {
            if (team == 1) {
                game.setTeam2Score(game.getTeam2Score() + game.getCurrentRoundPoints());
                log.info("[handleIncorrectAnswer] Robo fallido! Equipo 2 recibe {} puntos. score={}", game.getCurrentRoundPoints(), game.getTeam2Score());
            } else {
                game.setTeam1Score(game.getTeam1Score() + game.getCurrentRoundPoints());
                log.info("[handleIncorrectAnswer] Robo fallido! Equipo 1 recibe {} puntos. score={}", game.getCurrentRoundPoints(), game.getTeam1Score());
            }
            game.setCurrentRoundPoints(0);
            game.setCurrentRoundStatus(GameRoundStatus.FINISHED);
        } else if (game.getCurrentRoundStatus() == GameRoundStatus.TURN_PLAYER1) {
            game.setTeam1Errors(game.getTeam1Errors() + 1);
            log.info("[handleIncorrectAnswer] Equipo 1 error #{}", game.getTeam1Errors());
            if (game.getTeam1Errors() >= 3) {
                game.setCurrentRoundStatus(GameRoundStatus.STEAL_ATTEMPT);
                log.info("[handleIncorrectAnswer] Equipo 1 acumula 3 errores -> STEAL_ATTEMPT");
            }
        } else if (game.getCurrentRoundStatus() == GameRoundStatus.TURN_PLAYER2) {
            game.setTeam2Errors(game.getTeam2Errors() + 1);
            log.info("[handleIncorrectAnswer] Equipo 2 error #{}", game.getTeam2Errors());
            if (game.getTeam2Errors() >= 3) {
                game.setCurrentRoundStatus(GameRoundStatus.STEAL_ATTEMPT);
                log.info("[handleIncorrectAnswer] Equipo 2 acumula 3 errores -> STEAL_ATTEMPT");
            }
        }
    }

    @Transactional
    public GameDTO passTurn(Long gameId) {
        log.info("[passTurn] INICIO - gameId={}", gameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));
        log.info("[passTurn] Estado actual: roundStatus={}, controllingTeam={}", game.getCurrentRoundStatus(), game.getControllingTeam());

        if (game.getCurrentRoundStatus() != GameRoundStatus.TURN_PLAYER1 &&
                game.getCurrentRoundStatus() != GameRoundStatus.TURN_PLAYER2) {
            log.warn("[passTurn] No se puede pasar turno desde roundStatus={}", game.getCurrentRoundStatus());
            throw new IllegalStateException("Solo se puede pasar el turno durante un turno activo");
        }

        game.setCurrentRoundStatus(GameRoundStatus.STEAL_ATTEMPT);
        gameRepository.save(game);
        eventPublisher.publishEvent(new GameEvent("TURN_PASSED", gameId));
        log.info("[passTurn] Turno pasado -> STEAL_ATTEMPT. Equipo {} puede robar {} puntos", game.getControllingTeam() == 1 ? 2 : 1, game.getCurrentRoundPoints());
        return toGameDTO(game);
    }

    @Transactional
    public GameDTO incrementError(Long gameId) {
        log.info("[incrementError] INICIO - gameId={}", gameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));
        log.info("[incrementError] Estado actual: roundStatus={}, controllingTeam={}, t1Errors={}, t2Errors={}",
                game.getCurrentRoundStatus(), game.getControllingTeam(), game.getTeam1Errors(), game.getTeam2Errors());

        if (game.getCurrentRoundStatus() != GameRoundStatus.TURN_PLAYER1 &&
                game.getCurrentRoundStatus() != GameRoundStatus.TURN_PLAYER2) {
            log.warn("[incrementError] No se puede registrar error desde roundStatus={}", game.getCurrentRoundStatus());
            throw new IllegalStateException("Solo se puede registrar error durante un turno activo");
        }

        if (game.getCurrentRoundStatus() == GameRoundStatus.TURN_PLAYER1) {
            game.setTeam1Errors(game.getTeam1Errors() + 1);
            log.info("[incrementError] Equipo 1 error #{}/3", game.getTeam1Errors());
            if (game.getTeam1Errors() >= 3) {
                game.setCurrentRoundStatus(GameRoundStatus.STEAL_ATTEMPT);
                log.info("[incrementError] Equipo 1 acumula 3 errores -> STEAL_ATTEMPT");
                eventPublisher.publishEvent(new GameEvent("STEAL_ATTEMPT", gameId));
            }
        } else if (game.getCurrentRoundStatus() == GameRoundStatus.TURN_PLAYER2) {
            game.setTeam2Errors(game.getTeam2Errors() + 1);
            log.info("[incrementError] Equipo 2 error #{}/3", game.getTeam2Errors());
            if (game.getTeam2Errors() >= 3) {
                game.setCurrentRoundStatus(GameRoundStatus.STEAL_ATTEMPT);
                log.info("[incrementError] Equipo 2 acumula 3 errores -> STEAL_ATTEMPT");
                eventPublisher.publishEvent(new GameEvent("STEAL_ATTEMPT", gameId));
            }
        }

        gameRepository.save(game);
        if (game.getCurrentRoundStatus() != GameRoundStatus.STEAL_ATTEMPT) {
            eventPublisher.publishEvent(new GameEvent("ANSWER_WRONG", gameId));
        }
        log.info("[incrementError] FIN - t1Errors={}, t2Errors={}, roundStatus={}", game.getTeam1Errors(), game.getTeam2Errors(), game.getCurrentRoundStatus());
        return toGameDTO(game);
    }

    @Transactional
    public GameDTO endRound(Long gameId) {
        log.info("[endRound] INICIO - gameId={}", gameId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));
        log.info("[endRound] Estado actual: roundStatus={}, controllingTeam={}, roundPoints={}, team1Score={}, team2Score={}",
                game.getCurrentRoundStatus(), game.getControllingTeam(), game.getCurrentRoundPoints(), game.getTeam1Score(), game.getTeam2Score());

        if (game.getCurrentRoundStatus() == GameRoundStatus.FINISHED) {
            log.warn("[endRound] Ronda ya terminada, gameId={}", gameId);
            throw new IllegalStateException("La ronda ya terminó");
        }

        if (game.getControllingTeam() != null) {
            if (game.getControllingTeam() == 1) {
                game.setTeam1Score(game.getTeam1Score() + game.getCurrentRoundPoints());
                log.info("[endRound] Equipo 1 recibe {} puntos. Nuevo score={}", game.getCurrentRoundPoints(), game.getTeam1Score());
            } else {
                game.setTeam2Score(game.getTeam2Score() + game.getCurrentRoundPoints());
                log.info("[endRound] Equipo 2 recibe {} puntos. Nuevo score={}", game.getCurrentRoundPoints(), game.getTeam2Score());
            }
        } else {
            log.warn("[endRound] controllingTeam es null! No se asignan puntos de ronda. roundPoints={}", game.getCurrentRoundPoints());
        }

        game.setCurrentRoundStatus(GameRoundStatus.FINISHED);
        game.setCurrentRoundPoints(0);

        if (checkForEarlyWin(game)) {
            gameRepository.save(game);
            eventPublisher.publishEvent(new GameEvent("ROUND_ENDED", gameId));
            eventPublisher.publishEvent(new GameEvent("GAME_FINISHED", gameId));
        } else {
            gameRepository.save(game);
            eventPublisher.publishEvent(new GameEvent("ROUND_ENDED", gameId));
        }
        log.info("[endRound] Ronda finalizada. team1Score={}, team2Score={}", game.getTeam1Score(), game.getTeam2Score());

        return toGameDTO(game);
    }

    @Transactional
    public GameQuestionDTO revealAnswer(Long gameId, Long answerId) {
        log.info("[revealAnswer] INICIO - gameId={}, answerId={}", gameId, answerId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));
        log.info("[revealAnswer] Game state: status={}, roundStatus={}, controllingTeam={}, roundPoints={}, t1={}, t2={}",
                game.getStatus(), game.getCurrentRoundStatus(), game.getControllingTeam(), game.getCurrentRoundPoints(), game.getTeam1Score(), game.getTeam2Score());

        if (game.getCurrentGameQuestion() == null) {
            log.warn("[revealAnswer] No hay pregunta activa en gameId={}", gameId);
            throw new IllegalStateException("No hay pregunta activa en esta partida");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Respuesta no encontrada: " + answerId));

        log.info("[revealAnswer] Respuesta encontrada: id={}, text='{}', score={}", answer.getId(), answer.getText(), answer.getScore());

        GameQuestion gameQuestion = game.getCurrentGameQuestion();

        List<GameRound> existingRounds = gameRoundRepository.findByGameIdAndGameQuestionId(gameId, gameQuestion.getId());
        boolean alreadyRevealed = existingRounds.stream()
                .filter(GameRound::isCorrect)
                .anyMatch(r -> r.getAnswerText().equalsIgnoreCase(answer.getText()));
        if (alreadyRevealed) {
            log.warn("[revealAnswer] Respuesta '{}' ya fue revelada previamente", answer.getText());
            GameQuestionDTO dto = toGameQuestionDTO(gameQuestion, gameId);
            return dto;
        }

        int multiplier = game.getCurrentMultiplier();
        int points = answer.getScore() * multiplier;
        log.info("[revealAnswer] Puntos calculados: baseScore={}, multiplier={}, totalPoints={}", answer.getScore(), multiplier, points);

        GameRound round = new GameRound();
        round.setGame(game);
        round.setGameQuestion(gameQuestion);
        round.setAnswerText(answer.getText());
        round.setScore(points);
        round.setCorrect(true);
        round.setMultiplier(multiplier);

        if (game.getCurrentRoundStatus() == GameRoundStatus.STEAL_ATTEMPT) {
            int stealingTeam = (game.getControllingTeam() == 1) ? 2 : 1;
            int totalStolen = game.getCurrentRoundPoints() + points;
            log.info("[revealAnswer] STEAL_ATTEMPT! Equipo {} roba {} puntos (acumulados {}) + {} (nueva respuesta)",
                    stealingTeam, totalStolen, game.getCurrentRoundPoints(), points);

            Participant stealParticipant = participantRepository.findByGameId(gameId).stream()
                    .filter(p -> p.getTeam() == stealingTeam)
                    .findFirst().orElse(null);
            if (stealParticipant != null) {
                round.setParticipant(stealParticipant);
            }
            gameRoundRepository.save(round);

            if (stealingTeam == 1) {
                game.setTeam1Score(game.getTeam1Score() + totalStolen);
            } else {
                game.setTeam2Score(game.getTeam2Score() + totalStolen);
            }
            game.setCurrentRoundPoints(0);
            game.setCurrentRoundStatus(GameRoundStatus.FINISHED);

            if (checkForEarlyWin(game)) {
                gameRepository.save(game);
                eventPublisher.publishEvent(new GameEvent("ANSWER_CORRECT", gameId));
                eventPublisher.publishEvent(new GameEvent("ROUND_ENDED", gameId));
                eventPublisher.publishEvent(new GameEvent("GAME_FINISHED", gameId));
            } else {
                gameRepository.save(game);
                eventPublisher.publishEvent(new GameEvent("ANSWER_CORRECT", gameId));
                eventPublisher.publishEvent(new GameEvent("ROUND_ENDED", gameId));
            }

            GameQuestionDTO dto = toGameQuestionDTO(gameQuestion, gameId);
            log.info("[revealAnswer] ROBO EXITOSO! Equipo {} robó {} puntos. t1={}, t2={}", stealingTeam, totalStolen, game.getTeam1Score(), game.getTeam2Score());
            return dto;
        }

        Participant controllerParticipant = participantRepository.findByGameId(gameId).stream()
                .filter(p -> p.getTeam() == game.getControllingTeam())
                .findFirst().orElse(null);
        if (controllerParticipant != null) {
            round.setParticipant(controllerParticipant);
        }
        gameRoundRepository.save(round);

        game.setCurrentRoundPoints(game.getCurrentRoundPoints() + points);
        log.info("[revealAnswer] Puntos de ronda actualizados: roundPoints={}", game.getCurrentRoundPoints());
        gameRepository.save(game);

        eventPublisher.publishEvent(new GameEvent("ANSWER_CORRECT", gameId));

        GameQuestionDTO dto = toGameQuestionDTO(gameQuestion, gameId);
        log.info("[revealAnswer] FIN - answerId={}, text='{}', revealed=true, points={}", answerId, answer.getText(), points);
        return dto;
    }

    public GameResultsDTO getFinalResults(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        List<Participant> participants = participantRepository.findByGameId(gameId);
        List<ParticipantDTO> participantDTOs = participants.stream()
                .map(p -> {
                    ParticipantDTO dto = participantMapper.toDTO(p);
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
                .map(this::toGameDTO)
                .toList();
    }

    @Transactional
    public ParticipantDTO addParticipant(Long gameId, CreateParticipantDTO dto) {
        log.info("[addParticipant] gameId={}, name='{}', team={}", gameId, dto.getName(), dto.getTeam());
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getStatus() != GameStatus.NOT_STARTED) {
            log.warn("[addParticipant] No se pueden agregar participantes a partida con status={}", game.getStatus());
            throw new IllegalStateException("No se pueden agregar participantes a una partida ya iniciada");
        }

        long teamCount = participantRepository.findByGameId(gameId).stream()
                .filter(p -> p.getTeam() == dto.getTeam())
                .count();
        if (teamCount >= game.getTeamSize()) {
            log.warn("[addParticipant] Equipo {} ya tiene {} miembros (maximo {})", dto.getTeam(), teamCount, game.getTeamSize());
            throw new IllegalStateException("El equipo " + dto.getTeam() + " ya tiene " + game.getTeamSize() + " miembros (máximo)");
        }

        Participant participant = new Participant();
        participant.setGame(game);
        participant.setName(dto.getName());
        participant.setTeam(dto.getTeam());
        participant.setMemberOrder((int) teamCount);
        participant = participantRepository.save(participant);
        log.info("[addParticipant] Participante creado: id={}, name='{}', team={}, order={}", participant.getId(), participant.getName(), participant.getTeam(), participant.getMemberOrder());

        ParticipantDTO result = participantMapper.toDTO(participant);
        result.setScore(0);
        return result;
    }

    public List<ParticipantDTO> getParticipants(Long gameId) {
        List<ParticipantDTO> participants = participantRepository.findByGameId(gameId).stream()
                .map(p -> {
                    ParticipantDTO dto = participantMapper.toDTO(p);
                    dto.setScore(gameRoundRepository.sumScoreByParticipantId(p.getId()));
                    return dto;
                })
                .toList();
        log.info("[getParticipants] gameId={}, total={}, teams: T1={}, T2={}",
                gameId, participants.size(),
                participants.stream().filter(p -> p.getTeam() == 1).count(),
                participants.stream().filter(p -> p.getTeam() == 2).count());
        return participants;
    }

    private GameDTO toGameDTO(Game game) {
        GameDTO dto = gameMapper.toDTO(game);
        if (game.getStatus() == GameStatus.FINISHED) {
            if (game.getTeam1Score() > game.getTeam2Score()) {
                dto.setWinner("team1");
            } else if (game.getTeam2Score() > game.getTeam1Score()) {
                dto.setWinner("team2");
            } else {
                dto.setWinner("draw");
            }
        }
        if (game.getCurrentGameQuestion() != null) {
            dto.setCurrentAnswers(getCurrentAnswersForQuestion(game));
        }
        return dto;
    }

    private List<AnswerDTO> getCurrentAnswersForQuestion(Game game) {
        if (game.getCurrentGameQuestion() == null) {
            return List.of();
        }
        return toGameQuestionDTO(game.getCurrentGameQuestion(), game.getId()).getAnswers();
    }

    private GameQuestionDTO toGameQuestionDTO(GameQuestion gameQuestion, Long gameId) {
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
        return gameQuestionMapper.toDTOWithRevealed(gameQuestion, revealedIds);
    }

    private int[] getMultipliersForGame(Game game) {
        return game.getRoundMultipliers();
    }

    private boolean checkForEarlyWin(Game game) {
        int target = game.getTargetScore();
        if (game.getTeam1Score() >= target || game.getTeam2Score() >= target) {
            game.setStatus(GameStatus.FINISHED);
            game.setCurrentRoundStatus(GameRoundStatus.FINISHED);
            log.info("[checkForEarlyWin] Equipo alcanzo targetScore={}. team1Score={}, team2Score={}. Juego terminado.",
                    target, game.getTeam1Score(), game.getTeam2Score());
            return true;
        }
        return false;
    }

    private void advanceToNextPlayer(Game game, int team) {
        if (game.getCurrentRoundStatus() == GameRoundStatus.STEAL_ATTEMPT ||
                game.getCurrentRoundStatus() == GameRoundStatus.FINISHED) {
            return;
        }

        List<Participant> teamMembers = participantRepository.findByGameId(game.getId()).stream()
                .filter(p -> p.getTeam() == team)
                .sorted((a, b) -> Integer.compare(a.getMemberOrder(), b.getMemberOrder()))
                .toList();

        if (teamMembers.isEmpty()) {
            return;
        }

        int nextIndex = (game.getCurrentTurnIndex() + 1) % teamMembers.size();
        game.setCurrentTurnIndex(nextIndex);
        log.info("[advanceToNextPlayer] Equipo {} avanza al siguiente jugador. currentTurnIndex={}", team, nextIndex);
    }

    @Transactional
    public void setCaptain(Long gameId, Long participantId) {
        log.info("[setCaptain] gameId={}, participantId={}", gameId, participantId);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getStatus() != GameStatus.NOT_STARTED) {
            throw new IllegalStateException("Solo se puede asignar capitán antes de iniciar la partida");
        }

        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ParticipantNotFoundException("Participante no encontrado: " + participantId));

        if (!participant.getGame().getId().equals(gameId)) {
            throw new IllegalStateException("El participante no pertenece a esta partida");
        }

        participantRepository.findByGameId(gameId).stream()
                .filter(p -> p.getTeam() == participant.getTeam())
                .forEach(p -> p.setCaptain(false));

        participant.setCaptain(true);
        participantRepository.save(participant);
        log.info("[setCaptain] {} designado como capitán del Equipo {}", participant.getName(), participant.getTeam());
    }

    @Transactional
    public GameDTO startFaceOff(Long gameId, Long player1Id, Long player2Id) {
        log.info("[startFaceOff] gameId={}, player1Id={}, player2Id={}", gameId, player1Id, player2Id);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("La partida debe estar en progreso para iniciar face-off");
        }

        if (game.getCurrentRoundStatus() != GameRoundStatus.NOT_STARTED &&
                game.getCurrentRoundStatus() != GameRoundStatus.FINISHED) {
            throw new IllegalStateException("No se puede iniciar face-off durante una ronda activa");
        }

        Participant p1 = participantRepository.findById(player1Id)
                .orElseThrow(() -> new ParticipantNotFoundException("Jugador 1 no encontrado: " + player1Id));
        Participant p2 = participantRepository.findById(player2Id)
                .orElseThrow(() -> new ParticipantNotFoundException("Jugador 2 no encontrado: " + player2Id));

        if (p1.getTeam() == p2.getTeam()) {
            throw new IllegalStateException("Los jugadores deben ser de equipos diferentes");
        }

        game.setFaceOffPlayer1(player1Id);
        game.setFaceOffPlayer2(player2Id);
        game.setCurrentRoundStatus(GameRoundStatus.FACE_OFF);
        gameRepository.save(game);

        eventPublisher.publishEvent(new GameEvent("FACE_OFF_STARTED", gameId));
        log.info("[startFaceOff] Face-off iniciado entre {} (Equipo {}) y {} (Equipo {})",
                p1.getName(), p1.getTeam(), p2.getName(), p2.getTeam());

        return toGameDTO(game);
    }

    @Transactional
    public GameDTO buzzIn(Long gameId, Long participantId, String answerText) {
        log.info("[buzzIn] gameId={}, participantId={}, answerText='{}'", gameId, participantId, answerText);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getCurrentRoundStatus() != GameRoundStatus.FACE_OFF) {
            throw new IllegalStateException("No hay face-off activo");
        }

        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ParticipantNotFoundException("Participante no encontrado: " + participantId));

        if (!participant.getId().equals(game.getFaceOffPlayer1()) && !participant.getId().equals(game.getFaceOffPlayer2())) {
            throw new IllegalStateException("Solo los jugadores del face-off pueden responder");
        }

        GameQuestion gameQuestion = game.getCurrentGameQuestion();
        if (gameQuestion == null) {
            throw new IllegalStateException("No hay pregunta activa");
        }

        Answer answer = answerRepository.findByQuestionIdAndTextIgnoreCase(
                gameQuestion.getQuestion().getId(), answerText);

        if (answer != null) {
            List<Answer> allAnswers = gameQuestion.getQuestion().getAnswers();
            int answerRank = -1;
            for (int i = 0; i < allAnswers.size(); i++) {
                if (allAnswers.get(i).getId().equals(answer.getId())) {
                    answerRank = i;
                    break;
                }
            }

            if (answerRank == 0) {
                int controllingTeam = participant.getTeam();
                game.setControllingTeam(controllingTeam);
                game.setCurrentRoundStatus(
                        controllingTeam == 1 ? GameRoundStatus.TURN_PLAYER1 : GameRoundStatus.TURN_PLAYER2
                );
                game.setCurrentTurnIndex(0);
                log.info("[buzzIn] RESPUESTA #1! Equipo {} toma control", controllingTeam);
            } else {
                int otherTeam = participant.getTeam() == 1 ? 2 : 1;
                game.setControllingTeam(otherTeam);
                game.setCurrentRoundStatus(
                        otherTeam == 1 ? GameRoundStatus.TURN_PLAYER1 : GameRoundStatus.TURN_PLAYER2
                );
                game.setCurrentTurnIndex(0);
                log.info("[buzzIn] Respuesta rank={} (no es #1). Equipo {} toma control", answerRank + 1, otherTeam);
            }

            int multiplier = game.getCurrentMultiplier();
            int points = answer.getScore() * multiplier;
            game.setCurrentRoundPoints(points);

            GameRound round = new GameRound();
            round.setGame(game);
            round.setParticipant(participant);
            round.setGameQuestion(gameQuestion);
            round.setAnswerText(answerText);
            round.setScore(points);
            round.setCorrect(true);
            round.setMultiplier(multiplier);
            gameRoundRepository.save(round);

            eventPublisher.publishEvent(new GameEvent("ANSWER_CORRECT", gameId));
        } else {
            log.info("[buzzIn] RESPUESTA INCORRECTA: '{}'. Face-off continúa", answerText);

            GameRound round = new GameRound();
            round.setGame(game);
            round.setParticipant(participant);
            round.setGameQuestion(gameQuestion);
            round.setAnswerText(answerText);
            round.setScore(0);
            round.setCorrect(false);
            round.setMultiplier(game.getCurrentMultiplier());
            gameRoundRepository.save(round);

            eventPublisher.publishEvent(new GameEvent("ANSWER_WRONG", gameId));
        }

        game.setFaceOffPlayer1(null);
        game.setFaceOffPlayer2(null);
        gameRepository.save(game);

        return toGameDTO(game);
    }

    @Transactional
    public GameDTO suddenDeathFaceOff(Long gameId, Long player1Id, Long player2Id) {
        log.info("[suddenDeathFaceOff] gameId={}, player1Id={}, player2Id={}", gameId, player1Id, player2Id);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getStatus() != GameStatus.SUDDEN_DEATH) {
            throw new IllegalStateException("La partida no está en Muerte Súbita");
        }

        Participant p1 = participantRepository.findById(player1Id)
                .orElseThrow(() -> new ParticipantNotFoundException("Jugador 1 no encontrado: " + player1Id));
        Participant p2 = participantRepository.findById(player2Id)
                .orElseThrow(() -> new ParticipantNotFoundException("Jugador 2 no encontrado: " + player2Id));

        game.setFaceOffPlayer1(player1Id);
        game.setFaceOffPlayer2(player2Id);
        game.setCurrentRoundStatus(GameRoundStatus.SUDDEN_DEATH_FACE_OFF);
        gameRepository.save(game);

        eventPublisher.publishEvent(new GameEvent("SUDDEN_DEATH_FACE_OFF", gameId));
        log.info("[suddenDeathFaceOff] Face-off de muerte subita iniciado entre {} y {}", p1.getName(), p2.getName());

        return toGameDTO(game);
    }

    @Transactional
    public GameDTO suddenDeathBuzzIn(Long gameId, Long participantId, String answerText) {
        log.info("[suddenDeathBuzzIn] gameId={}, participantId={}, answerText='{}'", gameId, participantId, answerText);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getStatus() != GameStatus.SUDDEN_DEATH) {
            throw new IllegalStateException("La partida no está en Muerte Súbita");
        }

        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ParticipantNotFoundException("Participante no encontrado: " + participantId));

        GameQuestion gameQuestion = game.getCurrentGameQuestion();
        if (gameQuestion == null) {
            throw new IllegalStateException("No hay pregunta activa");
        }

        Answer answer = answerRepository.findByQuestionIdAndTextIgnoreCase(
                gameQuestion.getQuestion().getId(), answerText);

        if (answer != null) {
            int controllingTeam = participant.getTeam();
            game.setControllingTeam(controllingTeam);
            game.setCurrentRoundStatus(
                    controllingTeam == 1 ? GameRoundStatus.TURN_PLAYER1 : GameRoundStatus.TURN_PLAYER2
            );
            game.setCurrentTurnIndex(0);
            game.setCurrentRoundPoints(answer.getScore() * game.getCurrentMultiplier());

            GameRound round = new GameRound();
            round.setGame(game);
            round.setParticipant(participant);
            round.setGameQuestion(gameQuestion);
            round.setAnswerText(answerText);
            round.setScore(answer.getScore() * game.getCurrentMultiplier());
            round.setCorrect(true);
            round.setMultiplier(game.getCurrentMultiplier());
            gameRoundRepository.save(round);

            eventPublisher.publishEvent(new GameEvent("ANSWER_CORRECT", gameId));
            log.info("[suddenDeathBuzzIn] Respuesta correcta! Equipo {} toma control", controllingTeam);
        } else {
            int otherTeam = participant.getTeam() == 1 ? 2 : 1;
            game.setControllingTeam(otherTeam);
            game.setCurrentRoundStatus(
                    otherTeam == 1 ? GameRoundStatus.TURN_PLAYER1 : GameRoundStatus.TURN_PLAYER2
            );
            game.setCurrentTurnIndex(0);
            game.setCurrentRoundPoints(0);

            GameRound round = new GameRound();
            round.setGame(game);
            round.setParticipant(participant);
            round.setGameQuestion(gameQuestion);
            round.setAnswerText(answerText);
            round.setScore(0);
            round.setCorrect(false);
            round.setMultiplier(game.getCurrentMultiplier());
            gameRoundRepository.save(round);

            eventPublisher.publishEvent(new GameEvent("ANSWER_WRONG", gameId));
            log.info("[suddenDeathBuzzIn] Respuesta incorrecta. Equipo {} toma control", otherTeam);
        }

        game.setFaceOffPlayer1(null);
        game.setFaceOffPlayer2(null);
        gameRepository.save(game);

        return toGameDTO(game);
    }

    @Transactional
    public GameDTO suddenDeathAnswer(Long gameId, RoundDTO roundDTO) {
        log.info("[suddenDeathAnswer] gameId={}, participantId={}, answerText='{}'", gameId, roundDTO.getParticipantId(), roundDTO.getAnswerText());
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getStatus() != GameStatus.SUDDEN_DEATH) {
            throw new IllegalStateException("La partida no está en Muerte Súbita");
        }

        Participant participant = participantRepository.findById(roundDTO.getParticipantId())
                .orElseThrow(() -> new ParticipantNotFoundException("Participante no encontrado"));

        GameQuestion gameQuestion = game.getCurrentGameQuestion();
        if (gameQuestion == null) {
            throw new IllegalStateException("No hay pregunta activa");
        }

        Answer answer = answerRepository.findByQuestionIdAndTextIgnoreCase(
                gameQuestion.getQuestion().getId(), roundDTO.getAnswerText());

        GameRound round = new GameRound();
        round.setGame(game);
        round.setParticipant(participant);
        round.setGameQuestion(gameQuestion);
        round.setAnswerText(roundDTO.getAnswerText());
        round.setMultiplier(game.getCurrentMultiplier());

        if (answer != null) {
            int points = answer.getScore() * game.getCurrentMultiplier();
            round.setScore(points);
            round.setCorrect(true);

            if (game.getCurrentRoundStatus() == GameRoundStatus.STEAL_ATTEMPT) {
                int stealingTeam = (game.getControllingTeam() == 1) ? 2 : 1;
                int totalStolen = game.getCurrentRoundPoints() + points;
                if (stealingTeam == 1) {
                    game.setTeam1Score(game.getTeam1Score() + totalStolen);
                } else {
                    game.setTeam2Score(game.getTeam2Score() + totalStolen);
                }
                game.setCurrentRoundPoints(0);
                game.setCurrentRoundStatus(GameRoundStatus.FINISHED);
                game.setStatus(GameStatus.FINISHED);

                gameRoundRepository.save(round);
                gameRepository.save(game);

                eventPublisher.publishEvent(new GameEvent("ANSWER_CORRECT", gameId));
                eventPublisher.publishEvent(new GameEvent("SUDDEN_DEATH_WON", gameId));
                eventPublisher.publishEvent(new GameEvent("GAME_FINISHED", gameId));
            } else {
                game.setCurrentRoundPoints(game.getCurrentRoundPoints() + points);
                advanceToNextPlayer(game, participant.getTeam());

                gameRoundRepository.save(round);
                gameRepository.save(game);

                eventPublisher.publishEvent(new GameEvent("ANSWER_CORRECT", gameId));
            }
        } else {
            round.setScore(0);
            round.setCorrect(false);

            if (game.getCurrentRoundStatus() == GameRoundStatus.STEAL_ATTEMPT) {
                if (game.getControllingTeam() != null) {
                    if (game.getControllingTeam() == 1) {
                        game.setTeam1Score(game.getTeam1Score() + game.getCurrentRoundPoints());
                    } else {
                        game.setTeam2Score(game.getTeam2Score() + game.getCurrentRoundPoints());
                    }
                }
                game.setCurrentRoundPoints(0);
                game.setCurrentRoundStatus(GameRoundStatus.FINISHED);
                game.setStatus(GameStatus.FINISHED);

                gameRoundRepository.save(round);
                gameRepository.save(game);

                eventPublisher.publishEvent(new GameEvent("ANSWER_WRONG", gameId));
                eventPublisher.publishEvent(new GameEvent("SUDDEN_DEATH_LOST", gameId));
                eventPublisher.publishEvent(new GameEvent("GAME_FINISHED", gameId));
            } else {
                if (participant.getTeam() == 1) {
                    game.setTeam1Errors(game.getTeam1Errors() + 1);
                } else {
                    game.setTeam2Errors(game.getTeam2Errors() + 1);
                }

                gameRoundRepository.save(round);

                int teamErrors = (participant.getTeam() == 1) ? game.getTeam1Errors() : game.getTeam2Errors();
                if (teamErrors >= 1) {
                    int stealingTeam = (game.getControllingTeam() == 1) ? 2 : 1;
                    game.setCurrentRoundStatus(GameRoundStatus.STEAL_ATTEMPT);
                    gameRepository.save(game);
                    eventPublisher.publishEvent(new GameEvent("STEAL_ATTEMPT", gameId));
                    log.info("[suddenDeathAnswer] 1 strike en muerte subita -> STEAL_ATTEMPT automático");
                } else {
                    gameRepository.save(game);
                    eventPublisher.publishEvent(new GameEvent("ANSWER_WRONG", gameId));
                }
            }
        }

        log.info("[suddenDeathAnswer] FIN - status={}, roundStatus={}, t1={}, t2={}",
                game.getStatus(), game.getCurrentRoundStatus(), game.getTeam1Score(), game.getTeam2Score());
        return toGameDTO(game);
    }
}
