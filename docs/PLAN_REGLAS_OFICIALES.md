# Plan: Adaptación Reglas Oficiales — 100 Mexicanos Dijeron

> **Fecha:** Julio 2026
> **Estado:** Backend + Frontend completados ✅
> **Fuente:** Reglas oficiales del programa de televisión "100 Mexicanos Dijeron"

---

## Comparación: Reglas Oficiales vs App Actual

| Regla Oficial | Estado | Notas |
|---|---|---|
| 5 rondas (x1, x1, x2, x2, x3) | ✅ Completado | Defaults 5 rondas, multiplicadores correctos |
| Victoria a 300 puntos | ✅ Completado | `targetScore=300`, victoria mid-game detectada |
| **Cara a Cara** (careo inicial) | ✅ Completado | `startFaceOff()`, `buzzIn()`, endpoints creados |
| **Control de equipo** (cadena de respuestas) | ✅ Completado | Turnos individuales con `currentTurnIndex` |
| 3 strikes → robo | ✅ Sí | Implementado |
| **Capitán** designado | ✅ Completado | Campo `isCaptain` en Participant |
| Robo solo del capitán | ⚠️ Pendiente | Requiere validación en frontend |
| **Muerte Súbita** | ✅ Completado | `SUDDEN_DEATH` status, 1 solo strike |
| **Dinero Rápido** | ✅ Completado | 2 jugadores, 5 preguntas cada uno, bonus 200 pts |
| **Timer** por turno | ✅ Completado | `timerEnabled`, `turnTimeLimit`, configurable |
| **Host-control** (revelar respuestas) | ✅ Sí | Implementado |

---

## FASE 1: Core — Corregir estructura básica

**Objetivo:** Que el juego tenga 5 rondas con multiplicadores correctos y victoria a 300 puntos.

### Archivos a modificar

| Archivo | Cambios |
|---|---|
| `Game.java` | Default `totalRounds=5`, agregar `targetScore=300` |
| `GameService.java` | Fix `getMultipliersForGame()` para usar `[1,1,2,2,3]` |
| `GameService.java` | Modificar `getFinalResults()` para detectar si alguien pasó de 300 mid-game |
| `GameDTO.java` | Agregar `targetScore`, `roundMultipliers[]` |

### Criterios de verificación
- [x] Juego tiene 5 rondas por defecto
- [x] Multiplicadores son [1,1,2,2,3]
- [x] Si un equipo pasa 300 puntos durante el juego, gana inmediatamente
- [x] `./mvnw compile` pasa sin errores

---

## FASE 2: Cara a Cara (Face-to-Face)

**Objetivo:** Implementar careo inicial entre un jugador de cada equipo.

### Archivos a modificar/crear

| Archivo | Cambios |
|---|---|
| `GameRoundStatus.java` | Nuevo valor: `FACE_OFF` |
| `Game.java` | Campo `faceOffPlayer1`, `faceOffPlayer2` |
| `GameService.java` | Método `startFaceOff(gameId, player1Id, player2Id)` |
| `GameService.java` | Método `buzzIn(gameId, participantId, answerText)` |
| `GameController.java` | Endpoint `POST /{id}/faceoff/start` y `POST /{id}/faceoff/buzz` |

### Flujo
```
1. Host selecciona 1 jugador de cada equipo
2. Se muestra la pregunta
3. Jugadores "pulsan" (click rápido) para ganar derecho
4. El primero en pulsar responde:
   - Si acierta respuesta #1 → su equipo gana control
   - Si no → el otro puede intentar con respuesta de mayor rango
5. Si ninguno acierta → careo entre otros miembros
```

### Criterios de verificación
- [x] Dos jugadores pueden enfrentarse en careo
- [x] El primero en pulsar gana derecho a responder
- [x] Si acierta la respuesta #1, su equipo toma control
- [x] Si falla, el otro jugador puede intentar
- [x] `./mvnw compile` pasa sin errores

