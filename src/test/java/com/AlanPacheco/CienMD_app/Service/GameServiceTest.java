package com.AlanPacheco.CienMD_app.Service;

import com.AlanPacheco.CienMD_app.DTO.CreateParticipantDTO;
import com.AlanPacheco.CienMD_app.DTO.GameDTO;
import com.AlanPacheco.CienMD_app.DTO.ParticipantDTO;
import com.AlanPacheco.CienMD_app.Entity.Answer;
import com.AlanPacheco.CienMD_app.Entity.Question;
import com.AlanPacheco.CienMD_app.Exception.InsufficientQuestionsException;
import com.AlanPacheco.CienMD_app.Repository.AnswerRepository;
import com.AlanPacheco.CienMD_app.Repository.GameQuestionRepository;
import com.AlanPacheco.CienMD_app.Repository.GameRepository;
import com.AlanPacheco.CienMD_app.Repository.ParticipantRepository;
import com.AlanPacheco.CienMD_app.Repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GameServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameQuestionRepository gameQuestionRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @BeforeEach
    void setUp() {
        for (int q = 1; q <= 5; q++) {
            Question question = new Question();
            question.setText("Pregunta de prueba " + q);
            question = questionRepository.save(question);
            for (int a = 1; a <= 3; a++) {
                Answer answer = new Answer();
                answer.setQuestion(question);
                answer.setText("Respuesta " + q + "-" + a);
                answer.setScore(a * 10);
                answerRepository.save(answer);
            }
        }
    }

    @Test
    void createNewGame_WithEnoughQuestions_CreatesGame() {
        GameDTO game = gameService.createNewGame();
        assertNotNull(game.getId());
        assertEquals("NOT_STARTED", game.getStatus());
        assertEquals(0, game.getTeam1Score());
        assertEquals(0, game.getTeam2Score());
    }

    @Test
    void createNewGame_WithoutEnoughQuestions_ThrowsException() {
        gameQuestionRepository.deleteAll();
        participantRepository.deleteAll();
        gameRepository.deleteAll();
        answerRepository.deleteAll();
        questionRepository.deleteAll();
        assertThrows(InsufficientQuestionsException.class, () -> gameService.createNewGame());
    }

    @Test
    void addParticipant_ToNewGame_AddsSuccessfully() {
        GameDTO game = gameService.createNewGame();

        CreateParticipantDTO p1 = new CreateParticipantDTO();
        p1.setName("Jugador 1");
        p1.setTeam(1);

        ParticipantDTO participant = gameService.addParticipant(game.getId(), p1);

        assertNotNull(participant.getId());
        assertEquals("Jugador 1", participant.getName());
        assertEquals(1, participant.getTeam());
    }

    @Test
    void startNextRound_WithNewGame_StartsRound() {
        GameDTO game = gameService.createNewGame();

        CreateParticipantDTO p1 = new CreateParticipantDTO();
        p1.setName("Jugador 1");
        p1.setTeam(1);
        gameService.addParticipant(game.getId(), p1);

        GameDTO started = gameService.startNextRound(game.getId());

        assertEquals("IN_PROGRESS", started.getStatus());
        assertNotNull(started.getCurrentQuestionId());
    }
}
