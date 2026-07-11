package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.DTO.BuzzInDTO;
import com.AlanPacheco.CienMD_app.DTO.CreateGameDTO;
import com.AlanPacheco.CienMD_app.DTO.GameDTO;
import com.AlanPacheco.CienMD_app.DTO.GameQuestionDTO;
import com.AlanPacheco.CienMD_app.DTO.GameResultsDTO;
import com.AlanPacheco.CienMD_app.DTO.RoundDTO;
import com.AlanPacheco.CienMD_app.Service.GameService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameDTO> createGame(@RequestBody @Valid CreateGameDTO config) {
        log.info("[API POST /api/games] Creando partida: totalRounds={}, teamSize={}", config.getTotalRounds(), config.getTeamSize());
        GameDTO result = gameService.createNewGame(config);
        log.info("[API POST /api/games] Partida creada: id={}, status={}", result.getId(), result.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping
    public ResponseEntity<List<GameDTO>> getAllGames() {
        log.debug("[API GET /api/games] Listando todas las partidas");
        List<GameDTO> games = gameService.getAllGames();
        log.debug("[API GET /api/games] Total partidas: {}", games.size());
        return ResponseEntity.ok(games);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameDTO> getGameById(@PathVariable Long id) {
        log.info("[API GET /api/games/{}] Consultando partida", id);
        GameDTO game = gameService.getGameById(id);
        log.info("[API GET /api/games/{}] status={}, roundStatus={}, controllingTeam={}, currentQuestionId={}, t1={}, t2={}",
                id, game.getStatus(), game.getCurrentRoundStatus(), game.getControllingTeam(),
                game.getCurrentQuestionId(), game.getTeam1Score(), game.getTeam2Score());
        return ResponseEntity.ok(game);
    }

    @GetMapping("/{gameId}/results")
    public ResponseEntity<GameResultsDTO> getResults(@PathVariable Long gameId) {
        log.info("[API GET /api/games/{}/results] Consultando resultados", gameId);
        return ResponseEntity.ok(gameService.getFinalResults(gameId));
    }

    @GetMapping("/{gameId}/questions/{questionId}")
    public ResponseEntity<GameQuestionDTO> getGameQuestion(
            @PathVariable Long gameId, @PathVariable Long questionId) {
        log.info("[API GET /api/games/{}/questions/{}] Consultando pregunta", gameId, questionId);
        return ResponseEntity.ok(gameService.getGameQuestion(gameId, questionId));
    }

    @PostMapping("/{gameId}/rounds/start")
    public ResponseEntity<GameDTO> startRound(@PathVariable Long gameId) {
        log.info("[API POST /api/games/{}/rounds/start] Iniciando ronda", gameId);
        GameDTO result = gameService.startNextRound(gameId);
        log.info("[API POST /api/games/{}/rounds/start] Ronda iniciada: status={}, roundStatus={}, controllingTeam={}, questionId={}, multiplier={}",
                gameId, result.getStatus(), result.getCurrentRoundStatus(), result.getControllingTeam(),
                result.getCurrentQuestionId(), result.getCurrentMultiplier());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{gameId}/rounds/answer")
    public ResponseEntity<GameQuestionDTO> submitAnswer(
            @PathVariable Long gameId,
            @RequestBody @Valid RoundDTO roundDTO) {
        log.info("[API POST /api/games/{}/rounds/answer] participantId={}, gameQuestionId={}, answerText='{}', multiplier={}",
                gameId, roundDTO.getParticipantId(), roundDTO.getGameQuestionId(), roundDTO.getAnswerText(), roundDTO.getRoundMultiplier());
        GameQuestionDTO result = gameService.submitAnswer(gameId, roundDTO);
        log.info("[API POST /api/games/{}/rounds/answer] Respuesta procesada. Answers reveladas={}",
                gameId, result.getAnswers() != null ? result.getAnswers().stream().filter(a -> a.isRevealed()).count() : 0);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{gameId}/rounds/end")
    public ResponseEntity<GameDTO> endRound(@PathVariable Long gameId) {
        log.info("[API POST /api/games/{}/rounds/end] Terminando ronda", gameId);
        GameDTO result = gameService.endRound(gameId);
        log.info("[API POST /api/games/{}/rounds/end] Ronda terminada: t1={}, t2={}", gameId, result.getTeam1Score(), result.getTeam2Score());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{gameId}/rounds/pass")
    public ResponseEntity<GameDTO> passTurn(@PathVariable Long gameId) {
        log.info("[API POST /api/games/{}/rounds/pass] Pasando turno", gameId);
        GameDTO result = gameService.passTurn(gameId);
        log.info("[API POST /api/games/{}/rounds/pass] Turno pasado: roundStatus={}, roundPoints={}", gameId, result.getCurrentRoundStatus(), result.getCurrentRoundPoints());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{gameId}/rounds/error")
    public ResponseEntity<GameDTO> incrementError(@PathVariable Long gameId) {
        log.info("[API POST /api/games/{}/rounds/error] Registrando error", gameId);
        GameDTO result = gameService.incrementError(gameId);
        log.info("[API POST /api/games/{}/rounds/error] Error registrado: t1Errors={}, t2Errors={}, roundStatus={}", gameId, result.getTeam1Errors(), result.getTeam2Errors(), result.getCurrentRoundStatus());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{gameId}/rounds/reveal/{answerId}")
    public ResponseEntity<GameQuestionDTO> revealAnswer(
            @PathVariable Long gameId, @PathVariable Long answerId) {
        log.info("[API POST /api/games/{}/rounds/reveal/{}] Revelando respuesta", gameId, answerId);
        return ResponseEntity.ok(gameService.revealAnswer(gameId, answerId));
    }

    @PostMapping("/{gameId}/faceoff/start")
    public ResponseEntity<GameDTO> startFaceOff(
            @PathVariable Long gameId,
            @RequestBody java.util.Map<String, Long> body) {
        log.info("[API POST /api/games/{}/faceoff/start] player1={}, player2={}",
                gameId, body.get("player1Id"), body.get("player2Id"));
        GameDTO result = gameService.startFaceOff(gameId, body.get("player1Id"), body.get("player2Id"));
        log.info("[API POST /api/games/{}/faceoff/start] Face-off iniciado: roundStatus={}", gameId, result.getCurrentRoundStatus());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{gameId}/faceoff/buzz")
    public ResponseEntity<GameDTO> buzzIn(
            @PathVariable Long gameId,
            @RequestBody @Valid BuzzInDTO buzzInDTO) {
        log.info("[API POST /api/games/{}/faceoff/buzz] participantId={}, answerText='{}'",
                gameId, buzzInDTO.getParticipantId(), buzzInDTO.getAnswerText());
        GameDTO result = gameService.buzzIn(gameId, buzzInDTO.getParticipantId(), buzzInDTO.getAnswerText());
        log.info("[API POST /api/games/{}/faceoff/buzz] Buzz procesado: roundStatus={}, controllingTeam={}",
                gameId, result.getCurrentRoundStatus(), result.getControllingTeam());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{gameId}/sudden-death/faceoff")
    public ResponseEntity<GameDTO> suddenDeathFaceOff(
            @PathVariable Long gameId,
            @RequestBody java.util.Map<String, Long> body) {
        log.info("[API POST /api/games/{}/sudden-death/faceoff] player1={}, player2={}",
                gameId, body.get("player1Id"), body.get("player2Id"));
        GameDTO result = gameService.suddenDeathFaceOff(gameId, body.get("player1Id"), body.get("player2Id"));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{gameId}/sudden-death/buzz")
    public ResponseEntity<GameDTO> suddenDeathBuzzIn(
            @PathVariable Long gameId,
            @RequestBody @Valid BuzzInDTO buzzInDTO) {
        log.info("[API POST /api/games/{}/sudden-death/buzz] participantId={}, answerText='{}'",
                gameId, buzzInDTO.getParticipantId(), buzzInDTO.getAnswerText());
        GameDTO result = gameService.suddenDeathBuzzIn(gameId, buzzInDTO.getParticipantId(), buzzInDTO.getAnswerText());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{gameId}/sudden-death/answer")
    public ResponseEntity<GameDTO> suddenDeathAnswer(
            @PathVariable Long gameId,
            @RequestBody @Valid RoundDTO roundDTO) {
        log.info("[API POST /api/games/{}/sudden-death/answer] participantId={}, answerText='{}'",
                gameId, roundDTO.getParticipantId(), roundDTO.getAnswerText());
        GameDTO result = gameService.suddenDeathAnswer(gameId, roundDTO);
        return ResponseEntity.ok(result);
    }
}
