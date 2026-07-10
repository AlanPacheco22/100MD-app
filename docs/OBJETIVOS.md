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
- Mantener el estado de cada partida: `NOT_STARTED`, `IN_PROGRESS`, `FINISHED`.
- Gestionar la participación de jugadores dentro de cada partida.

### 3. Sistema de Rondas y Puntuación
- Implementar el flujo de rondas dentro de una partida.
- **Regla de errores:** Cada equipo puede fallar hasta 3 respuestas por ronda. Al llegar a 3 errores, el equipo contrario tiene UNA oportunidad de robar todos los puntos acumulados revelando una respuesta oculta. Si aciertan, roban los puntos; si fallan o el host selecciona "Terminar Ronda", los puntos se quedan con el equipo controlador.
- **Puntuación:** Cada acierto suma el puntaje de la respuesta (porcentaje de encuesta) multiplicado por un factor (`multiplier`).
- Controlar la transición de turnos entre equipos y el fin de ronda.

### 4. Comunicación en Tiempo Real
- Implementar WebSocket con protocolo STOMP para transmitir actualizaciones de la partida a todos los clientes conectados.
- Notificar cambios de estado, puntuaciones, turnos y resultados en tiempo real.
- Soportar SockJS como fallback para clientes que no soporten WebSocket nativo.

### 5. API Documentada
- Proveer documentación interactiva de la API mediante Swagger UI (OpenAPI).
- La documentación debe estar disponible en `/swagger-ui/index.html` en entorno de desarrollo.

### 6. Persistencia de Datos
- Utilizar MySQL como base de datos relacional.
- Emplear Spring Data JPA con Hibernate para el mapeo objeto-relacional.
- Mantener la integridad referencial entre entidades (Game, Question, Answer, Participant, GameRound, GameQuestion).

### 7. Calidad y Mantenibilidad del Código
- Seguir principios SOLID y buenas prácticas de diseño.
- Usar Lombok para reducir código boilerplate.
- Mantener una estructura de paquetes clara y por capas (Entity, Repository, Service, Controller, DTO, Config).
- Incluir pruebas unitarias y de integración.

---

## Funcionalidades Actuales (v1)

- [x] Creación de partidas con selección aleatoria de preguntas
- [x] CRUD de preguntas y respuestas
- [x] Registro de participantes por partida
- [x] Host-control: host revela respuestas haciendo clic (sin input de texto)
- [x] Cálculo de puntaje por respuesta correcta (con multiplicador)
- [x] Control de errores por equipo (3 por equipo, transición a STEAL_ATTEMPT)
- [x] Mecánica de robo: equipo contrario puede robar puntos al llegar a 3 errores
- [x] Auto-endRound cuando todas las respuestas se revelan
- [x] Finalización de rondas y partidas
- [x] WebSocket con STOMP y SockJS (broadcast en tiempo real)
- [x] UI premium estilo TV show (Poppins + Orbitron, colores MX, confetti, glow)
- [x] Sonidos con Web Audio API (correcto, incorrecto, robo, celebración, etc.)
- [x] Dashboard con cards y separación de juegos activos/historial
- [x] Documentación Swagger/OpenAPI
- [x] Logging completo (SLF4J backend + console.log frontend)

## Funcionalidades Planeadas / En Desarrollo

- [ ] Sistema de temporizador por turno
- [ ] Autenticación y autorización
- [ ] Panel administrativo web
- [ ] Historial de partidas y estadísticas
- [ ] Soporte para preguntas multimedia (imágenes, audio)
- [ ] Internacionalización (i18n) para otros países hispanohablantes
