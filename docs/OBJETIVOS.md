# Objetivos del Proyecto — 100MD-app

## Visión General

**100MD-app** es la API backend del juego *"100 Mexicanos Dijeron"*, la versión mexicana del formato televisivo internacionalmente conocido como *"Family Feud"* (Estados Unidos), *"100 to 1"* (internacional) o *"100 argentinos dicen"* (Argentina).

Dos equipos compiten adivinando las respuestas más populares a preguntas de encuesta. Gana el equipo que acumule más puntos tras una serie de rondas.

---

## Objetivo General

Construir una API REST robusta y escalable que gestione el ciclo de vida completo de las partidas del juego "100 Mexicanos Dijeron", incluyendo la administración del banco de preguntas, la lógica de rondas y puntuación, y la comunicación en tiempo real con los clientes.

---

## Objetivos Específicos

### 1. Gestión del Banco de Preguntas
- Proveer endpoints CRUD para administrar preguntas y sus respuestas asociadas.
- Cada respuesta debe tener un puntaje numérico que represente el porcentaje de personas que dieron esa respuesta en una encuesta real.
- Soportar búsqueda y filtrado de preguntas.

### 2. Administración de Partidas (Games)
- Permitir la creación de partidas asociando preguntas seleccionadas aleatoriamente del banco.
- Mantener el estado de cada partida: `NOT_STARTED`, `IN_PROGRESS`, `SUDDEN_DEATH`, `FAST_MONEY`, `FINISHED`.
- Gestionar la participación de jugadores dentro de cada partida.
- Configuración flexible: rondas (2-10), multiplicadores por ronda, miembros por equipo (2-5), targetScore, timer.

### 3. Sistema de Rondas y Puntuación
- Implementar el flujo completo de rondas fiel al formato televisivo:
  - **Cara a Cara**: careo inicial 1v1 entre jugadores para ganar control
  - **Turnos individuales**: miembros del equipo responden en orden secuencial
  - **Strikes**: 3 errores → robo del equipo contrario (solo el capitán)
  - **Muerte Súbita**: tiebreaker después de 5 rondas, 1 solo strike
  - **Dinero Rápido**: ronda de bonificación para el equipo ganador
- 5 rondas con multiplicadores x1, x1, x2, x2, x3
- Victoria a 300 puntos (detecta victoria mid-game)

### 4. Comunicación en Tiempo Real
- WebSocket con protocolo STOMP para transmitir actualizaciones de la partida a todos los clientes conectados.
- 8 eventos WebSocket para todas las fases del juego.
- SockJS como fallback para clientes que no soporten WebSocket nativo.
- Modelo host-control: un host administra el juego, los espectadores ven en tiempo real.

### 5. API Documentada
- Documentación interactiva mediante Swagger UI (OpenAPI) en `/swagger-ui/index.html`.

### 6. Persistencia de Datos
- MySQL con Spring Data JPA (Hibernate).
- Flyway con 13 migraciones versionadas (V1-V13).
- Spring Data JPA como capa de abstracción (cambio de BD sin modificar código Java).

### 7. Calidad y Mantenibilidad del Código
- Lombok para reducir boilerplate.
- MapStruct para mapeo automático DTO↔Entity.
- AttributeConverter para mapeo de tipos personalizados (int[] ↔ String).
- Estructura de paquetes clara por capas.
- Pruebas unitarias y de integración (5/5 pasan).
- GitHub Actions CI.

---

## Funcionalidades Implementadas (v0.3.0)

### Core del Juego
- [x] Creación de partidas con selección aleatoria de preguntas
- [x] Configuración flexible (rondas, multiplicadores, miembros, targetScore, timer)
- [x] Lógica Family Feud: turnos, rondas, strikes, steal
- [x] Cara a Cara (face-off) con buzzer y selección ganador
- [x] Turnos individuales con avance secuencial
- [x] Capitán designado por equipo
- [x] Muerte Súbita (tiebreaker con 1 solo strike)
- [x] Dinero Rápido (2 jugadores, timer, bonus 200 pts)
- [x] Timer por turno configurable
- [x] Host-control: host revela respuestas haciendo clic

### Backend
- [x] CRUD de preguntas y respuestas (AdminController)
- [x] Registro de participantes por partida
- [x] Cálculo de puntaje con multiplicadores
- [x] Control de errores por equipo (3 strikes → steal)
- [x] Auto-endRound cuando todas las respuestas se revelan
- [x] WebSocket STOMP + SockJS (8 eventos, broadcast en tiempo real)
- [x] Login y autenticación (Spring Security, BCrypt)
- [x] Registro de usuarios
- [x] Game history y player statistics
- [x] Documentación Swagger/OpenAPI
- [x] Logging completo (SLF4J backend + console.log frontend)

### Frontend
- [x] UI premium estilo TV show (Poppins + Orbitron)
- [x] Colores mexicanos: verde `#006847`, rojo `#CE1126`, dorado `#FFD700`
- [x] Dashboard rediseñado con cards y modal de configuración
- [x] Pantalla de juego host-controlled (~1297 líneas)
- [x] Pantalla de resultados dedicada con confetti
- [x] 8 sonidos con Web Audio API
- [x] Navegación consistente con navbar oscura
- [x] Diseño responsivo

### Infraestructura
- [x] MySQL con Flyway (13 migraciones V1-V13)
- [x] Spring Data JPA (cambio de BD sin modificar código)
- [x] Docker deployment
- [x] Tests con H2 (5/5 pasan)
- [x] GitHub Actions CI
- [x] MapStruct (6 mappers, compile-time DTO mapping)
- [x] AttributeConverter (IntArrayConverter)
- [x] canvas-confetti@1.9.3 (CDN)
- [x] Alpine.js componentes refactorizados (game-timer.js, fast-money.js)

---

## Funcionalidades Planeadas

- [ ] Testcontainers (MySQL real en tests) — pendiente por falta de Docker
- [ ] Robo solo del capitán (validación frontend)
- [ ] Soporte para preguntas multimedia (imágenes, audio)
- [ ] Internacionalización (i18n) para otros países hispanohablantes
