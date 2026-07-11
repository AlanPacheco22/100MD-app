# Changelog

Todos los cambios notables en **100MD-app** estarán documentados en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es/1.1.0/),
y el proyecto adherisce a [Semantic Versioning](https://semver.org/lang/es/).

---

## [Unreleased]

### Changed
- Documentación: limpieza de docs de planeación completadas, actualización de CHANGELOG, ARCHITECTURE y OBJETIVOS

---

## [0.3.0] - 2026-07-10

Integración de librerías para reducir boilerplate y corrección de 20 bugs de auditoría post-implementación.

### Added
- **MapStruct 1.5.5**: 6 interfaces de mapeo DTO↔Entity (`GameMapper`, `GameQuestionMapper`, `AnswerMapper`, `ParticipantMapper`, `FastMoneyAnswerMapper`, `GameHistoryMapper`), eliminando ~97 líneas de mapeo manual en `GameService`, `StatsService` y `FastMoneyService`
- **IntArrayConverter** (`AttributeConverter<int[], String>`): conversión automática de `roundMultipliers` en `Game.java`, eliminando `getMultipliersArray()`/`setMultipliersArray()`
- **canvas-confetti@1.9.3** via CDN: reemplaza `createConfetti()` manual con efectos superiores y soporte `disableForReducedMotion`
- **Alpine.data() refactor**: `game-timer.js` (timer mixin, 54 líneas) y `fast-money.js` (fast money mixin, 135 líneas) extraídos de play.html
- **GameHistoryMapper**: mapeo automático Game→GameHistoryDTO para el endpoint de historial
- Flyway migrations V12 (`round_multipliers`) y V13 (`face_off_status_to_round_enum`)

### Changed
- `Game.roundMultipliers` cambiado de `String` a `int[]` con `@Convert(converter = IntArrayConverter.class)`
- `GameService` usa `GameMapper`, `GameQuestionMapper`, `AnswerMapper` en vez de mapeo manual
- `StatsService` usa `GameHistoryMapper` en vez de mapeo manual
- `FastMoneyService` usa `FastMoneyAnswerMapper` en vez de mapeo manual
- play.html reducido de 1475 a 1297 líneas (-178 líneas, -12%)
- Confetti CSS eliminado de `style.css` (reemplazado por canvas-confetti)

### Fixed
- **20 bugs de auditoría post-implementación:**
  - `saveParticipants()` llamado 2 veces (step 1 + step 2)
  - `startRound()` se ejecuta antes de captain selection
  - `suddenDeathAnswer()` siempre incrementa `team1Errors`
  - `buzzIn()` usa `indexOf()` con referencia en vez de ID
  - `Long` comparado con `!=` en vez de `.equals()` (3 lugares)
  - `ANSWER_WRONG` handler ejecuta `loadCurrentQuestion()` 2 veces
  - Timer nunca inicia en flujo normal (solo en refresh)
  - Timer no se detiene durante STEAL_ATTEMPT / ROUND_ENDED
  - `ParticipantDTO` sin campo `captain` (se pierde en refresh)
  - Fast Money timers nunca decrementan
  - Fast Money 100% mock — nunca llama al backend
  - Multiplicadores custom siempre hardcodeados
  - BUZZ IN activo cuando no hay jugador seleccionado
  - Fast Money permite mismo jugador 2 veces
  - Fast Money muestra resultados random
  - `x-cloak` falta en sección de setup
  - `handleCorrectAnswer()` steal solo funciona para team 2
  - `mapToGameDTO()` omite `faceOffPlayer1/2`
  - `.fast-money-screen` sin `position: relative`
  - 5 eventos WebSocket sin handler
- `history.html`: fix `#temporals.format` en String → formato de fecha en `StatsService`
- WebSocket handler: fix syntax error con 2 `}` extra que rompían `gameBoard()`
- STOMP/ENUM: downgrade CDN STOMP, migración a V13, timer guard

### Dependencies
- `org.mapstruct:mapstruct:1.5.5`
- `org.mapstruct:mapstruct-processor:1.5.5` (annotation processor)
- `org.bsc.maven:maven-processor-plugin` + `org.mapstruct:lombok-mapstruct-binding:0.2.0`
- `org.springframework.boot:spring-boot-testcontainers` (dependencies only, sin Docker)
- `org.testcontainers:mysql` (dependencies only, sin Docker)
- `canvas-confetti@1.9.3` (CDN)

---

## [0.2.0] - 2026-07-10

Implementación completa de reglas oficiales del programa y rediseño UI/UX estilo TV show.

### Added
- **Cara a Cara (Face-Off)**: 1v1 entre jugadores con buzzer, timer y selección ganador por el host
- **Turnos individuales**: miembros del equipo responden en orden secuencial, avance automático
- **Capitán designado**: campo `isCaptain` en Participant, selección en UI antes de iniciar
- **Muerte Súbita**: tiebreaker después de 5 rondas si nadie llega a 300 puntos, 1 solo strike
- **Dinero Rápido**: 2 jugadores, 5 preguntas cada uno (15s y 20s), bonus 200 puntos
- **Timer por turno**: configurable (`timerEnabled`, `turnTimeLimit`), countdown visual con sonido
- **Reglas oficiales del TV show**: 5 rondas (x1,x1,x2,x2,x3), victoria a 300 puntos
- **8 sonidos nuevos** con Web Audio API: buzzer, tick, timerWarning, timerEnd, fastMoneyStart, fastMoneyDing, fastMoneyEnd, suddenDeath
- **8 eventos WebSocket nuevos**: `FACE_OFF_STARTED`, `FACE_OFF_ANSWERED`, `SUDDEN_DEATH_STARTED`, `SUDDEN_DEATH_FACE_OFF`, `SUDDEN_DEATH_WON`, `FAST_MONEY_STARTED`, `FAST_MONEY_PLAYER1_DONE`, `FAST_MONEY_WON`/`FAST_MONEY_LOST`
- **FastMoneyRound entity** + `FastMoneyService` + `FastMoneyController` + `FastMoneyRoundRepository`
- **FastMoneyDTO**, **FastMoneyAnswerDTO**, **FastMoneySubmissionDTO**, **BuzzInDTO**, **GameHistoryDTO**, **PlayerStatsDTO**, **CreateGameDTO**
- **GameHistoryController** + **StatsController**: endpoints de historial y estadísticas
- **GameOverException**: excepción para control de fin de juego
- `game-timer.js`: componente timer mixin reutilizable
- `fast-money.js`: componente fast money mixin reutilizable

### Changed
- **play.html reescritura completa** (~1475 líneas): game board host-controlled estilo TV show con setup, face-off, turnos, steal, sudden death, fast money, resultados
- **style.css reescritura completa** (~1132 líneas): premium TV show aesthetic con variables CSS, keyframe animations, componentes reutilizables
- **dashboard.html rediseñado**: cards con separación de juegos activos/historial, modal de configuración mejorado (targetScore, timerEnabled, turnTimeLimit, roundMultipliers)
- **GameService.java** (~1092 líneas): lógica completa Family Feud con face-off, turnos, steal, sudden death, fast money, timer
- **Game.java**: campos `totalRounds`, `teamSize`, `currentMultiplier`, `controllingTeam`, `faceOffPlayer1`, `faceOffPlayer2`, `targetScore`, `timerEnabled`, `turnTimeLimit`, `roundMultipliers`, `currentTurnIndex`
- **GameRoundStatus** enum: nuevos valores `FACE_OFF`, `STEAL_ATTEMPT`, `SUDDEN_DEATH_FACE_OFF`
- **GameStatus** enum: nuevos valores `SUDDEN_DEATH`, `FAST_MONEY`
- **ParticipantDTO**: campo `captain`
- **GameDTO**: campos `winner`, `gameQuestionText`, `currentAnswers`, `targetScore`, `roundMultipliers`, `timerEnabled`, `turnTimeLimit`, `faceOffPlayer1`, `faceOffPlayer2`

### Fixed
- CORS WebSocket: `setAllowedOrigins("*")` → `setAllowedOriginPatterns("http://localhost:*")`

---

## [0.1.0] - 2026-07-10

Versión inicial funcional del juego "100 Mexicanos Dijeron" con UI premium estilo TV show.

### Added

#### Juego
- Lógica Family Feud con control de turnos y rondas
- Configuración flexible de partidas (rondas, multiplicadores, miembros por equipo)
- Mecánica de robo (STEAL_ATTEMPT): equipo contrario puede robar puntos al llegar a 3 errores
- Host-control: host revela respuestas haciendo clic, sin input de texto
- Botón ERROR/STRIKE con contador X/3
- Auto-endRound cuando todas las respuestas se revelan

#### Interfaz
- UI premium estilo TV show con Poppins + Orbitron
- Colores mexicanos: verde `#006847`, rojo `#CE1126`, dorado `#FFD700`
- Efectos de confetti, glow y animaciones
- Dashboard rediseñado con cards y separación de juegos activos/historial
- Navegación consistente con navbar oscura en todas las páginas
- Pantalla de resultados dedicada al final del juego

#### Sonidos
- Sonidos con Web Audio API: correcto, incorrecto, robo, celebración, reveal, roundStart, roundEnd

#### Backend
- WebSocket con STOMP y SockJS para broadcast en tiempo real
- Admin panel con CRUD de preguntas y respuestas
- Game history y player statistics
- Login y módulos web (Spring Security, Thymeleaf, Bootstrap)
- Base de datos con Flyway migrations y seed data
- Docker deployment
- Tests con H2 y GitHub Actions CI
- Documentación Swagger/OpenAPI
- Logging completo (SLF4J backend + console.log frontend)

### Fixed
- CORS WebSocket: `setAllowedOrigins("*")` → `setAllowedOriginPatterns("http://localhost:*")`
- Bugs críticos en lógica del juego + logs debug
- Fix duplicate endpoint
- Corrección del Core — el proyecto compila

### Changed
- Dashboard rediseñado con cards
- play.html reescritura completa — game board estilo TV
- Navegación consistente con navbar oscura
- CSS reescritura completa — premium TV show aesthetic
- Arquitectura limpia

---

[unreleased]: https://github.com/AlanPacheco22/100MD-app/compare/v0.3.0...HEAD
[0.3.0]: https://github.com/AlanPacheco22/100MD-app/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/AlanPacheco22/100MD-app/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/AlanPacheco22/100MD-app/releases/tag/v0.1.0
