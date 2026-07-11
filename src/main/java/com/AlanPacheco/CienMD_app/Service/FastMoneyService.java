package com.AlanPacheco.CienMD_app.Service;

import com.AlanPacheco.CienMD_app.DTO.FastMoneyDTO;
import com.AlanPacheco.CienMD_app.DTO.FastMoneySubmissionDTO;
import com.AlanPacheco.CienMD_app.Entity.Answer;
import com.AlanPacheco.CienMD_app.Entity.FastMoneyRound;
import com.AlanPacheco.CienMD_app.Entity.Game;
import com.AlanPacheco.CienMD_app.Entity.GameQuestion;
import com.AlanPacheco.CienMD_app.Entity.Participant;
import com.AlanPacheco.CienMD_app.Entity.Question;
import com.AlanPacheco.CienMD_app.Enum.GameStatus;
import com.AlanPacheco.CienMD_app.Exception.GameNotFoundException;
import com.AlanPacheco.CienMD_app.Exception.ParticipantNotFoundException;
import com.AlanPacheco.CienMD_app.Mapper.FastMoneyAnswerMapper;
import com.AlanPacheco.CienMD_app.Repository.AnswerRepository;
import com.AlanPacheco.CienMD_app.Repository.FastMoneyRoundRepository;
import com.AlanPacheco.CienMD_app.Repository.GameRepository;
import com.AlanPacheco.CienMD_app.Repository.ParticipantRepository;
import com.AlanPacheco.CienMD_app.Repository.QuestionRepository;
import com.AlanPacheco.CienMD_app.Config.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FastMoneyService {

    private static final Logger log = LoggerFactory.getLogger(FastMoneyService.class);
    private static final int BONUS_TARGET = 200;
    private static final int QUESTIONS_COUNT = 5;

    private final GameRepository gameRepository;
    private final ParticipantRepository participantRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final FastMoneyRoundRepository fastMoneyRoundRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final FastMoneyAnswerMapper fastMoneyAnswerMapper;

    public FastMoneyService(GameRepository gameRepository, ParticipantRepository participantRepository,
                           QuestionRepository questionRepository, AnswerRepository answerRepository,
                           FastMoneyRoundRepository fastMoneyRoundRepository,
                           ApplicationEventPublisher eventPublisher,
                           FastMoneyAnswerMapper fastMoneyAnswerMapper) {
        this.gameRepository = gameRepository;
        this.participantRepository = participantRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.fastMoneyRoundRepository = fastMoneyRoundRepository;
        this.eventPublisher = eventPublisher;
        this.fastMoneyAnswerMapper = fastMoneyAnswerMapper;
    }

    @Transactional
    public FastMoneyDTO startFastMoney(Long gameId, Long player1Id, Long player2Id) {
        log.info("[startFastMoney] gameId={}, player1Id={}, player2Id={}", gameId, player1Id, player2Id);

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getStatus() != GameStatus.FINISHED) {
            throw new IllegalStateException("La partida debe estar terminada para iniciar Dinero Rápido");
        }

        Participant p1 = participantRepository.findById(player1Id)
                .orElseThrow(() -> new ParticipantNotFoundException("Jugador 1 no encontrado"));
        Participant p2 = participantRepository.findById(player2Id)
                .orElseThrow(() -> new ParticipantNotFoundException("Jugador 2 no encontrado"));

        game.setStatus(GameStatus.FAST_MONEY);
        gameRepository.save(game);

        eventPublisher.publishEvent(new GameEvent("FAST_MONEY_STARTED", gameId));
        log.info("[startFastMoney] Dinero Rápido iniciado entre {} y {}", p1.getName(), p2.getName());

        FastMoneyDTO dto = new FastMoneyDTO();
        dto.setGameId(gameId);
        dto.setStatus("FAST_MONEY");
        dto.setPlayer1Id(p1.getId().intValue());
        dto.setPlayer2Id(p2.getId().intValue());
        dto.setPlayer1Name(p1.getName());
        dto.setPlayer2Name(p2.getName());
        dto.setBonusTarget(BONUS_TARGET);

        List<Question> allQuestions = questionRepository.findAll();
        List<GameQuestion> gameQuestions = game.getGameQuestions();
        Set<Long> usedQuestionIds = gameQuestions.stream()
                .map(gq -> gq.getQuestion().getId())
                .collect(Collectors.toSet());

        List<Question> availableQuestions = allQuestions.stream()
                .filter(q -> !usedQuestionIds.contains(q.getId()))
                .toList();

        if (availableQuestions.size() < QUESTIONS_COUNT) {
            availableQuestions = new ArrayList<>(allQuestions);
        }
        Collections.shuffle(availableQuestions);
        List<Question> selected = availableQuestions.subList(0, Math.min(QUESTIONS_COUNT, availableQuestions.size()));

        dto.setQuestions(selected.stream().map(Question::getText).toList());

        return dto;
    }

    @Transactional
    public FastMoneyDTO submitAnswers(Long gameId, FastMoneySubmissionDTO submission) {
        log.info("[submitAnswers] gameId={}, participantId={}, playerNumber={}, answers={}",
                gameId, submission.getParticipantId(), submission.getAnswers().size());

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        if (game.getStatus() != GameStatus.FAST_MONEY) {
            throw new IllegalStateException("La partida no está en Dinero Rápido");
        }

        Participant participant = participantRepository.findById(submission.getParticipantId())
                .orElseThrow(() -> new ParticipantNotFoundException("Participante no encontrado"));

        List<FastMoneyRound> existingRounds = fastMoneyRoundRepository.findByGameIdAndParticipantId(gameId, participant.getId());
        int playerNumber = existingRounds.isEmpty() ? 1 : 2;

        List<Question> allQuestions = questionRepository.findAll();
        List<GameQuestion> gameQuestions = game.getGameQuestions();
        Set<Long> usedQuestionIds = gameQuestions.stream()
                .map(gq -> gq.getQuestion().getId())
                .collect(Collectors.toSet());

        List<Question> availableQuestions = allQuestions.stream()
                .filter(q -> !usedQuestionIds.contains(q.getId()))
                .toList();
        if (availableQuestions.size() < QUESTIONS_COUNT) {
            availableQuestions = new ArrayList<>(allQuestions);
        }
        Collections.shuffle(availableQuestions);
        List<Question> selected = availableQuestions.subList(0, Math.min(QUESTIONS_COUNT, availableQuestions.size()));

        List<String> previousAnswers = fastMoneyRoundRepository.findCorrectAnswersByGameIdAndPlayerNumber(gameId, 1);

        Set<String> usedAnswers = new HashSet<>(previousAnswers);
        int totalPoints = 0;

        for (int i = 0; i < submission.getAnswers().size() && i < selected.size(); i++) {
            String answerText = submission.getAnswers().get(i);
            Question question = selected.get(i);

            FastMoneyRound round = new FastMoneyRound();
            round.setGame(game);
            round.setParticipant(participant);
            round.setQuestion(question);
            round.setAnswerText(answerText);
            round.setPlayerNumber(playerNumber);

            Answer matchedAnswer = answerRepository.findByQuestionIdAndTextIgnoreCase(question.getId(), answerText);

            if (matchedAnswer != null && !usedAnswers.contains(answerText.toLowerCase())) {
                round.setPoints(matchedAnswer.getScore());
                round.setCorrect(true);
                totalPoints += matchedAnswer.getScore();
                usedAnswers.add(answerText.toLowerCase());
            } else {
                round.setPoints(0);
                round.setCorrect(false);
            }

            fastMoneyRoundRepository.save(round);
        }

        int totalPlayer1 = fastMoneyRoundRepository.sumPointsByGameIdAndParticipantId(gameId,
                Long.valueOf(submission.getParticipantId()));
        int totalPlayer2 = fastMoneyRoundRepository.sumAllPointsByGameId(gameId) - totalPlayer1;

        if (playerNumber == 1) {
            totalPlayer1 = totalPoints;
            totalPlayer2 = 0;
        } else {
            totalPlayer2 = totalPoints;
        }

        FastMoneyDTO dto = new FastMoneyDTO();
        dto.setGameId(gameId);
        dto.setPlayer1Id(participant.getId().intValue());
        dto.setPlayer1Name(participant.getName());
        dto.setPlayer1TotalPoints(playerNumber == 1 ? totalPoints : totalPlayer1);
        dto.setPlayer2TotalPoints(playerNumber == 2 ? totalPoints : totalPlayer2);
        dto.setCombinedTotal(dto.getPlayer1TotalPoints() + dto.getPlayer2TotalPoints());
        dto.setBonusTarget(BONUS_TARGET);
        dto.setWonBonus(dto.getCombinedTotal() >= BONUS_TARGET);

        if (playerNumber == 2) {
            dto.setStatus("COMPLETED");
            if (dto.getCombinedTotal() >= BONUS_TARGET) {
                game.setStatus(GameStatus.FINISHED);
                gameRepository.save(game);
                eventPublisher.publishEvent(new GameEvent("FAST_MONEY_WON", gameId));
                eventPublisher.publishEvent(new GameEvent("GAME_FINISHED", gameId));
            } else {
                game.setStatus(GameStatus.FINISHED);
                gameRepository.save(game);
                eventPublisher.publishEvent(new GameEvent("FAST_MONEY_LOST", gameId));
                eventPublisher.publishEvent(new GameEvent("GAME_FINISHED", gameId));
            }
        } else {
            dto.setStatus("PLAYER1_DONE");
            eventPublisher.publishEvent(new GameEvent("FAST_MONEY_PLAYER1_DONE", gameId));
        }

        return dto;
    }

    public FastMoneyDTO getFastMoneyStatus(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

        List<FastMoneyRound> allRounds = fastMoneyRoundRepository.findByGameIdAndPlayerNumber(gameId, 1);
        List<FastMoneyRound> player2Rounds = fastMoneyRoundRepository.findByGameIdAndPlayerNumber(gameId, 2);

        FastMoneyDTO dto = new FastMoneyDTO();
        dto.setGameId(gameId);
        dto.setStatus(game.getStatus().toString());
        dto.setBonusTarget(BONUS_TARGET);

        int total1 = fastMoneyRoundRepository.sumPointsByGameIdAndParticipantId(gameId,
                allRounds.isEmpty() ? 0L : allRounds.get(0).getParticipant().getId());
        int total2 = fastMoneyRoundRepository.sumAllPointsByGameId(gameId) - total1;

        dto.setPlayer1TotalPoints(total1);
        dto.setPlayer2TotalPoints(total2);
        dto.setCombinedTotal(total1 + total2);
        dto.setWonBonus(dto.getCombinedTotal() >= BONUS_TARGET);

        dto.setPlayer1Answers(allRounds.stream().map(fastMoneyAnswerMapper::toDTO).toList());

        dto.setPlayer2Answers(player2Rounds.stream().map(fastMoneyAnswerMapper::toDTO).toList());

        return dto;
    }
}
