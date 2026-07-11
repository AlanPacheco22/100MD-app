# Plan: Adaptación Reglas Oficiales — 100 Mexicanos Dijeron

> **Fecha:** Julio 2026
> **Estado:** Backend completado ✅
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

**Objetivo:** Actualizar frontend para soportar todas las fases.

### Archivos a modificar

| Archivo | Cambios |
|---|---|
| `play.html` | Agregar pantallas de Careo, Muerte Súbita, Dinero Rápido, Timer |
| `dashboard.html` | Botón "Nueva Partida" con opciones de 5 rondas, target 300 |
| `style.css` | Estilos para careo, timer, muerte súbita |

### Criterios de verificación
- [ ] Pantalla de careo funciona correctamente
- [ ] Timer visual se muestra durante turnos
- [ ] Pantalla de Muerte Súbita funciona
- [ ] Pantalla de Dinero Rápido funciona
- [ ] Diseño responsivo (funciona en móvil)

---

## Resumen de archivos

| Archivo | Fases | Acción |
|---|---|---|
| `Game.java` | 1,2,3,4,6 | Modificar |
| `GameStatus.java` | 1,4,5 | Modificar |
| `GameRoundStatus.java` | 2,4 | Modificar |
| `Participant.java` | 3 | Modificar |
| `GameService.java` | 1-6 | Modificar |
| `GameController.java` | 2,4,5 | Modificar |
| `GameDTO.java` | 1,2,3 | Modificar |
| `FastMoneyState.java` | 5 | **Crear** |
| `FastMoneyService.java` | 5 | **Crear** |
| `FastMoneyController.java` | 5 | **Crear** |
| `FastMoneyRepository.java` | 5 | **Crear** |
| `play.html` | 7 | Modificar |
| `dashboard.html` | 7 | Modificar |
| `style.css` | 7 | Modificar |

---

## Orden de implementación

1. **Fase 1** (Core) → ✅ Completado
2. **Fase 3** (Turnos individuales) → ✅ Completado
3. **Fase 2** (Cara a Cara) → ✅ Completado
4. **Fase 4** (Muerte Súbita) → ✅ Completado
5. **Fase 6** (Timer) → ✅ Completado
6. **Fase 5** (Dinero Rápido) → ✅ Completado
7. **Fase 7** (UI) → Pendiente (frontend)

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
