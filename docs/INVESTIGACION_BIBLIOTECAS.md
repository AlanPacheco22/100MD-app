# Investigación de Bibliotecas y Plugins — 100MD-app

> Fecha: 2026-07-10  
> Stack actual: Java 17, Spring Boot 3.3.5, JPA/Hibernate, MySQL, Flyway, Thymeleaf + Alpine.js 3.14.1 + Bootstrap 5, SockJS + STOMP, Lombok, Springdoc OpenAPI

---

## 1. MAPPER: MapStruct (reemplaza mapeo manual DTO↔Entity)

**Problema actual:** `GameService.mapToGameDTO()` y `mapToGameQuestionDTO()` son 41 y 31 líneas respectivamente de mapeo manual campo por campo. Hay 15+ DTOs y 10+ Entities, cada uno con mappers manuales similares.

**Solución:** MapStruct genera implementaciones de mappers en compile-time.

```java
@Mapper(componentModel = "spring")
public interface GameMapper {
    GameDTO toDto(Game game);
    Game toEntity(GameDTO dto);
}
```

**Impacto estimado:**
- Elimina ~400 líneas de mapeo manual en GameService alone
- Elimina errores de runtime (compile-time checking)
- Zero overhead de reflexión

**Dependencia:**
```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.6.3</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.6.3</version>
    <scope>provided</scope>
</dependency>
```

**Veredicto: ALTA PRIORIDAD** — Reemplaza el mayor boilerplate del backend.

---

## 2. TESTCONTAINERS (reemplaza H2 en tests)

**Problema actual:** Tests usan H2 que no soporta todas las features de MySQL (ENUMs, sintaxis específica). Los tests pasan pero no reflejan la realidad de producción.

**Solución:** Testcontainers levanta un MySQL real en Docker durante los tests.

```java
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfig {
    @Bean
    MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    }
}
```

**Impacto estimado:**
- Tests más confiables (mismos ENUMs, indexes, etc.)
- Descubre bugs como el ENUM mismatch ANTES de producción
- Spring Boot 3.1+ tiene `@ServiceConnection` para integración automática

**Dependencia:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>mysql</artifactId>
    <scope>test</scope>
</dependency>
```

**Veredicto: ALTA PRIORIDAD** — Hubiera prevenido el bug del ENUM que acabamos de arreglar.

---

## 3. CANVAS-CONFETTI (reemplaza createConfetti() manual)

**Problema actual:** `createConfetti()` en play.html es código vanilla canvas (~30 líneas) para el efecto de celebración.

**Solución:** canvas-confetti es una librería de 8KB que hace esto y mucho más.

```html
<script src="https://cdn.jsdelivr.net/npm/canvas-confetti@1.9.3/dist/confetti.browser.min.js"></script>
```
```javascript
// Celebración completa con un solo línea:
confetti({ particleCount: 150, spread: 100, origin: { y: 0.6 } });
```

**Impacto estimado:**
- Reemplaza ~30 líneas de canvas manual
- Añade soporte para `disableForReducedMotion` (accesibilidad)
- Mejores efectos: fireworks, shapes personalizados, colores configurables

**Veredicto: MEDIA PRIORIDAD** — Mejora visual inmediata con mínimo esfuerzo.

---

## 4. HOWLER.JS (reemplaza AudioManager manual)

**Problema actual:** `AudioManager` en play.html es un objeto manual con `new Audio()` y `play()` vanilla. Maneja archivos de audio estáticos pero sin control fino de volumen, pooling, o crossfading.

**Solución:** Howler.js es el estándar para audio en el web (719K weekly downloads en npm).

```html
<script src="https://cdn.jsdelivr.net/npm/howler@2.2.4/dist/howler.min.js"></script>
```
```javascript
const correctSound = new Howl({ src: ['/sounds/correct.mp3'], volume: 0.7 });
const wrongSound = new Howl({ src: ['/sounds/wrong.mp3'], volume: 0.7 });
```

**Ventajas sobre Audio() vanilla:**
- Auto-preloading de archivos
- Pooling de instancias (evita clicks/overlap)
- Control de volumen global/individual
- Fade in/out
- Soporte multi-formato (MP3, OGG, WAV)
- Manejo automático de Web Audio API vs HTML5 Audio

**Alternativa: Tone.js** — Más pesado (synthesized audio), mejor para juegos que generan sonidos en tiempo real. Overkill para nuestro caso.

**Veredicto: BAJA PRIORIDAD** — El AudioManager actual funciona, Howler es nice-to-have.

---

## 5. ALPINE.JS COMPONENTS + PLUGINS

### 5a. Alpine Fetch (simplifica HTTP requests)

**Problema actual:** Cada `async method()` en play.html repite el patrón try/catch/fetch/ok/error (~15 veces).

**Solución:** `alpine-fetch` plugin añade `$fetch` y `$fetchjson` magic helpers.

```html
<script defer src="https://cdn.jsdelivr.net/gh/hankhank10/alpine-fetch@main/alpine-fetch.js"></script>
```
```html
<!-- Antes: 6 líneas -->
<div x-data="{ result: null, async load() { 
    const res = await fetch('/api/endpoint'); 
    this.result = await res.json(); 
} }" x-init="load()">

