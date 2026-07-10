# Mejores Prácticas para Programar con Agentes de IA en 100MD-app

Esta guía establece convenciones y flujos de trabajo recomendados para colaborar con asistentes de IA (agentes) en el desarrollo de 100MD-app.

---

## 1. Convenciones del Proyecto

### Estilo de Código
- **Lombok:** Usar `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` en entidades y DTOs. No escribir getters/setters manualmente.
- **Nomenclatura:**
  - Clases: `PascalCase` (ej. `GameService`, `QuestionController`)
  - Métodos y variables: `camelCase` (ej. `createNewGame()`, `findByGameId`)
  - Constantes: `UPPER_SNAKE_CASE`
  - Paquetes: `minúsculas` (ej. `com.AlanPacheco.CienMD_app.Service`)
- **DTOs vs Entidades:** Nunca exponer entidades JPA directamente en los controladores. Usar DTOs como contratos de API.
- **Inyección de dependencias:** Preferir inyección por constructor (no `@Autowired` en campos).

### Estructura de Capas
```
Controller → DTO → Service → Repository → Entity
                         ↕
                    WebSocket (paralelo)
```

### Base de Datos
- Usar `spring.jpa.hibernate.ddl-auto=update` solo en desarrollo.
- Las consultas personalizadas van en los repositorios con `@Query`.
- Las búsquedas de texto deben ser `case-insensitive` (usar `LOWER()` en JPQL).

---

## 2. Cómo Pedirle al Agente

### Principio General
**Sé específico, pero proporciona contexto.** Un buen prompt incluye:
1. **Qué** quieres que haga (ej. "agregar un endpoint para eliminar preguntas")
2. **Dónde** (ej. "en QuestionController, QuestionService y QuestionRepository")
3. **Cómo** (ej. "siguiendo el patrón de los otros endpoints CRUD")
4. **Verificación** (ej. "después, ejecuta `mvn test`")

### Ejemplos

#### Buen prompt
```
Agrega un endpoint DELETE /api/questions/{id} en QuestionController.
Debe llamar a QuestionService.deleteQuestion(id), que a su vez usa
QuestionRepository.deleteById(). Si la pregunta no existe, devuelve 404.
Sigue el mismo patrón que el endpoint POST /api/questions.
```

#### Mal prompt
```
Haz un delete de preguntas.
```

### Contexto Mínimo Recomendado
Siempre incluir en el mensaje inicial:
- Los archivos relevantes que el agente debe leer
- El patrón a seguir (ej. "mira cómo se hizo en XController")
- Si se requieren pruebas o no

---

## 3. Flujo de Trabajo Recomendado

### Paso 1: Explorar
Antes de escribir código, el agente debe leer los archivos existentes para entender el contexto:
- Buscar archivos similares con glob (`**/*Controller.java`)
- Revisar imports y dependencias
- Entender las convenciones del proyecto

### Paso 2: Planificar
Para cambios complejos (múltiples archivos, nueva funcionalidad), pedir al agente que:
1. Proponga un plan
2. Enumere los archivos a modificar
3. Valide el plan con el desarrollador antes de ejecutar

### Paso 3: Implementar
- Usar el comando `edit` para cambios precisos (no reescribir archivos completos si no es necesario).
- Para archivos nuevos, usar `write`.
- No agregar comentarios redundantes; el código debe ser autoexplicativo.

### Paso 4: Verificar
Siempre solicitar verificación después de cambios:
- Ejecutar `./mvnw compile` para verificar que compila
- Ejecutar `./mvnw test` para las pruebas
- Revisar manualmente el diff con `git diff`

---

## 4. Patrones a Seguir

### Controlador REST
```java
@RestController
@RequestMapping("/api/recurso")
public class RecursoController {

    private final RecursoService service;

    public RecursoController(RecursoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RecursoDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @PostMapping
    public ResponseEntity<RecursoDTO> crear(@RequestBody @Valid RecursoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }
}
```

### Servicio
```java
@Service
public class RecursoService {

    private final RecursoRepository repository;

    public RecursoService(RecursoRepository repository) {
        this.repository = repository;
    }

    public RecursoDTO crear(RecursoDTO dto) {
        Recurso entity = mapper.toEntity(dto);
        entity = repository.save(entity);
        return mapper.toDTO(entity);
    }
}
```

### Repositorio
```java
public interface RecursoRepository extends JpaRepository<Recurso, Long> {
    List<Recurso> findByGameId(Long gameId);

    @Query("SELECT COALESCE(SUM(r.score), 0) FROM Recurso r WHERE r.game.id = :gameId")
    int sumScoreByGameId(@Param("gameId") Long gameId);
}
```

---

## 5. Pruebas

### Unitarias
- Usar JUnit 5 + Mockito.
- Mockear los repositorios en las pruebas de servicio.
- Probar casos felices y casos borde (errores, valores nulos, límites).

### De Integración
- Usar `@SpringBootTest` para pruebas de integración.
- Considerar H2 como base de datos embebida para pruebas.
- Probar el flujo completo: controller → service → repository.

### Comandos Útiles
```bash
./mvnw test                     # Ejecutar todas las pruebas
./mvnw test -Dtest=GameServiceTests  # Prueba específica
./mvnw compile                  # Solo compilar (más rápido que test)
```

---

## 6. Seguridad y Buenas Prácticas

- Nunca incluir credenciales, tokens o claves en el código. Usar variables de entorno o `application.properties` con valores placeholder.
- No exponer entidades JPA directamente al cliente (riesgo de sobre-exposición de datos).
- Validar entradas con `@Valid` y `jakarta.validation`.
- Usar `ResponseEntity` con códigos HTTP apropiados (201 CREATED, 400 BAD REQUEST, 404 NOT FOUND, etc.).

---

## 7. Comandos Útiles para el Día a Día

```bash
# Compilar el proyecto
./mvnw clean compile

# Ejecutar la aplicación
./mvnw spring-boot:run

# Ejecutar pruebas
./mvnw test

# Empaquetar JAR
./mvnw clean package -DskipTests

# Ver el árbol de dependencias
./mvnw dependency:tree

# Swagger UI (en navegador)
# http://localhost:8080/swagger-ui/index.html
```

---

## 8. Checklist para el Agente Antes de Entregar

- [ ] ¿Leí los archivos existentes para entender el contexto?
- [ ] ¿Sigo las convenciones de nomenclatura del proyecto?
- [ ] ¿Usé Lombok donde corresponde?
- [ ] ¿Creé DTOs si voy a exponer datos al cliente?
- [ ] ¿Usé inyección por constructor?
- [ ] ¿El código compila? (`./mvnw compile`)
- [ ] ¿Las pruebas pasan? (`./mvnw test`)
- [ ] ¿Evité comentarios innecesarios?
- [ ] ¿No incluí credenciales ni secretos?
- [ ] ¿Solicité confirmación antes de cambios destructivos?
