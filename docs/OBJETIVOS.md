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
- **Regla de errores:** El equipo 1 puede fallar hasta 3 respuestas por ronda; el equipo 2 puede fallar hasta 1.
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
- [x] Envío de respuestas y validación contra la base de datos
- [x] Cálculo de puntaje por respuesta correcta (con multiplicador)
- [x] Control de errores por equipo (3 para equipo 1, 1 para equipo 2)
- [x] Finalización de rondas y partidas
- [x] Configuración de WebSocket con STOMP y SockJS
- [x] Documentación Swagger/OpenAPI

## Funcionalidades Planeadas / En Desarrollo

- [ ] Transmisión completa de actualizaciones vía WebSocket
- [ ] Sistema de temporizador por turno
- [ ] Autenticación y autorización
- [ ] Panel administrativo web
- [ ] Historial de partidas y estadísticas
- [ ] Soporte para preguntas multimedia (imágenes, audio)
- [ ] Internacionalización (i18n) para otros países hispanohablantes
