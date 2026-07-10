# Plan: UI/UX Rediseño Completo — 100 Mexicanos Dijeron

> **Fecha:** Julio 2026
> **Estado:** Completado
> **Modelo:** Host-control (una persona administra el juego desde una sola pantalla)

---

## Objetivo

Rediseñar la interfaz del juego con estética de programa de televisión, controles centralizados para el host, pantalla de resultados dedicada, efectos de sonido, y animaciones premium.

---

## Decisiones de Diseño

| Decisión | Elección |
|----------|----------|
| Control | Host-control (un host maneja todo desde una pantalla) |
| Pantalla de resultados | Sí, dedicada al final del juego |
| Sonidos | Web Audio API (sin dependencias externas) |
| Estética | Aggressive TV show-style (confetti, glow, animaciones fuertes) |
| Fuentes | Poppins (body) + Orbitron (scores/números) |
| Colores | `#006847` (verde), `#CE1126` (rojo), `#FFD700` (dorado), fondos oscuros |
| WebSocket | Los espectadores ven en tiempo real, solo el host controla |

---

## Arquitectura del Juego (Modelo Host)

```
┌─────────────────────────────────────────────────┐
│                  HOST (único)                    │
│                                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────┐ │
│  │  Setup       │→│  Game Board │→│ Results  │ │
│  │  Equipos     │  │  Control    │  │ Screen   │ │
│  └─────────────┘  └─────────────┘  └──────────┘ │
│                                                  │
│  Botones: Iniciar Ronda | Responder | Pasar     │
│           Terminar Ronda | Siguiente Ronda      │
└─────────────────────────────────────────────────┘
         │ WebSocket broadcast
         ▼
┌─────────────────────────────────────────────────┐
│        ESPECTADORES (solo lectura)               │
│  Ven el mismo game board en tiempo real          │
└─────────────────────────────────────────────────┘
```

---

## FASE 0: Correcciones Backend Críticas ✅ COMPLETADO

### 0.1 — `GameService.java`: Fix `endRound()`

**Problema actual:** Asigna puntos a team1 durante STEAL_ATTEMPT sin importar quién tiene control.

**Fix:**
```java
// endRound() — cambiar lógica de asignación de puntos
if (game.getControllingTeam() != null) {
    if (game.getControllingTeam() == 1) {
        game.setTeam1Score(game.getTeam1Score() + game.getCurrentRoundPoints());
    } else {
        game.setTeam2Score(game.getTeam2Score() + game.getCurrentRoundPoints());
    }
}
```

### 0.2 — `GameService.java`: Fix `passTurn()`

**Problema actual:** Resetea `team1Errors=0, team2Errors=0`.

**Fix:** Eliminar las líneas de reset de errores. Solo cambiar estado a `STEAL_ATTEMPT`.

### 0.3 — `GameService.java`: Fix `startNextRound()` auto-finish

**Problema actual:** Lanza excepción cuando no hay más rondas.

**Fix:** Cuando `currentIndex + 1 >= gameQuestions.size()`:
```java
game.setStatus(GameStatus.FINISHED);
game.setCurrentRoundStatus(GameRoundStatus.FINISHED);
gameRepository.save(game);
eventPublisher.publishEvent(new GameEvent("GAME_FINISHED", gameId));
return mapToGameDTO(game); // NO lanzar excepción
```

### 0.4 — `GameDTO.java`: Agregar campos para el frontend

```java
private String winner;           // "team1", "team2", "draw", null
private String gameQuestionText; // Texto de la pregunta actual
private List<AnswerDTO> currentAnswers; // Respuestas con revealed status
```

### 0.5 — `GameService.java`: Enriquecer `mapToGameDTO()`

```java
// Calcular winner al final del juego
if (game.getStatus() == GameStatus.FINISHED) {
    if (game.getTeam1Score() > game.getTeam2Score()) dto.setWinner("team1");
    else if (game.getTeam2Score() > game.getTeam1Score()) dto.setWinner("team2");
    else dto.setWinner("draw");
}

// Incluir pregunta actual si existe
if (game.getCurrentGameQuestion() != null) {
    dto.setGameQuestionText(game.getCurrentGameQuestion().getQuestion().getText());
    dto.setCurrentAnswers(getCurrentAnswersForQuestion(game));
}
```

