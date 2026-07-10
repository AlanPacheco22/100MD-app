# Plan de Implementación — 100 Mexicanos Dijeron (100MD-app)

> **Generado:** Julio 2026
> **Propósito:** Guía detallada LLM-friendly para la implementación completa del proyecto.
> **Cada fase contiene:** archivos a modificar, cambios específicos, fragmentos de código, y criterios de verificación.

---

## Resumen del Proyecto

Backend API + Frontend web del juego **"100 Mexicanos Dijeron"** (Family Feud versión México).
Dos equipos compiten adivinando las respuestas más populares a preguntas de encuesta.

**Stack:**
- Backend: Java 17, Spring Boot 3.3.5, Maven, Flyway
- Base de datos: MySQL (desarrollo) / H2 (pruebas) — **abstraída con Spring Data JPA**
- Frontend: Thymeleaf, Bootstrap 5, Alpine.js
- Tiempo real: STOMP WebSocket + SockJS
- Seguridad: Spring Security con formularios + sesiones
- Documentación: Swagger OpenAPI

### Abstracción de Base de Datos con Spring Data JPA
El proyecto usa **Spring Data JPA** como capa de abstracción sobre la base de datos. Esto permite cambiar entre tecnologías (MySQL, H2, PostgreSQL, MariaDB, etc.) **sin modificar una sola línea de código Java** — solo cambiando el perfil de Spring Boot (`application-dev.properties`, `application-test.properties`, `application-prod.properties`).

| Perfil | BD | Propósito |
|---|---|---|
| `dev` | MySQL local | Desarrollo diario con datos reales |
| `test` | H2 en memoria | Pruebas unitarias y de integración (rápidas, sin setup) |
| `prod` | MySQL producción | Entorno real con variables de entorno |

Las migraciones Flyway están diseñadas en SQL estándar para que funcionen en cualquier base de datos soportada por Flyway.

**Estado actual:** ~38% completado. El proyecto NO compila (10+ errores de compilación).

---

## Fase 0: Base de Datos y Seed Data

### Objetivo
Migrar de `spring.jpa.hibernate.ddl-auto=update` a Flyway con migraciones versionadas, crear el esquema completo de base de datos, y sembrar datos iniciales de preguntas.

### Archivos a crear
- `pom.xml` — agregar dependencia Flyway
- `src/main/resources/db/migration/V1__initial_schema.sql` — esquema inicial
- `src/main/resources/db/migration/V2__seed_data.sql` — datos de preguntas
- `src/main/resources/application-dev.properties` — perfil de desarrollo
- `src/main/resources/application-prod.properties` — perfil de producción

### Archivos a modificar
- `src/main/resources/application.properties` — cambiar a configuración mínima, activar Flyway

### Detalle de cambios

#### 1. pom.xml — Agregar Flyway
Agregar dentro de `<dependencies>`:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

#### 2. V1__initial_schema.sql

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role ENUM('ADMIN', 'PLAYER') NOT NULL DEFAULT 'PLAYER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    text VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    text VARCHAR(300) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

CREATE TABLE games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'FINISHED') NOT NULL DEFAULT 'NOT_STARTED',
    current_round_status ENUM('NOT_STARTED', 'TURN_PLAYER1', 'TURN_PLAYER2', 'FINISHED') NOT NULL DEFAULT 'NOT_STARTED',
    team1_score INT NOT NULL DEFAULT 0,
    team2_score INT NOT NULL DEFAULT 0,
    team1_errors INT NOT NULL DEFAULT 0,
    team2_errors INT NOT NULL DEFAULT 0,
    current_game_question_id BIGINT,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_game_creator FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_game_current_question FOREIGN KEY (current_game_question_id) REFERENCES game_questions(id)
);

CREATE TABLE game_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_gq_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    CONSTRAINT fk_gq_question FOREIGN KEY (question_id) REFERENCES questions(id)
);

CREATE TABLE participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id BIGINT NOT NULL,
    user_id BIGINT,
    name VARCHAR(100) NOT NULL,
    team INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_participant_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    CONSTRAINT fk_participant_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE game_rounds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    game_question_id BIGINT NOT NULL,
    answer_text VARCHAR(300),
    score INT NOT NULL DEFAULT 0,
    multiplier INT NOT NULL DEFAULT 1,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gr_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    CONSTRAINT fk_gr_participant FOREIGN KEY (participant_id) REFERENCES participants(id),
    CONSTRAINT fk_gr_game_question FOREIGN KEY (game_question_id) REFERENCES game_questions(id)
);

CREATE TABLE game_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
);

