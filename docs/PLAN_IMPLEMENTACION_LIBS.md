# Plan de Implementación — Librerías y Plugins

> Fecha: 2026-07-10  
> Basado en: `docs/INVESTIGACION_BIBLIOTECAS.md`

---

## Fase 1: Backend — MapStruct (compile-time DTO mapping)

**Objetivo:** Eliminar mapeo manual de DTOs en Services.

### Pasos:
1. Agregar dependencias en `pom.xml` (mapstruct + mapstruct-processor)
2. Configurar annotation processor en maven-compiler-plugin
3. Crear interfaz `GameMapper` en `com.AlanPacheco.CienMD_app.Mapper`
4. Crear mappers para cada DTO principal:
   - `GameMapper` → Game ↔ GameDTO
   - `QuestionMapper` → Question ↔ QuestionDTO
   - `AnswerMapper` → Answer ↔ AnswerDTO
   - `ParticipantMapper` → Participant ↔ ParticipantDTO
   - `GameQuestionMapper` → GameQuestion ↔ GameQuestionDTO
   - `FastMoneyMapper` → FastMoneyRound ↔ FastMoneyDTO
5. Migrar `GameService.mapToGameDTO()` y `mapToGameQuestionDTO()` a usar mapper
6. Migrar otros services que mapean DTOs
7. Eliminar métodos manuales de mapeo
8. Compilar y ejecutar tests

### Archivos a crear/modificar:
- `pom.xml` — dependencias
- `src/main/java/.../Mapper/GameMapper.java` (nuevo)
- `src/main/java/.../Mapper/QuestionMapper.java` (nuevo)
- `src/main/java/.../Mapper/AnswerMapper.java` (nuevo)
- `src/main/java/.../Mapper/ParticipantMapper.java` (nuevo)
- `src/main/java/.../Mapper/GameQuestionMapper.java` (nuevo)
- `src/main/java/.../Mapper/FastMoneyMapper.java` (nuevo)
- `src/main/java/.../Service/GameService.java` — reemplazar mapToGameDTO/mapToGameQuestionDTO
- `src/main/java/.../Service/StatsService.java` — reemplazar mapeo manual
- `src/main/java/.../Service/FastMoneyService.java` — reemplazar mapeo manual

---

## Fase 2: Backend — AttributeConverter (limpia int[] ↔ String)

**Objetivo:** Eliminar métodos manuales `getMultipliersArray()`/`setMultipliersArray()`.

### Pasos:
1. Crear `IntArrayConverter` implements `AttributeConverter<int[], String>`
2. Agregar `@Convert(converter = IntArrayConverter.class)` en `Game.roundMultipliers`
3. Eliminar `getMultipliersArray()` y `setMultipliersArray()` de Game.java
4. Actualizar GameMapper para que MapStruct maneje el `int[]` directamente
5. Compilar y testear

### Archivos a crear/modificar:
- `src/main/java/.../Converter/IntArrayConverter.java` (nuevo)
- `src/main/java/.../Entity/Game.java` — agregar @Convert, eliminar métodos
- `src/main/java/.../Mapper/GameMapper.java` — ajustar si necesario

---

## Fase 3: Backend — Testcontainers (MySQL real en tests)

**Objetivo:** Reemplazar H2 con MySQL real para tests de integración.

### Pasos:
1. Agregar dependencias en `pom.xml` (spring-boot-testcontainers, testcontainers-mysql)
2. Crear clase base `AbstractIntegrationTest` con `@ServiceConnection`
3. Crear test de integración para `GameRepository` con MySQL real
4. Crear test de integración para `GameService` con MySQL real
5. Verificar que los 13 migrations (V1-V13) aplican correctamente en MySQL real
6. Configurar `application-test.properties` para Testcontainers
7. Ejecutar suite completa

### Archivos a crear/modificar:
- `pom.xml` — dependencias
- `src/test/java/.../config/AbstractIntegrationTest.java` (nuevo)
- `src/test/java/.../repository/GameRepositoryIntegrationTest.java` (nuevo)
- `src/test/java/.../service/GameServiceIntegrationTest.java` (nuevo)
- `src/test/resources/application-test.properties` (nuevo)

---

## Fase 4: Frontend — canvas-confetti (reemplaza createConfetti)

