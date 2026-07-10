# Arquitectura del Proyecto — 100MD-app

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Build | Maven (con Wrapper: `mvnw`) |
| Base de datos | MySQL |
| ORM | Spring Data JPA (Hibernate) |
| API docs | Springdoc OpenAPI (Swagger UI) |
| Tiempo real | Spring WebSocket + STOMP + SockJS |
| Boilerplate | Lombok |

---

## Estructura de Paquetes

```
com.AlanPacheco.CienMD_app/
├── Application.java                  # Punto de entrada
├── Config/
│   ├── SwaggerConfig.java            # Configuración de OpenAPI/Swagger
│   └── WebSocketConfig.java          # Configuración STOMP/SockJS
├── Controller/
│   ├── GameController.java           # REST: gestión de partidas
│   ├── GameWebSocketController.java  # WebSocket: mensajes STOMP
│   ├── ParticipantController.java    # REST: gestión de participantes
│   ├── QuestionController.java       # REST: CRUD de preguntas
│   └── RoundController.java          # REST: gestión de rondas
├── DTO/
│   ├── GameDTO.java                  # Estado de partida
│   ├── GameUpdateDTO.java            # Actualización por WebSocket
│   ├── GameQuestionDTO.java          # Pregunta con respuestas para una partida
│   ├── RoundDTO.java                 # Envío de respuesta de participante
│   └── AnswerDTO.java                # Respuesta con puntaje
├── Entity/
│   ├── Game.java                     # Partida (raíz del agregado)
│   ├── GameRound.java                # Ronda individual
│   ├── GameQuestion.java             # Relación Pregunta-Partida
│   ├── Question.java                 # Pregunta de encuesta
│   ├── Answer.java                   # Respuesta posible con puntaje
│   └── Participant.java              # Jugador en una partida
├── Enum/
│   ├── GameStatus.java               # NOT_STARTED, IN_PROGRESS, FINISHED
│   └── GameRoundStatus.java          # NOT_STARTED, TURN_PLAYER1, TURN_PLAYER2, FINISHED
├── Repository/
│   ├── GameRepository.java
│   ├── GameRoundRepository.java      # Suma de puntajes por partida
│   ├── GameQuestionRepository.java   # Evita duplicados por fecha
│   ├── QuestionRepository.java
│   ├── AnswerRepository.java         # Búsqueda case-insensitive
│   └── ParticipantRepository.java    # Busca por gameId
└── Service/
    └── GameService.java              # Lógica de negocio principal
```

---

## Flujo de Datos

### Creación de Partida
```
Cliente → POST /api/games
  → GameController.createGame()
    → GameService.createNewGame()
      → Crea entidad Game (status: NOT_STARTED)
      → Selecciona 3 preguntas aleatorias de QuestionRepository
      → Crea GameQuestion para cada una
    ← GameDTO
  ← 200 OK
```

### Flujo de Ronda
```
Cliente → POST /api/games/{id}/rounds/start
  → RoundController.startNextRound()
    → GameService.startNextRound()
      → Avanza GameRoundStatus: NOT_STARTED → TURN_PLAYER1
    ← GameDTO

Cliente → POST /api/games/{id}/rounds/answer  { participantId, answerText }
  → RoundController.submitAnswer()
    → GameService.submitAnswer()
      → Busca Answer en BD (case-insensitive)
      → Si correcta: calcula score * multiplier, acumula puntos
      → Si incorrecta: incrementa errores del equipo
      → Si errores >= límite: cambia turno o finaliza ronda
    ← GameQuestionDTO

Cliente → POST /api/games/{id}/rounds/end
  → RoundController.endRound()
    → GameService.endRound()
      → Avanza GameRoundStatus: FINISHED
    ← RoundDTO

Cliente → GET /api/games/{id}/results
  → GameController.getFinalResults()
    → GameService.getFinalResults()
      → Si FINISHED: devuelve puntajes totales
    ← GameDTO
```

### Comunicación WebSocket
```
Cliente → SockJS connect → /ws
  → Suscribe a /topic/gameUpdates
  → Envía a /app/updateGame  { GameUpdateDTO }
    → GameWebSocketController.handleGameUpdate()
      → Reenvía a /topic/gameUpdates
    ← Mensaje STOMP a todos los suscriptores
```

---

## Modelo Entidad-Relación

```
Game (1) ──── (N) GameQuestion (N) ──── (1) Question (1) ──── (N) Answer
  │
  └── (1) ──── (N) Participant
  │
  └── (1) ──── (N) GameRound
```

- **Game:** raíz del agregado. Contiene fecha, estado, equipo1/2 errores.
- **Question:** texto de la pregunta de encuesta.
- **Answer:** respuesta posible con `score` (porcentaje). Pertenece a una Question.
- **GameQuestion:** vincula una Question a un Game (evita preguntas repetidas en fechas recientes).
- **Participant:** jugador asociado a un Game, con nombre y equipo.
- **GameRound:** registro de un intento de respuesta de un Participant en un GameQuestion. Guarda el texto respondido, el score obtenido y el multiplicador.

---

## Decisiones Técnicas

### ¿Por qué MySQL y no H2?
El proyecto está diseñado para ejecutarse en un entorno real con persistencia. H2 se recomienda solo para pruebas locales; la configuración activa apunta a MySQL.

### ¿Por qué tres preguntas por partida?
Por simplicidad inicial. En el formato televisivo real, el número de preguntas varía; este valor puede hacerse configurable en el futuro.

### ¿Por qué WebSocket + STOMP + SockJS?
STOMP ofrece un modelo de suscripción/tópico limpio sobre WebSocket. SockJS proporciona fallbacks automáticos (long-polling, etc.) para clientes que no soporten WebSocket nativo.

### DTOs separados de Entidades
Se evita exponer las entidades JPA directamente al cliente. Los DTOs actúan como contratos de API, desacoplando la capa de persistencia de la de presentación.