<!-- Después: 1 línea -->
<div x-data>
    <span x-text="await $fetchjson('/api/endpoint')"></span>
</div>
```

**Impacto:** Moderado — Reduce verbosidad pero no elimina la lógica de negocio (updateGameState, AudioManager.play, etc.)

### 5b. Alpine.data() (componentes reutilizables)

**Problema actual:** `gameBoard()` es una función de 883 líneas que maneja todo el estado.

**Solución:** Dividir en componentes Alpine.data():

```javascript
document.addEventListener('alpine:init', () => {
    Alpine.data('gameTimer', () => ({
        remaining: 0, running: false,
        start() { /* ... */ },
        stop() { /* ... */ }
    }));
    
    Alpine.data('faceOff', () => ({
        player1: null, player2: null, buzzed: false,
        submit() { /* ... */ }
    }));
});
```

**Impacto estimado:** Alto — Divide el monolito en ~6 componentes manejables (Timer, FaceOff, FastMoney, Setup, GameBoard, Results).

### 5c. Alpidate (validación de formularios)

Plugin para validación declarativa inspirado en Vuelidate. Útil para el form de setup de equipos.

**Veredicto: BAJA PRIORIDAD** — El setup actual funciona con validación manual mínima.

---

## 6. JPA ATTRIBUTE CONVERTER (reemplaza mapeo manual int[] ↔ String)

**Problema actual:** `Game.getMultipliersArray()` y `setMultipliersArray()` son métodos manuales que convierten entre `int[]` y `String` (comma-separated) para la columna `round_multipliers`.

**Solución:** Un `AttributeConverter<int[], String>` automatiza esto:

```java
@Converter
public class IntArrayConverter implements AttributeConverter<int[], String> {
    @Override
    public String convertToDatabaseColumn(int[] array) {
        if (array == null) return null;
        return Arrays.stream(array).mapToObj(String::valueOf)
            .collect(Collectors.joining(","));
    }
    
    @Override
    public int[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return new int[0];
        return Arrays.stream(dbData.split(","))
            .mapToInt(Integer::parseInt).toArray();
    }
}
```

Uso en Entity:
```java
@Convert(converter = IntArrayConverter.class)
@Column(name = "round_multipliers")
private int[] roundMultipliers;
```

**Impacto estimado:** Bajo pero limpia el código. Elimina 2 métodos utility de Game.java.

**Veredicto: BAJA PRIORIDAD** — Nice cleanup, no es urgente.

---

## 7. SPRING REST DOCS / SPRINGDOC OPENAPI (ya tiene)

**Estado actual:** Ya usa `springdoc-openapi-starter-webmvc-ui` v2.2.0. Esto genera Swagger UI automáticamente en `/swagger-ui.html`.

**Acción:** Ninguna. Ya está implementado.

---

## 8. HTMX vs Alpine.js (consideración arquitectónica)

**Pregunta:** ¿Deberíamos reemplazar Alpine.js por HTMX?

**Análisis:**
- HTMX: Ideal para apps server-rendered que hacen intercambio de HTML parcial. Muy bueno para CRUDs simples.
- Alpine.js: Ideal para interactividad compleja en el cliente (estado, timers, WebSocket).
- **Nuestro caso:** El juego requiere WebSocket bidireccional, timers del lado del cliente, manejo de estado complejo (turnos, rondas, etc.). HTMX no maneja bien esto.

**Veredicto: NO CAMBIAR** — Alpine.js es la elección correcta para este tipo de app interactiva.

---

## Resumen de Prioridades

| # | Librería | Ahorro estimado | Prioridad | Esfuerzo |
|---|---|---|---|---|
| 1 | **MapStruct** | ~400 líneas Java | ALTA | Medio |
| 2 | **Testcontainers** | Previene bugs críticos | ALTA | Medio |
| 3 | **canvas-confetti** | ~30 líneas JS | Media | Bajo |
| 4 | **Alpine.data() refactor** | Divide 883 líneas en ~6 archivos | Media | Alto |
| 5 | **Alpine Fetch** | ~100 líneas JS | Media | Bajo |
| 6 | **AttributeConverter** | ~20 líneas Java | Baja | Bajo |
| 7 | **Howler.js** | Mejora audio, ~0 líneas ahorradas | Baja | Bajo |

### Recomendación Inmediata
1. **MapStruct** — El mayor ROI. Elimina el mayor boilerplate y previene errores de mapeo.
2. **Testcontainers** — Previene bugs como el ENUM mismatch que acabamos de fixear.
3. **canvas-confetti** — Quick win visual con CDN, 0 configuración.