**Objetivo:** Reemplazar canvas manual con librería CDN.

### Pasos:
1. Agregar CDN script en play.html `<head>`:
   ```html
   <script src="https://cdn.jsdelivr.net/npm/canvas-confetti@1.9.3/dist/confetti.browser.min.js"></script>
   ```
2. Reemplazar `createConfetti()` con llamada a `confetti()`
3. Agregar `confetti()` al evento GAME_FINISHED
4. Agregar `confetti()` al evento de victoria en sudden death
5. Eliminar función `createConfetti()` y su canvas HTML
6. Agregar `disableForReducedMotion: true` para accesibilidad

### Archivos a modificar:
- `src/main/resources/templates/play.html` — CDN + reemplazar función

---

## Fase 5: Frontend — Alpine Fetch (simplifica HTTP)

**Objetivo:** Reducir verbosidad de fetch() en play.html.

### Pasos:
1. Agregar CDN en play.html `<head>`:
   ```html
   <script defer src="https://cdn.jsdelivr.net/gh/hankhank10/alpine-fetch@main/alpine-fetch.js"></script>
   ```
2. Simplificar `loadParticipants()` usando `$fetch`
3. Simplificar `getResults()` usando `$fetchjson`
4. Mantener `submitAnswer()`, `incrementError()` etc. con fetch manual (requieren lógica post-response)
5. Medir líneas ahorradas

### Archivos a modificar:
- `src/main/resources/templates/play.html` — CDN + refactors puntuales

---

## Fase 6: Frontend — Alpine.data() refactor (divide monolito)

**Objetivo:** Dividir `gameBoard()` de 883 líneas en componentes reutilizables.

### Pasos:
1. Extraer componente `gameTimer()` con start/stop/remaining
2. Extraer componente `faceOff()` con submit/buzz/state
3. Extraer componente `suddenDeath()` con submit/buzz/state
4. Extraer componente `fastMoney()` con phases/timers/submit
5. Extraer componente `teamSetup()` con members/captains
6. Mantener `gameBoard()` como orquestador que usa los sub-componentes
7. Actualizar HTML para usar `x-data="gameBoard()"` con sub-componentes

### Archivos a crear/modificar:
- `src/main/resources/static/js/game-timer.js` (nuevo)
- `src/main/resources/static/js/face-off.js` (nuevo)
- `src/main/resources/static/js/sudden-death.js` (nuevo)
- `src/main/resources/static/js/fast-money.js` (nuevo)
- `src/main/resources/static/js/team-setup.js` (nuevo)
- `src/main/resources/templates/play.html` — reorganizar

---

## Fase 7: Frontend — Howler.js (audio mejorado)

**Objetivo:** Reemplazar `Audio()` vanilla con pooling y control fino.

### Pasos:
1. Agregar CDN en play.html:
   ```html
   <script src="https://cdn.jsdelivr.net/npm/howler@2.2.4/dist/howler.min.js"></script>
   ```
2. Reemplazar AudioManager con Howler instances
3. Agregar fade in/out a sonidos de timer
4. Agregar volumen diferenciado por tipo de sonido
5. Eliminar objetos `new Audio()` manuales

### Archivos a modificar:
- `src/main/resources/templates/play.html` — AudioManager refactor

---

## Orden de Ejecución Recomendado

```
Fase 1 (MapStruct) ─────┐
Fase 2 (Converter) ─────┼──→ Fase 4 (Confetti) ──→ Fase 5 (Fetch) ──→ Fase 6 (Refactor)
Fase 3 (Testcontainers) ┘                                    └──→ Fase 7 (Howler)
```

- Fases 1-3 son backend, independientes entre sí, se pueden hacer en paralelo
- Fases 4-7 son frontend, Fase 6 es la más laboriosa
- Fase 7 es opcional/nice-to-have

---

## Criterios de Aceptación

- [ ] `mvn compile` pasa sin errores
- [ ] `mvn test` pasa todos los tests (unit + integration)
- [ ] Juego completo funciona: setup → face-off → rondas → steal → sudden death → fast money → resultados
- [ ] WebSocket events siguen funcionando correctamente
- [ ] Confetti aparece al ganar
- [ ] play.html tiene menos de 1200 líneas (de 1483 actuales)
- [ ] GameService tiene menos de 800 líneas (de 1136 actuales)