---

## FASE 3: Turnos individuales dentro del equipo

**Objetivo:** Los miembros del equipo responden uno por uno en orden.

### Archivos a modificar

| Archivo | Cambios |
|---|---|
| `Game.java` | Campo `currentTurnIndex` (índice del jugador activo) |
| `Participant.java` | Campo `isCaptain` (boolean) |
| `GameService.java` | Modificar `submitAnswer()` para avanzar al siguiente jugador automáticamente |
| `GameService.java` | Método `passTurn()` solo para el equipo con control |

### Criterios de verificación
- [x] Cada jugador del equipo responde en orden secuencial
- [x] Después de cada respuesta, avanza al siguiente jugador
- [x] El capitán puede pasar el turno
- [x] `./mvnw compile` pasa sin errores

---

## FASE 4: Muerte Súbita

**Objetivo:** Desempate cuando nadie llega a 300 después de 5 rondas.

### Archivos a modificar/crear

| Archivo | Cambios |
|---|---|
| `GameStatus.java` | Nuevo valor: `SUDDEN_DEATH` |
| `GameRoundStatus.java` | Nuevo valor: `SUDDEN_DEATH_FACE_OFF` |
| `GameService.java` | Método `startSuddenDeath(gameId)` |
| `GameService.java` | Lógica: 1 solo strike, si falla el otro roba |

### Flujo
```
1. Después de 5 rondas, si team1 < 300 Y team2 < 300
2. Se activa MUERTE SÚBITA
3. Careo entre capitanes
4. 1 solo strike permitido
5. Si el equipo en control falla → robo automático
```

### Criterios de verificación
- [x] Se activa cuando nadie llega a 300 tras 5 rondas
- [x] Careo entre capitanes funciona
- [x] Solo 1 strike permitido
- [x] Si falla, el otro equipo roba automáticamente
- [x] `./mvnw compile` pasa sin errores

---

## FASE 5: Dinero Rápido

**Objetivo:** Ronda de bonificación para el equipo ganador.

### Archivos a crear

| Archivo | Propósito |
|---|---|
| `FastMoneyState.java` | Entity para guardar estado del Dinero Rápido |
| `FastMoneyService.java` | Lógica del juego rápido |
| `FastMoneyController.java` | Endpoints para el flujo |
| `FastMoneyRepository.java` | Acceso a datos |

### Archivos a modificar

| Archivo | Cambios |
|---|---|
| `GameStatus.java` | Nuevo valor: `FAST_MONEY` |

### Flujo
```
1. Equipo ganador elige 2 jugadores
2. Jugador 1: 5 preguntas, 15 segundos
3. Jugador 2: mismas 5 preguntas, 20 segundos
   - Si repite respuesta del jugador 1 → invalida, debe dar otra
4. Si total ≥ 200 puntos → gana bonificación
```

### Criterios de verificación
- [x] Equipo puede seleccionar 2 jugadores
- [x] Timer de 15s para jugador 1, 20s para jugador 2
- [x] Respuestas repetidas se invalidan
- [x] Si total ≥ 200, gana bonificación
- [x] `./mvnw compile` pasa sin errores

---

## FASE 6: Timer / Contador regresivo

**Objetivo:** Límite de tiempo por turno (configurable, puede desactivarse).

### Archivos a modificar

| Archivo | Cambios |
|---|---|
| `Game.java` | Campos `turnTimeLimit` (segundos, default 10), `timerEnabled` (boolean, default true) |
| `CreateGameDTO.java` | Campo `timerEnabled` para configurar al crear partida |
| `GameDTO.java` | Campo `timerEnabled` para enviar al frontend |
| `play.html` | Countdown visual con Web Audio API, ocultar timer si `timerEnabled=false` |
| `GameService.java` | Si `timerEnabled=true` y se acaba el tiempo → strike automático |