-- Índices
CREATE INDEX idx_answers_question ON answers(question_id);
CREATE INDEX idx_game_questions_game ON game_questions(game_id);
CREATE INDEX idx_participants_game ON participants(game_id);
CREATE INDEX idx_game_rounds_game ON game_rounds(game_id);
CREATE INDEX idx_game_rounds_participant ON game_rounds(participant_id);
CREATE INDEX idx_game_log_game ON game_log(game_id);
CREATE INDEX idx_games_status ON games(status);
CREATE INDEX idx_game_questions_order ON game_questions(game_id, question_order);
```

#### 3. V2__seed_data.sql

```sql
-- Preguntas típicas al estilo "100 Mexicanos Dijeron"
INSERT INTO questions (text) VALUES
('¿Qué harías si te ganaras la lotería?'),
('¿Cuál es la comida mexicana más popular?'),
('¿Qué llevas a una fiesta de cumpleaños?'),
('¿Cuál es el mejor programa de la televisión mexicana?'),
('¿Qué haces en Navidad?'),
('¿Cuál es la palabra que más dices al día?'),
('¿Qué estudiarías si pudieras empezar de nuevo?'),
('¿Cuál es el mejor destino para viajar en México?'),
('¿Qué no puede faltar en un altar de muertos?'),
('¿Cuál es la mejor excusa para llegar tarde al trabajo?');

-- Respuestas para "¿Qué harías si te ganaras la lotería?"
INSERT INTO answers (question_id, text, score) VALUES
(1, 'Comprar una casa', 35),
(1, 'Viajar por el mundo', 25),
(1, 'Ayudar a mi familia', 20),
(1, 'Invertir / Ahorrar', 10),
(1, 'Comprar un coche', 5),
(1, 'Pagar deudas', 5);

-- Respuestas para "¿Cuál es la comida mexicana más popular?"
INSERT INTO answers (question_id, text, score) VALUES
(2, 'Tacos', 40),
(2, 'Mole', 15),
(2, 'Pozole', 15),
(2, 'Tamales', 10),
(2, 'Chiles en nogada', 10),
(2, 'Guacamole', 10);

-- Respuestas para "¿Qué llevas a una fiesta de cumpleaños?"
INSERT INTO answers (question_id, text, score) VALUES
(3, 'Pastel', 40),
(3, 'Regalo', 30),
(3, 'Refresco', 10),
(3, 'Globos', 10),
(3, 'Botana', 5),
(3, 'Música', 5);

-- Respuestas para "¿Cuál es el mejor programa de la televisión mexicana?"
INSERT INTO answers (question_id, text, score) VALUES
(4, 'El Chavo del 8', 35),
(4, '100 Mexicanos Dijeron', 25),
(4, 'La Rosa de Guadalupe', 15),
(4, 'Vecinos', 10),
(4, 'El Chapulín Colorado', 10),
(4, 'MasterChef México', 5);

-- Respuestas para "¿Qué haces en Navidad?"
INSERT INTO answers (question_id, text, score) VALUES
(5, 'Cenar con la familia', 40),
(5, 'Abrir regalos', 20),
(5, 'Ir a misa', 15),
(5, 'Hacer posada', 10),
(5, 'Poner el árbol', 10),
(5, 'Dar las gracias', 5);

-- Respuestas para "¿Cuál es la palabra que más dices al día?"
INSERT INTO answers (question_id, text, score) VALUES
(6, 'Gracias', 25),
(6, 'Hola', 20),
(6, '¿Qué onda?', 15),
(6, 'No mames', 15),
(6, 'OK', 10),
(6, 'Güey/Wei', 15);

-- Respuestas para "¿Qué estudiarías si pudieras empezar de nuevo?"
INSERT INTO answers (question_id, text, score) VALUES
(7, 'Medicina', 25),
(7, 'Ingeniería', 20),
(7, 'Programación / Sistemas', 15),
(7, 'Arte / Diseño', 10),
(7, 'Negocios / Administración', 15),
(7, 'Derecho', 15);

-- Respuestas para "¿Cuál es el mejor destino para viajar en México?"
INSERT INTO answers (question_id, text, score) VALUES
(8, 'Cancún', 30),
(8, 'CDMX', 20),
(8, 'Puerto Vallarta', 15),
(8, 'Los Cabos', 10),
(8, 'Guadalajara', 10),
(8, 'Oaxaca', 10),
(8, 'Mérida', 5);

-- Respuestas para "¿Qué no puede faltar en un altar de muertos?"
INSERT INTO answers (question_id, text, score) VALUES
(9, 'Cempasúchil (flor)', 30),
(9, 'Fotos de los difuntos', 25),
(9, 'Pan de muerto', 20),
(9, 'Velas / Veladoras', 10),
(9, 'Calaveras de azúcar', 10),
(9, 'Agua', 5);

-- Respuestas para "¿Cuál es la mejor excusa para llegar tarde al trabajo?"
INSERT INTO answers (question_id, text, score) VALUES
(10, 'Tráfico', 40),
(10, 'Despertador no sonó', 20),
(10, 'Problemas en el transporte', 15),
(10, 'Se descompuso el coche', 10),
(10, 'Enfermedad', 10),
(10, 'Que se le olvidó algo', 5);
```

#### 4. application.properties (nuevo contenido)
```properties
spring.application.name=100MD-app
server.port=8080

# Activar perfil activo (dev por defecto)
spring.profiles.active=dev

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

#### 5. application-dev.properties
```properties
# MySQL - Desarrollo local
spring.datasource.url=jdbc:mysql://localhost:3306/100md_db_dev?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=${MYSQL_PASSWORD:root}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Swagger
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
```