### 0.6 — `GameService.java`: Nuevo método `revealAnswer()`

```java
@Transactional
public GameQuestionDTO revealAnswer(Long gameId, Long answerId) {
    // Marcar la respuesta como revealed en el contexto del juego
    // Retornar el GameQuestionDTO actualizado
}
```

### 0.7 — `GameController.java`: Nuevo endpoint

```java
@PostMapping("/{gameId}/rounds/reveal/{answerId}")
public ResponseEntity<GameQuestionDTO> revealAnswer(
        @PathVariable Long gameId, @PathVariable Long answerId) {
    return ResponseEntity.ok(gameService.revealAnswer(gameId, answerId));
}
```

---

## FASE 1: Rediseño de `play.html` — Game Board estilo TV

### Estructura de la pantalla

```
┌──────────────────────────────────────────────────────────┐
│  NAVBAR: "100 Mexicanos Dijeron"  │  Ronda 1/3  │ Salir │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  ┌─────────────┐    ┌──────────┐    ┌─────────────┐     │
│  │  EQUIPO 1   │    │  VS      │    │  EQUIPO 2   │     │
│  │  [SCORE]    │    │  R1 x1   │    │  [SCORE]    │     │
│  │  ✗ ✗ ✗     │    │          │    │  ✗ ✗ ✗     │     │
│  │  [CONTROL]  │    │          │    │             │     │
│  └─────────────┘    └──────────┘    └─────────────┘     │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │          PREGUNTA: "¿Qué harías si te...?"       │   │
│  ├──────────────────────────────────────────────────┤   │
│  │  1. [████████████]        [████] pts             │   │
│  │  2. [████████████]        [████] pts             │   │
│  │  3. [████████████]        [████] pts             │   │
│  │  4. [████████████]        [████] pts             │   │
│  │  5. [████████████]        [████] pts             │   │
│  │  6. [████████████]        [████] pts             │   │
│  └──────────────────────────────────────────────────┘   │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │  TURN INDICATOR: "Turno del Equipo 1"            │   │
│  │  Puntos en juego: 45 pts                         │   │
│  └──────────────────────────────────────────────────┘   │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │  INPUT: [Escribe la respuesta...] [Responder]    │   │
│  │  [Pasar Turno]  [Terminar Ronda]                 │   │
│  └──────────────────────────────────────────────────┘   │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │  CONTROLES DEL HOST:                             │   │
│  │  [Iniciar Ronda] [Siguiente Ronda] [Resultados] │   │
│  └──────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
```

### Estados de la pantalla

| Estado | Muestra | Controles visibles |
|--------|---------|-------------------|
| `NOT_STARTED` + `!setupComplete` | Setup de equipos | "Agregar miembros" + "Iniciar Juego" |
| `NOT_STARTED` + `setupComplete` | Pantalla de bienvenida | "Iniciar Ronda" |
| `IN_PROGRESS` + `TURN_PLAYER1/2` | Game board + input | Input + "Responder" + "Pasar Turno" + "Error" |
| `IN_PROGRESS` + `STEAL_ATTEMPT` | Game board + alerta robo | Respuestas clickeables "CLIC PARA ROBAR" + "Terminar Ronda" |
| `FINISHED` (ronda) | Game board + "Ronda terminada" | "Siguiente Ronda" |
| `FINISHED` (juego) | Pantalla resultados | "Volver al Dashboard" |

### Alpine.js — State del componente

