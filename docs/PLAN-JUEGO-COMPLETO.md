# Plan: Juego 100 Mexicanos Dijieron — Family Feud Completo

## Objetivo
Terminar de codificar el juego completo con lógica fiel al formato Family Feud, UI mejorada y configuración flexible.

---

## FASE 1: Lógica del Juego — Corregir a modelo Family Feud

### GameService.java
- `startNextRound()`: Solo un equipo tiene control por ronda. Alterna qué equipo empieza entre rondas (par/impar)
- `submitAnswer()`: Validar que solo el equipo con control pueda responder durante `TURN_PLAYER1`/`TURN_PLAYER2`
- `handleIncorrectAnswer()`: Acumula errores solo al equipo con control
- `handleCorrectAnswer()`: Suma puntos a `currentRoundPoints` para el equipo con control
- `passTurn()`: Solo el equipo con control puede pasar, activando `STEAL_ATTEMPT`
- `endRound()`: Asigna `currentRoundPoints` al equipo que tenía control

### Game.java — Nuevos campos
- `totalRounds` (int) — rondas configuradas
- `teamSize` (int) — miembros por equipo (2-5)
- `currentMultiplier` (int) — multiplicador de la ronda actual
- `controllingTeam` (int) — equipo con control (1 o 2)

### GameDTO.java — Campos nuevos
- `totalRounds`, `teamSize`, `currentMultiplier`, `controllingTeam`

---

## FASE 2: Configuración del Juego

### Nuevo DTO: CreateGameDTO.java
```java
int totalRounds;        // 2-10
int[] multipliers;      // ej: [1,1,2,3]
int teamSize;           // 2-5
```

### createNewGame(CreateGameDTO)
- Valida teamSize (2-5), totalRounds (2-10)
- Selecciona `totalRounds` preguntas al azar
- Guarda configuración en el game

---

## FASE 3: Participants — Límites y orden

### Participant.java — Nuevo campo
- `memberOrder` (int) — posición en la secuencia del equipo

### addParticipant() — Validaciones
- No más de `teamSize` miembros por equipo
- Asigna `memberOrder` automáticamente

---

## FASE 4: Migración SQL

### V5__game_config.sql
```sql
ALTER TABLE games ADD COLUMN total_rounds INT NOT NULL DEFAULT 3;
ALTER TABLE games ADD COLUMN team_size INT NOT NULL DEFAULT 5;
ALTER TABLE games ADD COLUMN current_multiplier INT NOT NULL DEFAULT 1;
ALTER TABLE games ADD COLUMN controlling_team INT DEFAULT NULL;

ALTER TABLE participants ADD COLUMN member_order INT NOT NULL DEFAULT 0;
```

---

## FASE 5: Frontend — Configuración de Partida

### dashboard.html
- Modal/formulario "Nueva Partida" con:
  - Cantidad de rondas (2-10)
  - Multiplicador por ronda (inputs dinámicos)
  - Miembros por equipo (2-5)
- POST a `/api/games` con `CreateGameDTO`

---

## FASE 6: Frontend — Setup de Participantes

### play.html — Antes de iniciar
- Pantalla para agregar nombres de ambos equipos
- Límite visual de `teamSize` por equipo
- Botón "Iniciar Juego" solo después de tener al menos 1 miembro por equipo

---

## FASE 7: Frontend — Play UI Rediseñada

### play.html — Mejoras
- `getCurrentParticipantId()`: Seleccionar miembro activo según el turno
- Indicador de quién tiene control (badge en el equipo activo)
- Mostrar multiplicador de ronda actual (ej: "x2")
- Strikes claros con animación
- Puntos acumulados de la ronda visibles
- Botón "Pasar Turno" visible solo para el equipo con control

---

## FASE 8: Fix WebSocket duplicado

### GameWebSocketController.java
- Quitar los `messagingTemplate.convertAndSend()` directos
- Dejar solo el `eventPublisher` que ya hace el broadcast via `WebSocketEventListener`

---

## Archivos a modificar/crear

| Archivo | Acción |
|---|---|
| `Entity/Game.java` | Modificar — nuevos campos |
| `Entity/Participant.java` | Modificar — memberOrder |
| `Service/GameService.java` | Modificar — lógica Family Feud |
| `DTO/GameDTO.java` | Modificar — nuevos campos |
| `DTO/CreateGameDTO.java` | **Crear** |
| `Controller/GameWebSocketController.java` | Modificar — quitar duplicados |
| `Controller/GameController.java` | Modificar — recibe CreateGameDTO |
| `Controller/AdminController.java` | Modificar — endpoint config |
| `resources/db/migration/V5__game_config.sql` | **Crear** |
| `templates/dashboard.html` | Modificar — config de partida |
| `templates/play.html` | Modificar — UI rediseñada |
| `static/css/style.css` | Modificar — nuevos estilos |

---

## Orden de implementación

1. FASE 1 + 2 + 3 + 4 (Backend core) → Compilar y verificar
2. FASE 8 (Fix WebSocket) → Compilar y verificar
3. FASE 5 + 6 + 7 (Frontend) → Probar en navegador