#### 6. application-prod.properties
```properties
# MySQL - Producción
spring.datasource.url=${MYSQL_URL}
spring.datasource.username=${MYSQL_USER}
spring.datasource.password=${MYSQL_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Swagger (deshabilitado en producción)
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

### Criterios de verificación
- [ ] `./mvnw flyway:migrate` ejecuta V1 y V2 sin errores
- [ ] `./mvnw compile` pasa sin errores
- [ ] La BD tiene 10 preguntas con 6 respuestas cada una
- [ ] Tabla `users` creada para login futuro

---

## Fase 1: Corrección del Core

### Objetivo
Hacer que el proyecto compile y la lógica del juego funcione correctamente.

### Archivos a modificar

#### 1. `src/main/java/com/AlanPacheco/CienMD_app/Entity/Game.java`
**Agregar campos faltantes:**
```java
private int team1Score;
private int team2Score;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "current_game_question_id")
private GameQuestion currentGameQuestion;
```

Agregar relación con el creador (para login futuro):
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "created_by")
private User createdBy;
```

**Agregar anotación `@Table`:**
```java
@Table(name = "games")
```

**Agregar FetchType.LAZY** a las colecciones para evitar problemas de serialización:
```java
@OneToMany(mappedBy = "game", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
private List<Participant> participants = new ArrayList<>();

@OneToMany(mappedBy = "game", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
private List<GameQuestion> gameQuestions = new ArrayList<>();
```

#### 2. `src/main/java/com/AlanPacheco/CienMD_app/Entity/Answer.java`
```java
@Table(name = "answers")
```

#### 3. `src/main/java/com/AlanPacheco/CienMD_app/Entity/GameQuestion.java`
**Agregar campo de orden:**
```java
@Column(name = "question_order")
private int questionOrder;
```
```java
@Table(name = "game_questions")
```

#### 4. `src/main/java/com/AlanPacheco/CienMD_app/Entity/GameRound.java`
```java
@Table(name = "game_rounds")
```
**Agregar campo `isCorrect` para seguimiento:**
```java
@Column(name = "is_correct", nullable = false)
private boolean isCorrect;
```

#### 5. `src/main/java/com/AlanPacheco/CienMD_app/Entity/Participant.java`
```java
@Table(name = "participants")
```
**Agregar campo `team`:**
```java
@Column(nullable = false)
private int team = 1;
```

#### 6. `src/main/java/com/AlanPacheco/CienMD_app/Entity/Question.java`
```java
@Table(name = "questions")
```
**Agregar columna de longitud:**
```java
@Column(length = 500, nullable = false)
private String text;
```

#### 7. `src/main/java/com/AlanPacheco/CienMD_app/DTO/GameQuestionDTO.java`
**Agregar import faltante:**
```java
import java.util.List;
```

#### 8. `src/main/java/com/AlanPacheco/CienMD_app/DTO/GameDTO.java`
**Sincronizar con Game entity:**
```java
private Long currentGameQuestionId;
```

#### 9. `src/main/java/com/AlanPacheco/CienMD_app/Repository/GameQuestionRepository.java`
**Corregir retorno de findByGameId:**
```java
List<GameQuestion> findByGameId(Long gameId);
```

#### 10. `src/main/java/com/AlanPacheco/CienMD_app/Repository/GameRoundRepository.java`
**Corregir sumScoreByParticipantId para evitar NPE:**
```java
@Query("SELECT COALESCE(SUM(gr.score), 0) FROM GameRound gr WHERE gr.participant.id = :participantId")
int sumScoreByParticipantId(@Param("participantId") Long participantId);
```

#### 11. `src/main/java/com/AlanPacheco/CienMD_app/Service/GameService.java`
**Reescritura completa. La nueva implementación debe:**

**a) `createNewGame()`:**
```java
public GameDTO createNewGame() {
    Game game = new Game();
    game.setDate(LocalDateTime.now());
    game.setStatus(GameStatus.NOT_STARTED);
    game.setCurrentRoundStatus(GameRoundStatus.NOT_STARTED);
    game.setTeam1Score(0);
    game.setTeam2Score(0);
    game.setTeam1Errors(0);
    game.setTeam2Errors(0);

    List<Question> allQuestions = questionRepository.findAll();
    if (allQuestions.size() < 3) {
        throw new InsufficientQuestionsException(
            "Se necesitan al menos 3 preguntas para crear una partida. Hay: " + allQuestions.size());
    }

    Collections.shuffle(allQuestions);
    List<Question> selected = allQuestions.subList(0, 3);

    game = gameRepository.save(game);
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
```