```javascript
function gameBoard() {
    return {
        // === STATE DEL JUEGO ===
        gameId: null,
        status: 'NOT_STARTED',
        turn: 'NOT_STARTED',
        team1Score: 0,
        team2Score: 0,
        team1Errors: 0,
        team2Errors: 0,
        currentRoundPoints: 0,
        roundsPlayed: 0,
        totalRounds: 3,
        teamSize: 5,
        currentMultiplier: 1,
        controllingTeam: null,
        winner: null,

        // === PREGUNTA ACTUAL ===
        currentQuestion: null,

        // === INPUT DEL HOST ===
        answerText: '',
        loading: false,
        lastResult: null,

        // === SETUP ===
        setupComplete: false,
        team1Members: [],
        team2Members: [],
        team1Participants: [],
        team2Participants: [],

        // === CONTROL DEL FLUJO ===
        roundFinished: false,
        showResults: false,
        stompClient: null,

        // === COMPUTED ===
        get turnMessage() { ... },
        get isHostMode() { return true; },

        // === METHODS ===
        init() { ... },
        connectWebSocket() { ... },
        updateGameState(game) { ... },
        loadParticipants() { ... },
        saveParticipants() { ... },
        startRound() { ... },
        submitAnswer() { ... },
        passTurn() { ... },
        endRound() { ... },
        getResults() { ... },
        loadCurrentQuestion() { ... },
    }
}
```

### HTML Structure (secciones principales)

```html
<!-- SECTION 1: SETUP DE EQUIPOS -->
<div x-show="status === 'NOT_STARTED' && !setupComplete">
    <!-- Formulario de miembros por equipo -->
</div>

<!-- SECTION 2: GAME BOARD -->
<div x-show="setupComplete && status !== 'FINISHED'">
    <!-- Scores + Strikes + Turn Indicator + Question Board + Input -->
</div>

<!-- SECTION 3: PANTALLA DE RESULTADOS -->
<div x-show="status === 'FINISHED' && showResults">
    <!-- Ganador + Scores finales + Stats + Botones -->
</div>
```

---

## FASE 2: CSS Premium — Estilo TV Show

### 2.1 — Google Fonts

```css
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;600;700;900&family=Orbitron:wght@700;900&display=swap');
```

### 2.2 — Variables globales

```css
:root {
    --green: #006847;
    --red: #CE1126;
    --gold: #FFD700;
    --dark-bg: #0a0a1a;
    --board-bg: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
    --card-bg: rgba(255, 255, 255, 0.05);
    --text-primary: #ffffff;
    --text-secondary: rgba(255, 255, 255, 0.7);
    --glow-green: 0 0 20px rgba(0, 104, 71, 0.6);
    --glow-red: 0 0 20px rgba(206, 17, 38, 0.6);
    --glow-gold: 0 0 20px rgba(255, 215, 0, 0.6);
}
```

### 2.3 — Keyframe Animations

```css
/* Score subiendo */
@keyframes scoreUp {
    0% { transform: scale(1); }
    50% { transform: scale(1.3); color: var(--gold); }
    100% { transform: scale(1); }
}

/* Strike apareciendo */
@keyframes strikeHit {
    0% { transform: scale(0) rotate(-180deg); opacity: 0; }
    60% { transform: scale(1.2) rotate(10deg); opacity: 1; }
    100% { transform: scale(1) rotate(0deg); opacity: 1; }
}

/* Respuesta revelándose */
@keyframes revealSlide {
    0% { transform: translateX(-100%); opacity: 0; }
    100% { transform: translateX(0); opacity: 1; }
}

/* Turno activo pulsando */
@keyframes activePulse {
    0%, 100% { box-shadow: 0 0 5px rgba(255, 215, 0, 0.3); }
    50% { box-shadow: 0 0 25px rgba(255, 215, 0, 0.7); }
}

/* Confetti */
@keyframes confettiFall {
    0% { transform: translateY(-100vh) rotate(0deg); opacity: 1; }
    100% { transform: translateY(100vh) rotate(720deg); opacity: 0; }
}

/* Shake para errores */
@keyframes shake {
    0%, 100% { transform: translateX(0); }
    10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
    20%, 40%, 60%, 80% { transform: translateX(5px); }
}

/* Glow para equipo activo */
@keyframes glowPulse {
    0%, 100% { border-color: rgba(255, 215, 0, 0.3); }
    50% { border-color: rgba(255, 215, 0, 0.9); }
}
```

