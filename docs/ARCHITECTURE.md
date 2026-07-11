# Arquitectura del Proyecto — 100MD-app

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Build | Maven (con Wrapper: `mvnw`) |
| Base de datos | MySQL (dev/prod) / H2 (tests) |
| ORM | Spring Data JPA (Hibernate) |
| Mapeo DTO | MapStruct 1.5.5 + Lombok |
| Seguridad | Spring Security (BCrypt, form login) |
| Templates | Thymeleaf |
| Frontend | Alpine.js 3.14.1 + Bootstrap 5 |
| Tiempo real | Spring WebSocket + STOMP + SockJS |
| Boilerplate | Lombok 1.18.34 |
| API docs | Springdoc OpenAPI (Swagger UI) |
| Migraciones | Flyway (13 versiones: V1-V13) |
| Audio | Web Audio API (oscillator-based) |
| Confetti | canvas-confetti@1.9.3 (CDN) |

---

## Estructura de Paquetes

```
com.AlanPacheco.CienMD_app/
├── Application.java
├── Config/
│   ├── SecurityConfig.java          # Spring Security (form login, BCrypt)
│   ├── WebConfig.java               # CORS configuration
│   ├── WebSocketConfig.java         # STOMP + SockJS configuration
│   ├── WebSocketEventListener.java  # WebSocket event logging
│   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│   ├── GameEvent.java               # Application event for WS broadcast
│   ├── DataInitializer.java         # Seed data on startup
│   └── SwaggerConfig.java           # OpenAPI/Swagger config
├── Controller/
│   ├── GameController.java          # REST: CRUD partidas + rondas
│   ├── GameWebSocketController.java # WebSocket: mensajes STOMP
│   ├── FastMoneyController.java     # REST: dinero rápido
│   ├── GameController.java          # REST: gestión de partidas
│   ├── AuthController.java          # Web: login + registro
│   ├── HomeController.java          # Web: dashboard + play
│   ├── AdminController.java         # Web: CRUD preguntas
│   ├── QuestionController.java      # REST: CRUD preguntas
│   ├── StatsController.java         # REST: estadísticas
│   └── RoundController.java         # REST: rondas (legacy)
├── Service/
│   ├── GameService.java             # Lógica de negocio principal (~1092 líneas)
│   ├── FastMoneyService.java        # Lógica dinero rápido
│   ├── StatsService.java            # Estadísticas e historial
│   ├── UserService.java             # Registro de usuarios
│   └── QuestionService.java         # CRUD preguntas
├── Entity/
│   ├── Game.java                    # Partida (raíz del agregado)
│   ├── GameQuestion.java            # Relación Pregunta-Partida
│   ├── GameRound.java               # Registro de intento de respuesta
│   ├── Question.java                # Pregunta de encuesta
│   ├── Answer.java                  # Respuesta posible con puntaje
│   ├── Participant.java             # Jugador en una partida
│   ├── FastMoneyRound.java          # Ronda de dinero rápido
│   └── User.java                    # Usuario del sistema
├── DTO/
│   ├── GameDTO.java                 # Estado de partida
│   ├── GameQuestionDTO.java         # Pregunta con respuestas
│   ├── GameUpdateDTO.java           # Actualización por WebSocket
│   ├── GameResultsDTO.java          # Resultados finales
│   ├── GameHistoryDTO.java          # Historial de partida
│   ├── ParticipantDTO.java          # Participante con captain
│   ├── CreateParticipantDTO.java    # Crear participante
│   ├── CreateGameDTO.java           # Configurar partida
│   ├── RoundDTO.java                # Envío de respuesta
│   ├── AnswerDTO.java               # Respuesta con puntaje
│   ├── QuestionDTO.java             # Pregunta
│   ├── FastMoneyDTO.java            # Estado dinero rápido
│   ├── FastMoneyAnswerDTO.java      # Respuesta dinero rápido
│   ├── FastMoneySubmissionDTO.java  # Envío dinero rápido
│   ├── BuzzInDTO.java               # Buzzer face-off
│   ├── PlayerStatsDTO.java          # Estadísticas de jugador
│   ├── RegisterDTO.java             # Registro de usuario
│   └── ErrorResponse.java           # Error estandarizado
├── Repository/
│   ├── GameRepository.java
│   ├── GameQuestionRepository.java
│   ├── GameRoundRepository.java
│   ├── QuestionRepository.java
│   ├── AnswerRepository.java
│   ├── ParticipantRepository.java
│   ├── FastMoneyRoundRepository.java
│   └── UserRepository.java
├── Enum/
│   ├── GameStatus.java              # NOT_STARTED, IN_PROGRESS, SUDDEN_DEATH, FAST_MONEY, FINISHED
│   ├── GameRoundStatus.java         # NOT_STARTED, FACE_OFF, TURN_PLAYER1, TURN_PLAYER2, STEAL_ATTEMPT, SUDDEN_DEATH_FACE_OFF, FINISHED
│   └── UserRole.java                # ADMIN, PLAYER
├── Exception/
│   ├── GameNotFoundException.java
│   ├── GameQuestionNotFoundException.java
│   ├── GameAlreadyFinishedException.java
│   ├── InsufficientQuestionsException.java
│   └── ParticipantNotFoundException.java
├── Mapper/                          # MapStruct (compile-time)
│   ├── GameMapper.java              # Game ↔ GameDTO
│   ├── GameQuestionMapper.java      # GameQuestion ↔ GameQuestionDTO
│   ├── AnswerMapper.java            # Answer ↔ AnswerDTO
│   ├── ParticipantMapper.java       # Participant ↔ ParticipantDTO
│   ├── FastMoneyAnswerMapper.java   # FastMoneyRound ↔ FastMoneyAnswerDTO
│   └── GameHistoryMapper.java       # Game → GameHistoryDTO
└── Converter/
    └── IntArrayConverter.java       # int[] ↔ String (AttributeConverter)
```