**b) `submitAnswer()` — flujo unificado:**
```java
@Transactional
public GameQuestionDTO submitAnswer(Long gameId, RoundDTO roundDTO) {
    Game game = gameRepository.findById(gameId)
        .orElseThrow(() -> new GameNotFoundException("Partida no encontrada: " + gameId));

    if (game.getStatus() == GameStatus.FINISHED) {
        throw new GameAlreadyFinishedException("La partida ya terminó");
    }

    Participant participant = participantRepository.findById(roundDTO.getParticipantId())
        .orElseThrow(() -> new ParticipantNotFoundException("Participante no encontrado"));

    GameQuestion gameQuestion = gameQuestionRepository.findById(roundDTO.getGameQuestionId())
        .orElseThrow(() -> new GameQuestionNotFoundException("Pregunta no encontrada en la partida"));

    // Buscar respuesta (case-insensitive)
    Answer answer = answerRepository
        .findByQuestionIdAndTextIgnoreCase(gameQuestion.getQuestion().getId(), roundDTO.getAnswerText());

    GameRound round = new GameRound();
    round.setGame(game);
    round.setParticipant(participant);
    round.setGameQuestion(gameQuestion);
    round.setAnswerText(roundDTO.getAnswerText());
    round.setMultiplier(roundDTO.getRoundMultiplier());

    if (answer != null) {
        // Respuesta correcta
        int points = answer.getScore() * roundDTO.getRoundMultiplier();
        round.setScore(points);
        round.setCorrect(true);
        accumulatePoints(game, points, participant.getTeam());
    } else {
        // Respuesta incorrecta
        round.setScore(0);
        round.setCorrect(false);
        incrementErrors(game, participant.getTeam());
    }

    gameRoundRepository.save(round);
    gameRepository.save(game);

    // Verificar si todos los participantes del equipo actual respondieron
    if (allTeamMembersAnswered(game, participant.getTeam(), gameQuestion)) {
        changeTurn(game);
    }

    return mapToGameQuestionDTO(gameQuestion);
}
```

**c) Métodos auxiliares nuevos:**
```java
private void accumulatePoints(Game game, int points, int team) {
    if (team == 1) {
        game.setTeam1Score(game.getTeam1Score() + points);
    } else {
        game.setTeam2Score(game.getTeam2Score() + points);
    }
}

private void incrementErrors(Game game, int team) {
    int maxErrors = (team == 1) ? 3 : 1;
    if (team == 1) {
        game.setTeam1Errors(game.getTeam1Errors() + 1);
        if (game.getTeam1Errors() >= maxErrors) {
            changeTurn(game);
        }
    } else {
        game.setTeam2Errors(game.getTeam2Errors() + 1);
        if (game.getTeam2Errors() >= maxErrors) {
            endRound(game.getId());
        }
    }
}

private boolean allTeamMembersAnswered(Game game, int team, GameQuestion gameQuestion) {
    List<Participant> teamMembers = game.getParticipants().stream()
        .filter(p -> p.getTeam() == team)
        .collect(Collectors.toList());

    long answeredCount = gameRoundRepository.findByGameQuestionId(gameQuestion.getId()).stream()
        .filter(r -> r.getParticipant().getTeam() == team)
        .count();

    return answeredCount >= teamMembers.size();
}

private void changeTurn(Game game) {
    if (game.getCurrentRoundStatus() == GameRoundStatus.TURN_PLAYER1) {
        game.setCurrentRoundStatus(GameRoundStatus.TURN_PLAYER2);
        game.setTeam1Errors(0);
    } else {
        endRound(game.getId());
    }
}
```

**d) Corregir `registerAnswerAndAdvance()` — se puede eliminar o marcar como deprecado.**

**e) Corregir `getGameById(Long id)` — devolver DTO en vez de entidad.**

**f) Corregir `mapToGameDTO()`:**
```java
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
```

#### 12. `src/main/java/com/AlanPacheco/CienMD_app/Controller/GameController.java`
**Eliminar duplicación con RoundController. Consolidar todos los endpoints de rondas aquí:**
- Eliminar `RoundController.java` o dejarlo solo con los endpoints que no estén en GameController.
- Cambiar `getGameById()` para que devuelva `GameDTO` en vez de `Game`.
- Usar `ResponseEntity` en todos los endpoints.
- Agregar manejo de excepciones.

```java
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameDTO> createGame() {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.createNewGame());
    }

    @GetMapping
    public ResponseEntity<List<GameDTO>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameDTO> getGameById(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.getGameById(id));
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<GameResultsDTO> getResults(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.getFinalResults(id));
    }

    @PostMapping("/{id}/rounds/start")
    public ResponseEntity<GameDTO> startRound(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.startNextRound(id));
    }

    @PostMapping("/{id}/rounds/answer")
    public ResponseEntity<GameQuestionDTO> submitAnswer(
            @PathVariable Long id,
            @RequestBody @Valid RoundDTO roundDTO) {
        return ResponseEntity.ok(gameService.submitAnswer(id, roundDTO));
    }

    @PostMapping("/{id}/rounds/end")
    public ResponseEntity<GameDTO> endRound(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.endRound(id));
    }

    @PostMapping("/{id}/participants")
    public ResponseEntity<ParticipantDTO> addParticipant(
            @PathVariable Long id,
            @RequestBody @Valid CreateParticipantDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(gameService.addParticipant(id, dto));
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantDTO>> getParticipants(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.getParticipants(id));
    }
}
```