### 2.4 — Componentes CSS

```css
/* Tablero de respuestas estilo Family Feud */
.answer-slot {
    background: var(--card-bg);
    border: 2px solid rgba(255, 255, 255, 0.15);
    border-radius: 8px;
    padding: 12px 20px;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    transition: all 0.4s ease;
}

.answer-slot.revealed {
    background: rgba(0, 104, 71, 0.2);
    border-color: var(--green);
    animation: revealSlide 0.5s ease-out;
}

.answer-slot.hidden {
    background: rgba(255, 255, 255, 0.03);
    border-color: rgba(255, 255, 255, 0.1);
}

.answer-slot .slot-number {
    background: var(--green);
    color: white;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    margin-right: 16px;
    flex-shrink: 0;
}

.answer-slot .slot-text {
    flex: 1;
    font-size: 1.1rem;
    font-weight: 600;
    color: white;
}

.answer-slot .slot-points {
    font-family: 'Orbitron', sans-serif;
    font-size: 1.2rem;
    font-weight: 700;
    color: var(--gold);
    margin-left: 16px;
}

/* Score display grande */
.score-display {
    font-family: 'Orbitron', sans-serif;
    font-size: 3.5rem;
    font-weight: 900;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

/* Strike visual */
.strike {
    font-size: 2rem;
    font-weight: 900;
    transition: all 0.3s;
}

.strike.active {
    color: #FF4444;
    text-shadow: 0 0 15px rgba(255, 68, 68, 0.7);
    animation: strikeHit 0.5s ease-out;
}

.strike.inactive {
    color: rgba(255, 255, 255, 0.15);
}

/* Input del host */
.host-input {
    background: rgba(255, 255, 255, 0.1);
    border: 2px solid rgba(255, 255, 255, 0.2);
    border-radius: 12px;
    color: white;
    font-size: 1.2rem;
    padding: 16px 20px;
    transition: border-color 0.3s;
}

.host-input:focus {
    border-color: var(--gold);
    box-shadow: 0 0 15px rgba(255, 215, 0, 0.3);
    outline: none;
}

/* Botón de acción */
.btn-action {
    font-family: 'Poppins', sans-serif;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 1px;
    border-radius: 12px;
    padding: 12px 30px;
    transition: all 0.3s;
}

.btn-action.primary {
    background: linear-gradient(135deg, var(--green), #00a86b);
    color: white;
    border: none;
}

.btn-action.primary:hover {
    transform: translateY(-2px);
    box-shadow: var(--glow-green);
}

.btn-action.danger {
    background: linear-gradient(135deg, var(--red), #ff3355);
    color: white;
    border: none;
}

.btn-action.warning {
    background: linear-gradient(135deg, #ff9500, #ffb340);
    color: white;
    border: none;
}

/* Pantalla de resultados */
.results-screen {
    background: var(--board-bg);
    border-radius: 20px;
    padding: 3rem;
    text-align: center;
    position: relative;
    overflow: hidden;
}

.winner-display {
    font-family: 'Orbitron', sans-serif;
    font-size: 4rem;
    font-weight: 900;
    color: var(--gold);
    text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.5);
    animation: scoreUp 0.8s ease-out;
}
```

---

## FASE 3: Sonidos (Web Audio API)

### 3.1 — AudioManager (`static/js/audio.js`)