---

## Modelo Entidad-Relación

```
User
  │
Game (1) ──── (N) GameQuestion (N) ──── (1) Question (1) ──── (N) Answer
  │
  ├── (1) ──── (N) Participant
  │               └── isCaptain, memberOrder
  │
  ├── (1) ──── (N) GameRound
  │               └── answerText, score, multiplier, isCorrect
  │
  └── (1) ──── (N) FastMoneyRound
                  └── playerNumber, questionOrder, answerText, score
```

---

## Frontend (Templates + JS)

```
src/main/resources/
├── templates/
│   ├── login.html                   # Login
│   ├── register.html                # Registro
│   ├── dashboard.html               # Dashboard con cards + modal configuración
│   ├── play.html                    # Game board host-controlled (~1297 líneas)
│   ├── history.html                 # Historial de partidas
│   ├── stats.html                   # Estadísticas de jugadores
│   └── admin/
│       ├── layout.html              # Layout admin
│       ├── questions.html           # Lista de preguntas
│       └── question-form.html       # Formulario crear/editar
├── static/
│   ├── css/
│   │   └── style.css                # Premium TV show aesthetic (~1132 líneas)
│   └── js/
│       ├── audio.js                 # AudioManager (Web Audio API oscillator)
│       ├── game-timer.js            # Timer mixin (Alpine.js component)
│       └── fast-money.js            # Fast money mixin (Alpine.js component)
```

---

## Flujo del Juego

```
1. SETUP: Host crea partida → agrega miembros → selecciona capitanes
2. FACE_OFF: 1v1 buzzer → ganador toma control
3. TURNOS: Equipo responde en cadena → aciertos suman puntos
4. STRIKES: 3 errores → STEAL_ATTEMPT → capitán contrario intenta robar
5. END_ROUND: Se asignan puntos al equipo controlador
6. Repite 1-5 por 5 rondas (x1,x1,x2,x2,x3)
7. Si nadie llega a 300 → MUERTE SÚBITA (1 strike, robo automático)
8. Equipo ganador → DINERO RÁPIDO (2 jugadores, 5 preguntas, bonus 200 pts)
9. RESULTADOS: Confetti, scores finales, volver al dashboard
```

---

## Decisiones Técnicas

### ¿Por qué MySQL y no H2?
H2 se usa solo para pruebas. El entorno de desarrollo y producción usa MySQL. Spring Data JPA abstrae la BD.

### ¿Por qué MapStruct y no ModelMapper?
MapStruct genera código en compile-time (sin reflexión, cero overhead). ModelMapper usa reflexión en runtime.

### ¿Por qué Web Audio API en vez de archivos MP3?
Los sonidos son tonos sintetizados con osciladores (sine waves). No dependen de archivos externos, cargan instantáneamente, y son personalizables.

### ¿Por qué canvas-confetti en vez de CSS puro?
canvas-confetti ofrece efectos superiores (fireworks, shapes), soporte de accesibilidad (`disableForReducedMotion`), y es solo 8KB vía CDN.

### ¿Por qué Alpine.js y no React/Vue?
El juego es server-rendered (Thymeleaf) con interactividad en el cliente. Alpine.js se integra directamente en HTML sin build step. React/Vue requerirían un setup de bundler adicional.

### DTOs separados de Entidades
Se evita exponer las entidades JPA directamente al cliente. MapStruct genera los mappers en compile-time.