#### 13. `src/main/java/com/AlanPacheco/CienMD_app/Controller/GameWebSocketController.java`
**Dejar como stub por ahora; se integrará en Fase 5.**

### Nuevos archivos a crear

#### `src/main/java/com/AlanPacheco/CienMD_app/Exception/`
- `GameNotFoundException.java`
- `InsufficientQuestionsException.java`
- `GameAlreadyFinishedException.java`
- `ParticipantNotFoundException.java`
- `GameQuestionNotFoundException.java`

#### `src/main/java/com/AlanPacheco/CienMD_app/Config/GlobalExceptionHandler.java`
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleGameNotFound(GameNotFoundException ex) {
        return new ErrorResponse("GAME_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InsufficientQuestionsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInsufficientQuestions(InsufficientQuestionsException ex) {
        return new ErrorResponse("INSUFFICIENT_QUESTIONS", ex.getMessage());
    }

    @ExceptionHandler(GameAlreadyFinishedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleGameFinished(GameAlreadyFinishedException ex) {
        return new ErrorResponse("GAME_FINISHED", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return new ErrorResponse("VALIDATION_ERROR", msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception ex) {
        return new ErrorResponse("INTERNAL_ERROR", "Error interno del servidor");
    }
}
```

#### `src/main/java/com/AlanPacheco/CienMD_app/DTO/ErrorResponse.java`
```java
public class ErrorResponse {
    private String code;
    private String message;
    // constructor, getters
}
```

#### `src/main/java/com/AlanPacheco/CienMD_app/DTO/CreateParticipantDTO.java`
```java
public class CreateParticipantDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    
    @Min(value = 1, message = "El equipo debe ser 1 o 2")
    @Max(value = 2, message = "El equipo debe ser 1 o 2")
    private int team;
    // getters, setters
}
```

#### `src/main/java/com/AlanPacheco/CienMD_app/DTO/ParticipantDTO.java`
```java
public class ParticipantDTO {
    private Long id;
    private String name;
    private int team;
    private int score;
    // getters, setters
}
```

#### `src/main/java/com/AlanPacheco/CienMD_app/DTO/GameResultsDTO.java`
```java
public class GameResultsDTO {
    private Long gameId;
    private String status;
    private int team1Score;
    private int team2Score;
    private List<ParticipantDTO> participants;
    private String winner; // "team1", "team2", "draw"
    // getters, setters
}
```

### Criterios de verificación
- [ ] `./mvnw compile` pasa SIN errores
- [ ] `POST /api/games` crea partida con 3 preguntas aleatorias
- [ ] `POST /api/games/{id}/rounds/answer` valida respuestas y acumula puntos
- [ ] El juego cambia de turno correctamente (equipo 1: 3 errores / equipo 2: 1 error)
- [ ] `GET /api/games/{id}/results` devuelve resultados correctos

---

## Fase 2: Arquitectura Limpia

### Objetivo
Refactorizar controladores, DTOs, y validación para seguir buenas prácticas.

### Archivos a crear

#### `src/main/java/com/AlanPacheco/CienMD_app/Config/WebConfig.java`
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:8080", "http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

### Archivos a modificar

#### `QuestionController.java`
- Cambiar `@RequestBody Question` por `@RequestBody @Valid CreateQuestionDTO`
- Devolver `QuestionDTO` en vez de entidad
- Agregar validación

#### `ParticipantController.java`
- Consolidar en `GameController` (eliminar este controlador)
- Usar `CreateParticipantDTO` y `ParticipantDTO`

#### `Dtos restantes`
Migrar todos los endpoints para que usen DTOs de respuesta en vez de entidades JPA.

### Criterios de verificación
- [ ] Ningún controlador retorna entidades JPA directamente
- [ ] Todos los endpoints tienen validación con `@Valid`
- [ ] CORS configurado correctamente

---

## Fase 3: Login y Módulos Web

### Objetivo
Agregar autenticación con Spring Security y páginas web con Thymeleaf + Bootstrap.

### Archivos a crear

#### `pom.xml` — Nuevas dependencias
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

#### `src/main/java/com/AlanPacheco/CienMD_app/Entity/User.java`
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "full_name", length = 100)
    private String fullName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.PLAYER;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

#### `src/main/java/com/AlanPacheco/CienMD_app/Enum/UserRole.java`
```java
public enum UserRole {
    ADMIN, PLAYER
}
```

#### `src/main/java/com/AlanPacheco/CienMD_app/Repository/UserRepository.java`
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

#### `src/main/java/com/AlanPacheco/CienMD_app/Config/SecurityConfig.java`
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/", "/login", "/register").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // Deshabilitar solo para desarrollo
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

#### `src/main/java/com/AlanPacheco/CienMD_app/Service/UserService.java`
```java
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String email, String password, String fullName) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("El usuario ya existe");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(UserRole.PLAYER);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
```

#### `src/main/java/com/AlanPacheco/CienMD_app/Controller/AuthController.java`
```java
@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new RegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid RegisterDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            return "register";
        }
        userService.register(dto.getUsername(), dto.getEmail(), dto.getPassword(), dto.getFullName());
        return "redirect:/login?registered";
    }
}
```

#### `src/main/java/com/AlanPacheco/CienMD_app/Controller/HomeController.java`
```java
@Controller
public class HomeController {
    private final GameService gameService;