```javascript
const AudioManager = {
    ctx: null,

    init() {
        this.ctx = new (window.AudioContext || window.webkitAudioContext)();
    },

    playCorrect() {
        this.playTone([523.25, 659.25, 783.99], 0.15, 0.3);
    },

    playWrong() {
        this.playTone([311.13, 233.08], 0.2, 0.4);
    },

    playReveal() {
        this.playTone([880], 0.05, 0.15);
    },

    playStrike() {
        this.playTone([110], 0.3, 0.2);
    },

    playCelebration() {
        const notes = [523, 587, 659, 698, 784, 880, 988, 1047];
        notes.forEach((freq, i) => {
            setTimeout(() => this.playTone([freq], 0.1, 0.15), i * 100);
        });
    },

    playRoundStart() {
        this.playTone([440, 554, 659], 0.1, 0.4);
    },

    playTone(frequencies, volume, duration) {
        if (!this.ctx) this.init();
        frequencies.forEach(freq => {
            const osc = this.ctx.createOscillator();
            const gain = this.ctx.createGain();
            osc.connect(gain);
            gain.connect(this.ctx.destination);
            osc.frequency.value = freq;
            osc.type = 'sine';
            gain.gain.setValueAtTime(volume, this.ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + duration);
            osc.start(this.ctx.currentTime);
            osc.stop(this.ctx.currentTime + duration);
        });
    }
};
```

### 3.2 — Integración en `play.html`

```html
<script th:src="@{/js/audio.js}"></script>
<script>
// En gameBoard():
init() {
    AudioManager.init();
    // ... resto del init
},

submitAnswer() {
    if (correct) AudioManager.playCorrect();
    else AudioManager.playWrong();
},

startRound() {
    AudioManager.playRoundStart();
}
</script>
```

---

## FASE 4: Pantalla de Resultados

### 4.1 — Sección en `play.html`

```html
<div x-show="status === 'FINISHED' && showResults" class="results-screen">
    <div id="confetti-container"></div>

    <h2 class="text-white mb-4">¡Juego Terminado!</h2>

    <div class="winner-display mb-4">
        <span x-show="winner === 'team1'" style="color: var(--green);">🏆 ¡GANA EL EQUIPO 1!</span>
        <span x-show="winner === 'team2'" style="color: var(--red);">🏆 ¡GANA EL EQUIPO 2!</span>
        <span x-show="winner === 'draw'" style="color: var(--gold);">🤝 ¡EMPATE!</span>
    </div>

    <div class="row justify-content-center mb-4">
        <div class="col-md-4">
            <div class="team-card team-1">
                <h4>Equipo 1</h4>
                <div class="score-display" x-text="team1Score"></div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="team-card team-2">
                <h4>Equipo 2</h4>
                <div class="score-display" x-text="team2Score"></div>
            </div>
        </div>
    </div>

    <div class="mt-4">
        <a href="/dashboard" class="btn btn-action primary btn-lg me-3">Volver al Dashboard</a>
        <button class="btn btn-action warning btn-lg" @click="createNewGame">Jugar de Nuevo</button>
    </div>
</div>
```

### 4.2 — Confetti con CSS puro

```javascript
function createConfetti() {
    const container = document.getElementById('confetti-container');
    const colors = ['#006847', '#CE1126', '#FFD700', '#FFFFFF'];
    for (let i = 0; i < 100; i++) {
        const confetti = document.createElement('div');
        confetti.className = 'confetti-piece';
        confetti.style.left = Math.random() * 100 + '%';
        confetti.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
        confetti.style.animationDelay = Math.random() * 3 + 's';
        confetti.style.animationDuration = (Math.random() * 2 + 2) + 's';
        container.appendChild(confetti);
    }
}
```

```css
.confetti-piece {
    position: absolute;
    width: 10px;
    height: 10px;
    top: -10px;
    border-radius: 2px;
    animation: confettiFall linear forwards;
}
```

---

## FASE 5: Dashboard Rediseñado

### Cambios principales
- **Cards en vez de tabla** para cada juego
- **Juegos activos** primero, terminados al final
- **Botón "Nueva Partida"** prominente con modal
- **Juegos terminados** muestran "Ver Resultados" en vez de "Jugar"

### Game Card

