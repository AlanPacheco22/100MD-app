# Changelog

Todos los cambios notables en **100MD-app** estarán documentados en este archivo.

El formato se basa en [Keep a Changelog](https://keepachangelog.com/es/1.1.0/),
y el proyecto adherisce a [Semantic Versioning](https://semver.org/lang/es/).

---

## [Unreleased]

### Added
- (pendiente)

### Changed
- (pendiente)

### Fixed
- (pendiente)

---

## [0.1.0] - 2026-07-10

Versión inicial funcional del juego "100 Mexicanos Dijeron" con UI premium estilo TV show.

### Added

#### Juego
- Mecánica de robo (STEAL_ATTEMPT): equipo contrario puede robar puntos al llegar a 3 errores ([`7de1ca2`](https://github.com/AlanPacheco22/100MD-app/commit/7de1ca2))
- Host-control: host revela respuestas haciendo clic, sin input de texto ([`987a23a`](https://github.com/AlanPacheco22/100MD-app/commit/987a23a))
- Botón ERROR/STRIKE con contador X/3 ([`7de1ca2`](https://github.com/AlanPacheco22/100MD-app/commit/7de1ca2))
- Auto-endRound cuando todas las respuestas se revelan ([`7de1ca2`](https://github.com/AlanPacheco22/100MD-app/commit/7de1ca2))
- Lógica Family Feud con control de turnos y rondas ([`b1ae75e`](https://github.com/AlanPacheco22/100MD-app/commit/b1ae75e))
- Configuración flexible de partidas (rondas, multiplicadores, miembros por equipo) ([`334e2b5`](https://github.com/AlanPacheco22/100MD-app/commit/334e2b5))

#### Interfaz
- UI premium estilo TV show con Poppins + Orbitron ([`b61b958`](https://github.com/AlanPacheco22/100MD-app/commit/b61b958))
- Colores mexicanos: verde `#006847`, rojo `#CE1126`, dorado `#FFD700` ([`b61b958`](https://github.com/AlanPacheco22/100MD-app/commit/b61b958))
- Efectos de confetti, glow y animaciones ([`987a23a`](https://github.com/AlanPacheco22/100MD-app/commit/987a23a))
- Dashboard rediseñado con cards y separación de juegos activos/historial ([`cc17c5f`](https://github.com/AlanPacheco22/100MD-app/commit/cc17c5f))
- Navegación consistente con navbar oscura en todas las páginas ([`90aa6f0`](https://github.com/AlanPacheco22/100MD-app/commit/90aa6f0))
- Pantalla de resultados dedicada al final del juego ([`987a23a`](https://github.com/AlanPacheco22/100MD-app/commit/987a23a))

#### Sonidos
- Sonidos con Web Audio API: correcto, incorrecto, robo, celebración, reveal, roundStart, roundEnd ([`5807f1b`](https://github.com/AlanPacheco22/100MD-app/commit/5807f1b))

#### Backend
- WebSocket con STOMP y SockJS para broadcast en tiempo real ([`fe97d84`](https://github.com/AlanPacheco22/100MD-app/commit/fe97d84))
- Admin panel con CRUD de preguntas y respuestas ([`698bb86`](https://github.com/AlanPacheco22/100MD-app/commit/698bb86))
- Game history y player statistics ([`b0584df`](https://github.com/AlanPacheco22/100MD-app/commit/b0584df))
- Login y módulos web ([`27ee957`](https://github.com/AlanPacheco22/100MD-app/commit/27ee957))
- Base de datos con Flyway migrations y seed data ([`9e022c6`](https://github.com/AlanPacheco22/100MD-app/commit/9e022c6))
- Docker deployment ([`818d7fa`](https://github.com/AlanPacheco22/100MD-app/commit/818d7fa))
- Tests con H2 y GitHub Actions CI ([`6962db5`](https://github.com/AlanPacheco22/100MD-app/commit/6962db5))
- Documentación Swagger/OpenAPI
- Logging completo (SLF4J backend + console.log frontend) ([`7de1ca2`](https://github.com/AlanPacheco22/100MD-app/commit/7de1ca2))

### Fixed
- CORS WebSocket: `setAllowedOrigins("*")` → `setAllowedOriginPatterns("http://localhost:*")` ([`7de1ca2`](https://github.com/AlanPacheco22/100MD-app/commit/7de1ca2))
- Bugs críticos en lógica del juego + logs debug ([`ed48220`](https://github.com/AlanPacheco22/100MD-app/commit/ed48220))
- Fix duplicate endpoint ([`6962db5`](https://github.com/AlanPacheco22/100MD-app/commit/6962db5))
- Corrección del Core — el proyecto compila ([`3e46d9f`](https://github.com/AlanPacheco22/100MD-app/commit/3e46d9f))

### Changed
- Dashboard rediseñado con cards ([`cc17c5f`](https://github.com/AlanPacheco22/100MD-app/commit/cc17c5f))
- play.html reescritura completa — game board estilo TV ([`987a23a`](https://github.com/AlanPacheco22/100MD-app/commit/987a23a))
- Navegación consistente con navbar oscura ([`90aa6f0`](https://github.com/AlanPacheco22/100MD-app/commit/90aa6f0))
- CSS reescritura completa — premium TV show aesthetic ([`b61b958`](https://github.com/AlanPacheco22/100MD-app/commit/b61b958))
- Arquitectura limpia ([`7d1f9a2`](https://github.com/AlanPacheco22/100MD-app/commit/7d1f9a2))

---

[unreleased]: https://github.com/AlanPacheco22/100MD-app/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/AlanPacheco22/100MD-app/releases/tag/v0.1.0