    public HomeController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("games", gameService.getAllGames());
        return "dashboard";
    }
}
```

#### Templates Thymeleaf

**`src/main/resources/templates/login.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar Sesión - 100 Mexicanos Dijeron</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link th:href="@{/css/style.css}" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6 col-lg-4">
                <div class="card shadow">
                    <div class="card-body p-5">
                        <h1 class="text-center mb-4" style="color: #006847;">100</h1>
                        <h2 class="text-center mb-4" style="color: #CE1126;">Mexicanos Dijeron</h2>
                        
                        <form th:action="@{/login}" method="post">
                            <div th:if="${param.error}" class="alert alert-danger">
                                Usuario o contraseña incorrectos
                            </div>
                            <div th:if="${param.registered}" class="alert alert-success">
                                Registro exitoso. Inicia sesión.
                            </div>
                            <div class="mb-3">
                                <label for="username" class="form-label">Usuario</label>
                                <input type="text" id="username" name="username" class="form-control" required>
                            </div>
                            <div class="mb-3">
                                <label for="password" class="form-label">Contraseña</label>
                                <input type="password" id="password" name="password" class="form-control" required>
                            </div>
                            <button type="submit" class="btn btn-success w-100">Entrar</button>
                        </form>
                        
                        <p class="text-center mt-3">
                            ¿No tienes cuenta? <a th:href="@{/register}">Regístrate</a>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