```html
<div class="game-card" :class="{ 'game-finished': game.status === 'FINISHED' }">
    <div class="game-card-header">
        <span class="badge" :class="statusBadge(game.status)" x-text="game.status"></span>
        <small x-text="formatDate(game.date)"></small>
    </div>
    <div class="game-card-body">
        <div class="d-flex justify-content-between">
            <div class="text-center">
                <div class="team-label" style="color: var(--green);">Equipo 1</div>
                <div class="score-big" x-text="game.team1Score"></div>
            </div>
            <div class="text-center align-self-center">
                <div class="vs-text">VS</div>
                <div class="rounds-text" x-text="'R' + game.roundsPlayed + '/' + game.totalRounds"></div>
            </div>
            <div class="text-center">
                <div class="team-label" style="color: var(--red);">Equipo 2</div>
                <div class="score-big" x-text="game.team2Score"></div>
            </div>
        </div>
    </div>
    <div class="game-card-footer">
        <a :href="'/jugar/' + game.id" class="btn btn-action primary btn-sm"
           x-text="game.status === 'FINISHED' ? 'Ver Resultados' : 'Jugar'"></a>
    </div>
</div>
```

---

## FASE 6: Redirecciones Corregidas

### Mapa de navegación

```
/ (redirect) → /dashboard
/login → /dashboard (post-login)
/register → /login?registered
/logout → /login?logout

/dashboard
  ├── "Nueva Partida" → POST /api/games → /jugar/{id}
  ├── Game card "Jugar" → /jugar/{id}
  └── Game card "Ver Resultados" → /jugar/{id} (con showResults=true)

/jugar/{id}
  ├── Setup → Play → Results
  └── Results → "Volver al Dashboard" → /dashboard

/history → "Ver" → /jugar/{id}
/stats
/admin/questions → CRUD
```

### Cambios en `HomeController.java`

```java
@GetMapping("/jugar/{id}")
public String playGame(@PathVariable Long id,
        @RequestParam(defaultValue = "false") boolean results,
        Model model) {
    model.addAttribute("game", gameService.getGameById(id));
    model.addAttribute("showResults", results);
    return "play";
}
```

---

## Archivos a Modificar

| Archivo | Cambios |
|---------|---------|
| `GameService.java` | Fix endRound, passTurn, startNextRound; enrich mapToGameDTO; revealAnswer con puntos; incrementError |
| `GameDTO.java` | Agregar winner, gameQuestionText, currentAnswers |
| `GameController.java` | Agregar endpoints reveal, error |
| `HomeController.java` | Agregar param `results` a `/jugar/{id}` |
| `WebSocketConfig.java` | Fix CORS con setAllowedOriginPatterns |
| `WebSocketEventListener.java` | Logging completo |
| `play.html` | **Reescritura completa** — Game board estilo TV host-controlled |
| `dashboard.html` | Cards con separación activos/historial |
| `style.css` | **Reescritura completa** — Premium TV show |
| `history.html` | Ajustar formato de fecha |

## Archivos a Crear

| Archivo | Propósito |
|---------|-----------|
| `static/js/audio.js` | AudioManager con Web Audio API |

## Archivos sin cambios

| Archivo | Razón |
|---------|-------|
| `login.html` | Funciona correctamente |
| `register.html` | Funciona correctamente |
| `admin/*.html` | CRUD funcional |
| `stats.html` | Funcional |
| `SecurityConfig.java` | Sin cambios |

---

## Orden de Implementación ✅ COMPLETADO

1. **FASE 0** — Backend fixes (GameService, GameDTO) → compilar y testear ✅
2. **FASE 2** — CSS premium (style.css) → base visual ✅
3. **FASE 3** — AudioManager (audio.js) → sonidos ✅
4. **FASE 1** — play.html reescritura → game board host-controlled ✅
5. **FASE 4** — Resultados → pantalla de cierre ✅
6. **FASE 5** — Dashboard rediseñado → cards ✅
7. **FASE 6** — Redirecciones → HomeController + rutas ✅

### Features Adicionales Implementadas
- Mecánica de robo (STEAL_ATTEMPT) con equipo contrario robando puntos
- Botón ERROR/STRIKE con contador X/3
- Auto-endRound cuando todas las respuestas se revelan
- Logging completo (SLF4J + console.log)
- CORS WebSocket fix
- Dashboard con separación de juegos activos/historial