### Opciones de configuración
| Opción | Default | Descripción |
|---|---|---|
| `timerEnabled` | `true` | Activa/desactiva el timer por turno |
| `turnTimeLimit` | `10` | Segundos por turno (solo aplica si `timerEnabled=true`) |

### Flujo del timer
```
1. Al inicio de cada turno, inicia countdown
2. Si timerEnabled=false → sin límite de tiempo
3. Si timerEnabled=true:
   - Muestra countdown visual
   - Sonido de ticking al quedar 3 segundos
   - Si llega a 0 → strike automático y pasa al siguiente jugador
```

### Criterios de verificación
- [x] Timer visual aparece durante el turno (si está activado)
- [x] Timer NO aparece si `timerEnabled=false`
- [x] Sonido de ticking al acercarse al final (solo si timer activado)
- [x] Si se acaba el tiempo → strike automático (solo si timer activado)
- [x] Host puede desactivar timer al crear partida
- [x] `./mvnw compile` pasa sin errores

---

## FASE 7: UI/UX — Adaptar pantallas

**Objetivo:** Actualizar frontend para soportar todas las fases del juego.

### Estado actual del frontend
- **play.html**: 792 líneas, pantalla monolítica con setup, game board, resultados
- **Templates existentes**: login, register, dashboard, play, history, stats, admin/*
- **CSS**: 686 líneas, sistema de diseño con Poppins + Orbitron, colores MX
- **JS**: audio.js (Web Audio API), Alpine.js para estado reactivo
- **WebSocket**: SockJS + STOMP, suscripción a `/topic/game/{id}`

---

### 7.1 — Selector de Capitán

**Objetivo:** Cada equipo designa un capitán antes de iniciar.

| Archivo | Cambios |
|---|---|
| `play.html` | Nuevo paso en setup: dropdown para seleccionar capitán por equipo |
| `GameService.java` | Método `setCaptain(gameId, participantId)` |
| `GameController.java` | Endpoint `POST /{id}/participants/{pid}/captain` |

**Flujo UI:**
```
1. Setup normal (agregar miembros)
2. Paso 2: "Selecciona el capitán de cada equipo"
   - Dropdown con nombres del equipo
   - Estrella/oro en el capitán seleccionado
3. Botón "INICIAR JUEGO" solo disponible después de seleccionar ambos capitanes
```

- [ ] Backend: Endpoint `setCaptain()` implementado
- [ ] Frontend: Dropdown de selección por equipo
- [ ] Frontend: Botón deshabilitado hasta seleccionar ambos
- [ ] `./mvnw compile` pasa sin errores

---

### 7.2 — Componente Timer reutilizable

**Objetivo:** Countdown visual circular para todas las fases con timer.

| Archivo | Cambios |
|---|---|
| `style.css` | Clases: `.timer-ring`, `.timer-circle`, `.timer-warning`, `.timer-critical` |
| `play.html` | Componente SVG circular con countdown |

**Especificaciones:**
- **Visual**: Anillo SVG circular con progreso
- **Colores**: Verde (>5s) → Amarillo (3-5s) → Rojo (<3s)
- **Animaciones**: Pulse en últimos 3 segundos
- **Sonido**: `playTick()` cada segundo, `playTimerWarning()` en 3s, `playTimerEnd()` en 0s
- **Configurable**: Se oculta si `timerEnabled=false`

- [ ] CSS: Anillo SVG con transiciones de color
- [ ] HTML: Componente countdown reutilizable
- [ ] JavaScript: Lógica de countdown sincronizada con backend
- [ ] Se oculta correctamente cuando `timerEnabled=false`

---

### 7.3 — Pantalla de Cara a Cara (Face-Off)

**Objetivo:** 1v1 entre un jugador de cada equipo, estilo "buzzer".

| Archivo | Cambios |
|---|---|
| `play.html` | Nueva sección `x-show="turn === 'FACE_OFF'"` |
| `audio.js` | Nuevo sonido: `playBuzzer()` |
| `style.css` | Estilos: `.faceoff-screen`, `.faceoff-vs`, `.faceoff-player`, `.faceoff-buzzer` |

**Layout:**
```
┌──────────────────────────────────────────────────┐
│              CAR A CARA                          │
│                                                  │
│  ┌─────────────┐    VS    ┌─────────────┐       │
│  │  JUGADOR 1  │         │  JUGADOR 2  │       │
│  │  (Equipo 1) │         │  (Equipo 2) │       │
│  └─────────────┘         └─────────────┘       │
│                                                  │
│  ┌──────────────────────────────────────────┐   │
│  │  PREGUNTA: "¿Cuál es la comida...?"      │   │
│  └──────────────────────────────────────────┘   │
│                                                  │
│  ┌──────────────────────────────────────────┐   │
│  │  [  ⚡ BUZZ IN  ]  (botón grande)        │   │
│  │  Timer: 10s                               │   │
│  └──────────────────────────────────────────┘   │
│                                                  │
│  Host: Selecciona ganador si ambos pulsaron      │
└──────────────────────────────────────────────────┘
```

**Estados UI:**
| Estado | Acción |
|---|---|
| `FACE_OFF` (inicio) | Muestra los 2 jugadores, pregunta, botón BUZZ IN |
| `FACE_OFF` (uno pulsó) | Resalta quién pulsó primero, espera respuesta |
| `FACE_OFF` (respondió) | Muestra resultado, transición a TURN_PLAYERx |

- [ ] HTML: Sección Face-Off con layout VS
- [ ] CSS: Estilos para pantalla de careo
- [ ] JavaScript: Lógica de buzz-in con timer
- [ ] Audio: Sonido buzzer al pulsar
- [ ] Host puede seleccionar ganador si ambos pulsaron

---

### 7.4 — Indicador de Turno por Jugador

**Objetivo:** Mostrar qué jugador específico está respondiendo dentro del equipo.

| Archivo | Cambios |
|---|---|
| `play.html` | Badge/nombre del jugador activo en el scoreboard |
| `style.css` | Estilo `.active-player-badge` |

**Layout:**
```
┌─────────────────┐
│   EQUIPO 1      │
│   [SCORE]       │
│   ✗ ✗ ✗        │
│   ★ Juan (cap)  │  ← jugador activo con badge
│   [CONTROL]     │
└─────────────────┘
```

- [ ] HTML: Badge del jugador activo en scoreboard
- [ ] CSS: Estilo con resaltado y animación sutil
- [ ] Se actualiza automáticamente al avanzar turno

---

### 7.5 — Pantalla de Muerte Súbita

**Objetivo:** Pantalla especial para el tiebreaker.

| Archivo | Cambios |
|---|---|
| `play.html` | Nueva sección `x-show="status === 'SUDDEN_DEATH'"` |
| `audio.js` | Sonido: `playSuddenDeath()` |
| `style.css` | Estilos: `.sudden-death-screen`, `.sudden-death-title`, `.sudden-death-pulse` |

**Layout:**
```
┌──────────────────────────────────────────────────┐
│                                                  │
│       ⚡  MUERTE SÚBITA  ⚡                      │
│                                                  │
│  ┌──────────────────────────────────────────┐   │
│  │  Pregunta: "¿Cuál es...?"                │   │
│  └──────────────────────────────────────────┘   │
│                                                  │
│  ┌─────────────┐    VS    ┌─────────────┐       │
│  │  CAPITÁN 1  │         │  CAPITÁN 2  │       │
│  └─────────────┘         └─────────────┘       │
│                                                  │
│  Regla: 1 SOLO STRIKE                           │
│  [  BUZZ IN  ]                                  │
│                                                  │
│  Si falla → robo automático del otro equipo     │
└──────────────────────────────────────────────────┘
```

- [ ] HTML: Sección Muerte Súbita con layout VS capitanes
- [ ] CSS: Estilo especial dramático (colores, animaciones)
- [ ] Indicador visual de "1 SOLO STRIKE"
- [ ] Audio: Sonido de tensión dramática al iniciar

---

### 7.6 — Pantalla de Dinero Rápido

**Objetivo:** Ronda de bonificación con 2 jugadores y timer.

| Archivo | Cambios |
|---|---|
| `play.html` | Nueva sección completa `x-show="status === 'FAST_MONEY'"` |
| `audio.js` | Sonidos: `playFastMoneyStart()`, `playFastMoneyDing()`, `playFastMoneyEnd()` |
| `style.css` | Estilos: `.fast-money-screen`, `.fast-money-card`, `.fast-money-timer`, `.fast-money-score` |

**7.6.1 — Selección de jugadores:**
```
┌──────────────────────────────────────────────────┐
│          💰 DINERO RÁPIDO 💰                     │
│                                                  │
│  Equipo Ganador selecciona 2 jugadores:          │
│                                                  │
│  Jugador 1: [Dropdown de miembros del equipo]    │
│  Jugador 2: [Dropdown de miembros del equipo]    │
│                                                  │
│  [  INICIAR DINERO RÁPIDO  ]                     │
└──────────────────────────────────────────────────┘
```

**7.6.2 — Jugador 1 respondiendo (15 segundos):**
```
┌──────────────────────────────────────────────────┐
│  DINERO RÁPIDO — Jugador 1 de 2                 │
│                                                  │
│  ⏱️ 12:34  (timer grande, countdown)             │
│                                                  │
│  Pregunta 1: "¿Qué harías si...?"              │
│  [Respuesta 1..............]                     │
│                                                  │
│  Pregunta 2: "¿Cuál es...?"                    │
│  [Respuesta 2..............]                     │
│                                                  │
│  ... (5 preguntas apiladas)                      │
│                                                  │
│  [  ENVIAR RESPUESTAS  ]                         │
│                                                  │
│  ⏱️ Tiempo límite: 15 segundos                  │
└──────────────────────────────────────────────────┘
```

**7.6.3 — Jugador 2 respondiendo (20 segundos):**
```
┌──────────────────────────────────────────────────┐
│  DINERO RÁPIDO — Jugador 2 de 2                 │
│                                                  │
│  ⏱️ 18:22  (timer countdown)                     │
│                                                  │
│  Pregunta 1: "¿Qué harías si...?"              │
│  [Respuesta 1..............]                     │
│                                                  │
│  ... (las mismas 5 preguntas)                    │
│                                                  │
│  [  ENVIAR RESPUESTAS  ]                         │
│                                                  │
│  ⚠️ Si repite respuesta del Jugador 1, no cuenta │
│  ⏱️ Tiempo límite: 20 segundos                  │
└──────────────────────────────────────────────────┘
```

**7.6.4 — Revelación de resultados:**
```
┌──────────────────────────────────────────────────┐
│          💰 RESULTADOS DINERO RÁPIDO 💰         │
│                                                  │
│  Jugador 1:                                      │
│  ┌──────────────────────────────────────────┐   │
│  │ P1: "Comprar casa"      → 35 pts ✅      │   │
│  │ P2: "Viajar"            → 25 pts ✅      │   │
│  │ P3: "Ayudar familia"    →  0 pts ❌      │   │
│  │ P4: "Invertir"          → 10 pts ✅      │   │
│  │ P5: "Comprar coche"     →  5 pts ✅      │   │
│  │ TOTAL: 75 puntos                       │   │
│  └──────────────────────────────────────────┘   │
│                                                  │
│  Jugador 2:                                      │
│  ┌──────────────────────────────────────────┐   │
│  │ (mismo formato)                          │   │
│  │ TOTAL: 80 puntos                       │   │
│  └──────────────────────────────────────────┘   │
│                                                  │
│  ┌──────────────────────────────────────────┐   │
│  │  TOTAL COMBINADO: 155 / 200             │   │
│  │  [████████████░░░░░░] 77.5%             │   │
│  │                                          │   │
│  │  ❌ No alcanzaron el bonus               │   │
│  │  (o ✅ ¡BONIFICACIÓN GANADA!)            │   │
│  └──────────────────────────────────────────┘   │
│                                                  │
│  [  VOLVER AL DASHBOARD  ]                       │
└──────────────────────────────────────────────────┘
```

- [ ] HTML: Sección selección de jugadores
- [ ] HTML: Formulario jugador 1 (5 inputs + timer 15s)
- [ ] HTML: Formulario jugador 2 (5 inputs + timer 20s)
- [ ] HTML: Pantalla de resultados con barra de progreso
- [ ] CSS: Estilos de cada sub-sección
- [ ] JavaScript: Timer por jugador, validación de respuestas repetidas
- [ ] Audio: 3 sonidos nuevos (start, ding, end)
- [ ] Botón "Volver al Dashboard" al final

---

### 7.7 — Sonidos nuevos (audio.js)

| Método | Sonido | Uso |
|---|---|---|
| `playBuzzer()` | Zumbido electrónico | Cara a cara, cuando alguien pulsa |
| `playTick()` | Tic-tac | Cada segundo del timer |
| `playTimerWarning()` | Triple tic urgente | Timer < 3 segundos |
| `playTimerEnd()` | Buzzer de tiempo agotado | Timer llega a 0 |
| `playFastMoneyStart()` | Fanfarria especial | Inicio de Dinero Rápido |
| `playFastMoneyDing()` | Ding de respuesta | Cada respuesta de Dinero Rápido |
| `playFastMoneyEnd()` | Fanfarria de resultados | Fin de Dinero Rápido |
| `playSuddenDeath()` | Tensión dramática | Inicio de Muerte Súbita |

- [ ] Implementar los 8 sonidos con Web Audio API

---

### 7.8 — Eventos WebSocket nuevos

| Evento | Descripción |
|---|---|
| `FACE_OFF_STARTED` | Inicia cara a cara |
| `FACE_OFF_ANSWERED` | Alguien pulsó/buzzó |
| `SUDDEN_DEATH_STARTED` | Se activa muerte súbita |
| `SUDDEN_DEATH_FACE_OFF` | Careo de muerte súbita |
| `SUDDEN_DEATH_WON` | Alguien ganó la muerte súbita |
| `FAST_MONEY_STARTED` | Inicia dinero rápido |
| `FAST_MONEY_PLAYER1_DONE` | Jugador 1 terminó |
| `FAST_MONEY_WON` / `FAST_MONEY_LOST` | Resultado del bonus |

- [ ] Agregar eventos en `GameWebSocketController.java`
- [ ] Frontend suscrito a todos los eventos nuevos

---

### 7.9 — Dashboard: Modal de configuración mejorado

| Archivo | Cambios |
|---|---|
| `dashboard.html` | Agregar campos: `targetScore`, `timerEnabled`, `turnTimeLimit` en modal |

**Campos nuevos en el modal:**
```
- Puntos para ganar: [300] (default)
- Timer activado: [✓] (checkbox)
- Segundos por turno: [10] (solo visible si timer activado)
```

- [ ] HTML: Campos nuevos en modal de crear partida
- [ ] Condicional: campo de segundos solo visible si timer activado

---

### 7.10 — Mejoras CSS generales

| Estilo | Descripción |
|---|---|
| `.screen-transition` | Transición suave entre pantallas (fade + slide) |
| `.glitch-text` | Efecto glitch para títulos de Muerte Súbita |
| `.money-rain` | Animación de billetes cayendo para Dinero Rápido |
| `.pulse-danger` | Pulse rojo para timer crítico |
| `.player-card` | Tarjeta de jugador con avatar/badge |
| `.captain-badge` | Estrella/escudo para capitán |
| `.vs-divider` | Divisor VS animado para cara a cara |

- [ ] Implementar todas las clases CSS
- [ ] Verificar transiciones entre pantallas
- [ ] Diseño responsivo (funciona en móvil)

---

### Criterios de verificación globales Fase 7

- [ ] Selector de capitán funciona correctamente
- [ ] Timer visual se muestra durante turnos
- [ ] Pantalla de Careo funciona (buzz-in, timer, selección ganador)
- [ ] Pantalla de Muerte Súbita funciona (1 strike, robo automático)
- [ ] Pantalla de Dinero Rápido funciona (2 jugadores, timer, resultados)
- [ ] Todos los sonidos funcionan
- [ ] Diseño responsivo (funciona en móvil)
- [ ] `./mvnw compile` pasa sin errores

---

## Resumen de archivos

| Archivo | Fases | Acción |
|---|---|---|
| `Game.java` | 1,2,3,4,6 | Modificar |
| `GameStatus.java` | 1,4,5 | Modificar |
| `GameRoundStatus.java` | 2,4 | Modificar |
| `Participant.java` | 3 | Modificar |
| `GameService.java` | 1-6,7.1 | Modificar |
| `GameController.java` | 2,4,5,7.1 | Modificar |
| `GameDTO.java` | 1,2,3 | Modificar |
| `CreateGameDTO.java` | 6 | Modificar |
| `FastMoneyRound.java` | 5 | **Crear** |
| `FastMoneyRoundRepository.java` | 5 | **Crear** |
| `FastMoneyService.java` | 5 | **Crear** |
| `FastMoneyController.java` | 5 | **Crear** |
| `GameWebSocketController.java` | 7.8 | Modificar |
| `play.html` | 7 | Modificar extensivamente → ✅ |
| `dashboard.html` | 7.9 | Modificar → ✅ |
| `style.css` | 7 | Modificar extensivamente → ✅ |
| `audio.js` | 7.7 | Modificar → ✅ |

---

## Orden de implementación

### Backend (✅ completado)
1. **Fase 1** (Core) → ✅ Completado
2. **Fase 3** (Turnos individuales) → ✅ Completado
3. **Fase 2** (Cara a Cara) → ✅ Completado
4. **Fase 4** (Muerte Súbita) → ✅ Completado
5. **Fase 6** (Timer) → ✅ Completado
6. **Fase 5** (Dinero Rápido) → ✅ Completado

### Frontend (✅ completado)
7. **Fase 7.10** — CSS base y transiciones → ✅
8. **Fase 7.7** — Sonidos nuevos (8 sonidos) → ✅
9. **Fase 7.2** — Componente Timer reutilizable → ✅
10. **Fase 7.4** — Indicador de turno por jugador → ✅
11. **Fase 7.1** — Selector de capitán (requiere backend endpoint) → ✅
12. **Fase 7.3** — Pantalla Cara a Cara → ✅
13. **Fase 7.5** — Pantalla Muerte Súbita → ✅
14. **Fase 7.6** — Pantalla Dinero Rápido → ✅
15. **Fase 7.8** — Eventos WebSocket nuevos → ✅
16. **Fase 7.9** — Dashboard modal mejorado
17. **Verificación** — Compilar, probar en navegador, responsividad

---

## Reglas oficiales (referencia)

### Flujo del juego
1. Host lee la pregunta → Cara a Cara entre un jugador de cada equipo.
2. El que acierta la respuesta de mayor rango gana el control.
3. El equipo en control responde en cadena hasta acertar todo o llegar a 3 strikes.
4. Si hay 3 strikes, el equipo contrario intenta el robo (solo responde el capitán).
5. Se suman los puntos de la ronda al equipo ganador.
6. Se repite por 5 rondas (valores x1, x1, x2, x2, x3).
7. Si nadie llega a 300 puntos, se juega Muerte Súbita.
8. El equipo ganador juega Dinero Rápido para el premio mayor.

### Multiplicadores por ronda
| Ronda | Valor |
|---|---|
| 1 | x1 |
| 2 | x1 |
| 3 | x2 |
| 4 | x2 |
| 5 | x3 |

### Victoria
- Primer equipo en alcanzar **300 puntos** gana
- Si después de 5 rondas nadie llega a 300 → Muerte Súbita

### Muerte Súbita
- Careo entre capitanes
- 1 solo strike
- Si el equipo en control falla → el otro roba automáticamente

### Dinero Rápido
- 2 jugadores del equipo ganador
- Jugador 1: 5 preguntas, 15 segundos
- Jugador 2: mismas 5 preguntas, 20 segundos
- Si repite respuesta → invalida
- Si total ≥ 200 puntos → gana bonificación

---

## FASE 7b: Corrección de bugs — Auditoría post-implementación

**Fecha:** Julio 2026
**Commits:** 20 fixes aplicados

### Bugs corregidos

| # | Severidad | Descripción | Archivos |
|---|---|---|---|
| 1 | Critical | `saveParticipants()` llamado 2 veces (step 1 + step 2) | play.html |
| 2 | Critical | `startRound()` se ejecuta antes de captain selection | play.html |
| 3 | Critical | `suddenDeathAnswer()` siempre incrementa `team1Errors` | GameService.java |
| 4 | Critical | `buzzIn()` usa `indexOf()` con referencia en vez de ID | GameService.java |
| 5 | High | `Long` comparado con `!=` en vez de `.equals()` (3 lugares) | GameService.java |
| 6 | High | `ANSWER_WRONG` handler ejecuta `loadCurrentQuestion()` 2 veces | play.html |
| 7 | High | Timer nunca inicia en flujo normal (solo en refresh) | play.html |
| 8 | High | Timer no se detiene durante STEAL_ATTEMPT / ROUND_ENDED | play.html |
| 9 | High | `ParticipantDTO` sin campo `captain` (se pierde en refresh) | ParticipantDTO.java, GameService.java |
| 10 | High | Fast Money timers nunca decrementan | play.html |
| 11 | High | Fast Money 100% mock — nunca llama al backend | play.html |
| 12 | Medium | Multiplicadores custom siempre hardcodeados | Game.java, GameService.java, V12 migration |
| 13 | Medium | BUZZ IN activo cuando no hay jugador seleccionado | play.html |
| 14 | Medium | Fast Money permite mismo jugador 2 veces | play.html |
| 15 | Medium | Fast Money muestra resultados random | play.html |
| 16 | Medium | `x-cloak` falta en sección de setup | play.html |
| 17 | Medium | `handleCorrectAnswer()` steal solo funciona para team 2 | GameService.java |
| 18 | Medium | `mapToGameDTO()` omite `faceOffPlayer1/2` | GameDTO.java, GameService.java |
| 19 | Medium | `.fast-money-screen` sin `position: relative` | style.css |
| 20 | Medium | 5 eventos WebSocket sin handler | play.html |

### Archivos modificados
- `src/main/java/.../Service/GameService.java` — Fixes 3,4,5,12,17,18
- `src/main/java/.../DTO/ParticipantDTO.java` — Fix 9 (campo `captain`)
- `src/main/java/.../DTO/GameDTO.java` — Fix 18 (campos `faceOffPlayer1/2`)
- `src/main/java/.../Entity/Game.java` — Fix 12 (`roundMultipliers`, `getMultipliersArray()`)
- `src/main/resources/templates/play.html` — Fixes 1,2,6,7,8,10,11,13,14,15,16,20
- `src/main/resources/static/css/style.css` — Fix 19
- `src/main/resources/db/migration/V12__round_multipliers.sql` — Fix 12