**`src/main/resources/templates/register.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registro - 100 Mexicanos Dijeron</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link th:href="@{/css/style.css}" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6 col-lg-4">
                <div class="card shadow">
                    <div class="card-body p-5">
                        <h2 class="text-center mb-4">Crear Cuenta</h2>
                        
                        <form th:action="@{/register}" th:object="${user}" method="post">
                            <div th:if="${#fields.hasErrors('*')}" class="alert alert-danger">
                                <p th:each="err : ${#fields.errors('*')}" th:text="${err}"></p>
                            </div>
                            <div class="mb-3">
                                <label for="fullName" class="form-label">Nombre completo</label>
                                <input type="text" id="fullName" th:field="*{fullName}" class="form-control" required>
                            </div>
                            <div class="mb-3">
                                <label for="username" class="form-label">Usuario</label>
                                <input type="text" id="username" th:field="*{username}" class="form-control" required>
                            </div>
                            <div class="mb-3">
                                <label for="email" class="form-label">Email</label>
                                <input type="email" id="email" th:field="*{email}" class="form-control" required>
                            </div>
                            <div class="mb-3">
                                <label for="password" class="form-label">Contraseña</label>
                                <input type="password" id="password" th:field="*{password}" class="form-control" required>
                            </div>
                            <button type="submit" class="btn btn-success w-100">Registrarse</button>
                        </form>
                        
                        <p class="text-center mt-3">
                            ¿Ya tienes cuenta? <a th:href="@{/login}">Inicia sesión</a>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

**`src/main/resources/templates/dashboard.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - 100 Mexicanos Dijeron</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link th:href="@{/css/style.css}" rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-expand-lg" style="background: linear-gradient(90deg, #006847 0%, #FFFFFF 50%, #CE1126 100%);">
        <div class="container">
            <a class="navbar-brand fw-bold text-dark" href="#">100 Mexicanos Dijeron</a>
            <div class="d-flex">
                <span class="navbar-text me-3" sec:authentication="name"></span>
                <a th:href="@{/logout}" class="btn btn-outline-dark btn-sm">Salir</a>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
        <div class="row mb-4">
            <div class="col">
                <h1>Partidas</h1>
            </div>
            <div class="col text-end">
                <a href="#" class="btn btn-success btn-lg">Nueva Partida</a>
            </div>
        </div>

        <div class="row">
            <div class="col">
                <div class="card">
                    <div class="card-body">
                        <div th:if="${games.empty}" class="text-center py-5">
                            <p class="text-muted">No hay partidas aún. ¡Crea una!</p>
                        </div>
                        <div th:if="${!games.empty}">
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Fecha</th>
                                            <th>Estado</th>
                                            <th>Equipo 1</th>
                                            <th>Equipo 2</th>
                                            <th>Acción</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr th:each="game : ${games}">
                                            <td th:text="${game.id}"></td>
                                            <td th:text="${game.date}"></td>
                                            <td th:text="${game.status}"></td>
                                            <td th:text="${game.team1Score}"></td>
                                            <td th:text="${game.team2Score}"></td>
                                            <td>
                                                <a th:href="@{'/jugar/' + ${game.id}}" class="btn btn-sm btn-primary">Jugar</a>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

**`src/main/resources/templates/play.html`** (placeholder para Fase 4)
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Jugar - 100 Mexicanos Dijeron</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link th:href="@{/css/style.css}" rel="stylesheet">
</head>
<body>
    <nav class="navbar navbar-expand-lg" style="background: linear-gradient(90deg, #006847 0%, #FFFFFF 50%, #CE1126 130%);">
        <div class="container">
            <a class="navbar-brand fw-bold text-dark" href="#">100 Mexicanos Dijeron</a>
            <div class="d-flex">
                <a th:href="@{/dashboard}" class="btn btn-outline-dark btn-sm">Volver</a>
            </div>
        </div>
    </nav>

    <div class="container mt-4">
        <div class="row">
            <div class="col-12">
                <div class="alert alert-info">
                    Pantalla de juego — Pendiente de implementar en Fase 4
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

#### `src/main/resources/static/css/style.css`
```css
:root {
    --green: #006847;
    --white: #FFFFFF;
    --red: #CE1126;
}

body {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.navbar-brand {
    font-size: 1.5rem;
    letter-spacing: 1px;
}

.game-board {
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    border-radius: 15px;
    padding: 2rem;
    min-height: 400px;
}

.team-card {
    border-radius: 15px;
    padding: 1.5rem;
    text-align: center;
}

.team-card.team-1 {
    background-color: var(--green);
    color: white;
}

.team-card.team-2 {
    background-color: var(--red);
    color: white;
}

.score-display {
    font-size: 3rem;
    font-weight: bold;
}

.question-text {
    font-size: 1.5rem;
    font-weight: 600;
    text-align: center;
    padding: 2rem;
}

.answer-btn {
    width: 100%;
    text-align: left;
    padding: 1rem;
    margin-bottom: 0.5rem;
    font-size: 1.1rem;
    border: 2px solid #dee2e6;
    transition: all 0.3s;
}

.answer-btn:hover {
    border-color: var(--green);
    background-color: #f0f8f0;
}

.answer-btn .score {
    float: right;
    font-weight: bold;
    color: var(--green);
}

/* Animación para revelar respuestas */
@keyframes revealAnswer {
    from { opacity: 0; transform: translateY(-10px); }
    to { opacity: 1; transform: translateY(0); }
}

.reveal {
    animation: revealAnswer 0.5s ease-out;
}
```

#### `src/main/java/com/AlanPacheco/CienMD_app/DTO/RegisterDTO.java`
```java
public class RegisterDTO {
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;
    // getters, setters
}
```

### Criterios de verificación
- [ ] Página `/login` funcional con diseño mexicano
- [ ] Registro de usuarios nuevo
- [ ] Redirección a `/dashboard` tras login exitoso
- [ ] Dashboard lista partidas existentes
- [ ] Botón "Nueva Partida" presente
- [ ] Enlace a `/jugar/{id}` funciona

---

## Fase 4: Interfaz del Juego

### Objetivo
Implementar la pantalla de juego completa con Thymeleaf + Bootstrap + Alpine.js + WebSocket.

### Archivos a crear

#### `src/main/resources/templates/play.html` (completo)
Pantalla de juego que incluya:
- **Encabezado**: nombre del juego con colores patrios
- **Tabla de puntuaciones**: equipo 1 (verde) vs equipo 2 (rojo)
- **Pregunta actual**: texto grande y centrado
- **Respuestas**: botones que el jugador puede presionar
- **Contador de errores**: visual para cada equipo (x/3 y x/1)
- **Indicador de turno**: qué equipo está jugando
- **WebSocket**: conexión a `/ws` para recibir actualizaciones en vivo

#### `src/main/java/com/AlanPacheco/CienMD_app/Controller/GameWebController.java`
```java
@Controller
public class GameWebController {
    private final GameService gameService;

    public GameWebController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/jugar/{id}")
    public String playGame(@PathVariable Long id, Model model) {
        model.addAttribute("game", gameService.getGameById(id));
        return "play";
    }
}
```

### Criterios de verificación
- [ ] Pantalla de juego se ve correctamente
- [ ] Se muestran puntuaciones en tiempo real
- [ ] Botones de respuesta funcionan
- [ ] Turnos se alternan correctamente
- [ ] Diseño responsivo (funciona en móvil)

---

## Fase 5: WebSocket Real

### Objetivo
Integrar WebSocket con el flujo real del juego para actualizaciones en vivo.

### Archivos a modificar

#### `src/main/java/com/AlanPacheco/CienMD_app/Config/WebSocketConfig.java`
(ya está configurado, solo verificar que funcione)

#### `src/main/java/com/AlanPacheco/CienMD_app/Controller/GameWebSocketController.java`
Implementar:
```java
@Controller
public class GameWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;

    public GameWebSocketController(SimpMessagingTemplate messagingTemplate, GameService gameService) {
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
    }

    @MessageMapping("/game/{gameId}/answer")
    @SendTo("/topic/game/{gameId}")
    public GameUpdateDTO handleAnswer(@DestinationVariable Long gameId, RoundDTO roundDTO) {
        GameQuestionDTO result = gameService.submitAnswer(gameId, roundDTO);
        GameDTO gameState = gameService.getGameById(gameId);
        return new GameUpdateDTO("ANSWER_SUBMITTED", gameState);
    }

    @MessageMapping("/game/{gameId}/start")
    @SendTo("/topic/game/{gameId}")
    public GameUpdateDTO handleStartRound(@DestinationVariable Long gameId) {
        GameDTO gameState = gameService.startNextRound(gameId);
        return new GameUpdateDTO("ROUND_STARTED", gameState);
    }
}
```

### Eventos WebSocket
| Evento | Descripción |
|---|---|
| `ROUND_STARTED` | Nueva ronda comenzó, se envía la pregunta |
| `ANSWER_SUBMITTED` | Un jugador respondió, se actualiza puntuación |
| `TURN_CHANGED` | Cambió el turno al otro equipo |
| `ROUND_ENDED` | Ronda finalizada, se muestran resultados parciales |
| `GAME_FINISHED` | Partida terminada, se muestran resultados finales |

### Criterios de verificación
- [ ] Cliente WebSocket recibe eventos en tiempo real
- [ ] Múltiples jugadores ven el mismo estado
- [ ] Los eventos corresponden a la acción correcta

---

## Fase 6: Pruebas y CI

### Objetivo
Alcanzar al menos 70% de cobertura de prueba y configurar integración continua.

### Archivos a crear

#### `src/test/java/com/AlanPacheco/CienMD_app/Service/GameServiceTest.java`
- Test: `createNewGame()` crea juego con 3 preguntas
- Test: `submitAnswer()` con respuesta correcta suma puntos
- Test: `submitAnswer()` con respuesta incorrecta suma error
- Test: cambio de turno después de 3 errores del equipo 1
- Test: cambio de turno después de 1 error del equipo 2
- Test: game over después de todas las rondas
- Test: excepción si no hay suficientes preguntas

#### `src/test/java/com/AlanPacheco/CienMD_app/Controller/GameControllerTest.java`
- Test: POST /api/games retorna 201
- Test: GET /api/games/{id} retorna GameDTO
- Test: POST /api/games/{id}/rounds/answer retorna 200
- Test: POST /api/games/{id}/rounds/answer con datos inválidos retorna 400

#### `src/test/resources/application-test.properties`
```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=false
```

#### `.github/workflows/ci.yml`
```yaml
name: CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8
        env:
          MYSQL_ALLOW_EMPTY_PASSWORD: yes
          MYSQL_DATABASE: 100md_db_dev
        ports:
          - 3306:3306
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Compile
        run: ./mvnw compile -Dspring.profiles.active=dev
      - name: Test
        run: ./mvnw test
```

### Criterios de verificación
- [ ] `./mvnw test` pasa todas las pruebas
- [ ] Cobertura > 50% en GameService
- [ ] CI pasa en GitHub Actions

---

## Resumen de Arquitectura Final

```
[Browser/Cliente]
    │
    ├── HTTP (Thymeleaf + Bootstrap) ──→ [Spring MVC Controllers] ──→ [GameService] ──→ [Repositories] ──→ [MySQL]
    │                                                                                                       │
    ├── WebSocket (STOMP + SockJS) ──→ [WebSocket Controller] ──→ [GameService] ────────────────────────────┘
    │
    └── REST API ──→ [API Controllers] ──→ [GameService] ──→ [Repositories]
                                                        │
                                                   [@Transactional]
                                                        │
                                              [GlobalExceptionHandler]
```

**Flujo del juego:**
1. Usuario se registra / inicia sesión (Spring Security + formularios)
2. Crea o se une a una partida (REST API o Web)
3. Comienza la ronda (WebSocket: `ROUND_STARTED`)
4. Los jugadores envían respuestas (WebSocket o REST)
5. El servidor valida, puntúa, y transmite actualizaciones (WebSocket: `ANSWER_SUBMITTED`, `TURN_CHANGED`)
6. Al finalizar, se muestran resultados (WebSocket: `GAME_FINISHED`)

---

## Convenciones para el Equipo (LLM-Friendly)

### Estructura de paquetes después del rediseño
```
com.AlanPacheco.CienMD_app/
├── Application.java
├── Config/          # Security, Swagger, WebSocket, Web
├── Controller/      # API REST + Web Controllers
├── DTO/             # Request/Response DTOs
├── Entity/          # JPA Entities
├── Enum/            # Enums
├── Exception/       # Custom exceptions + GlobalExceptionHandler
├── Repository/      # Spring Data JPA
└── Service/         # Business logic
```

### Orden de implementación sugerido
Este plan está diseñado para implementarse en orden secuencial. Cada fase depende de la anterior:
```
Fase 0 → Fase 1 → Fase 2 → Fase 3 → Fase 4 → Fase 5 → Fase 6
```

### Checklist general antes de cada commit
- [ ] Compila (`./mvnw compile`)
- [ ] Pruebas pasan (`./mvnw test`)
- [ ] Sin errores de Flyway (`./mvnw flyway:migrate`)
- [ ] Swagger UI carga sin errores
- [ ] Sin contraseñas hardcodeadas
- [ ] Sin entidades JPA expuestas al cliente
