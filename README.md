# 100MD-app

API backend del juego **"100 Mexicanos Dijeron"**, la versión mexicana del formato televisivo *Family Feud*.

Dos equipos compiten adivinando las respuestas más populares a preguntas de encuesta. Gana el que acumule más puntos.

---

## Stack Tecnológico

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.3.5 |
| Maven | Wrapper (`mvnw`) |
| MySQL | - |
| Spring Data JPA | Hibernate |
| WebSocket | STOMP + SockJS |
| Swagger | OpenAPI (springdoc) |
| Lombok | 1.18.24 |

---

## Requisitos

- Java 17 JDK
- MySQL (o Docker para levantar MySQL)
- Maven (o usar `mvnw` incluido)

---

## Ejecución Local

### 1. Base de datos

```bash
# Con Docker
docker run --name mysql-100md -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=cienmd_db -p 3306:3306 -d mysql:8
```

### 2. Configurar conexión

Editar `src/main/resources/application.properties` con tus credenciales MySQL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cienmd_db
spring.datasource.username=root
spring.datasource.password=root
```

### 3. Iniciar la app

```bash
./mvnw spring-boot:run
```

La app arranca en `http://localhost:8080`.

---

## Documentación de la API

Disponible en Swagger UI cuando la app está corriendo:

➡️ **http://localhost:8080/swagger-ui/index.html**

---

## Endpoints Principales

### Partidas (Games)

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/games` | Crear partida (asigna 3 preguntas aleatorias) |
| `GET` | `/api/games` | Listar todas las partidas |
| `GET` | `/api/games/{id}` | Obtener partida por ID |
| `GET` | `/api/games/{id}/results` | Resultados finales de la partida |

### Rondas

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/games/{id}/rounds/start` | Iniciar siguiente ronda |
| `POST` | `/api/games/{id}/rounds/answer` | Enviar respuesta de un participante |
| `POST` | `/api/games/{id}/rounds/end` | Finalizar ronda actual |

### Participantes

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/games/{id}/participants` | Agregar participante |
| `GET` | `/api/games/{id}/participants` | Listar participantes de una partida |

### Preguntas y Respuestas

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/questions` | Crear pregunta con respuestas |
| `GET` | `/api/questions` | Listar todas las preguntas |

---

## WebSocket

- **Endpoint:** `/ws` (con SockJS)
- **Tópico de suscripción:** `/topic/gameUpdates`
- **Destino de envío:** `/app/updateGame`

Los clientes reciben actualizaciones en tiempo real sobre cambios en la partida.

---

## Reglas del Juego

1. Se muestran 3 preguntas por partida (seleccionadas aleatoriamente del banco).
2. El **Equipo 1** juega primero y puede fallar hasta **3 respuestas** por ronda.
3. El **Equipo 2** juega después y puede fallar hasta **1 respuesta** por ronda.
4. Cada acierto suma el puntaje de la respuesta (porcentaje de encuesta) × un multiplicador.
5. Al final de las 3 rondas, gana el equipo con mayor puntaje acumulado.

---

## Estructura del Proyecto

```
src/main/java/com/AlanPacheco/CienMD_app/
├── Application.java
├── Config/          # Swagger, WebSocket
├── Controller/      # REST endpoints
├── DTO/             # Objetos de transferencia
├── Entity/          # Modelo JPA
├── Enum/            # Constantes del dominio
├── Repository/      # Acceso a datos
└── Service/         # Lógica de negocio
```

---

## Documentación Adicional

- [Objetivos del Proyecto](docs/OBJETIVOS.md)
- [Arquitectura](docs/ARCHITECTURE.md)
- [Mejores Prácticas con Agentes de IA](docs/BEST_PRACTICES_AGENTS.md)
