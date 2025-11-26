# Copilot Instructions for This Repository

This is a Spring Boot 3.5 (Java 17) backend with JWT auth, layered architecture, and JPA/H2 for development. Use these project-specific notes to be productive fast.

## Architecture & Boundaries
- Controllers: `controller/*Controller.java` expose REST endpoints for `productos` and `pedidos`. Auth endpoints live in `AuthController` under `/api/auth/*`.
- Services: `service/*Service.java` contain business logic and DTO/entity transformations.
- Persistence: `repository/*Repository.java` use Spring Data JPA with custom `@Query` JPQL and optimized `@Modifying` updates.
- Models & DTOs: Entities in `model/*` (tables `productos`, `pedidos`, `usuarios`); transport objects in `dto/*`.
- Responses: Business controllers return `ResponseDTO<T>` for consistent messages/data/timestamps. Auth endpoints return simple maps (`token`, `error`). See `ResponseDTO` for shape.
- Security: All endpoints require JWT except `/api/auth/**` and static root. See `config/SecurityConfig.java`, `security/JwtAuthFilter.java`, `security/JwtUtils.java`.
- CORS: Configured in both `SecurityConfig.corsConfigurationSource()` and `config/WebConfig.java`. Update allowed origins in BOTH places when adding frontends.

## Auth Flow (JWT)
- Register: `POST /api/auth/register` with `{"username":"u","password":"p"}`.
- Login: `POST /api/auth/login` returns `{ "token": "<jwt>" }` on success.
- Use token: send header `Authorization: Bearer <jwt>` on protected routes, e.g. `GET /api/productos`.
- Users seeded in dev: `admin/admin123` (ROLE_ADMIN) and `cliente/cliente123`. See `DataInitializer`.

## API Conventions & Examples
- Productos
  - `GET /api/productos` → `ResponseDTO<List<ProductoDTO>>`
  - `POST /api/productos` with `ProductoDTO` (Jakarta validation on fields) → 201 Created
- Pedidos
  - `POST /api/pedidos` with `PedidoDTO` → starts as `PENDIENTE`
  - `PUT /api/pedidos/{id}/estado?estado=ATENDIDO|CANCELADO`
- Validation errors are centralized in `exception/GlobalExceptionHandler.java` and returned as `ResponseDTO` with field map for `MethodArgumentNotValidException`.

## Build, Run, Test
- Java 17, Spring Boot `${spring.boot.version}` in `pom.xml`. Dev DB: in-memory H2; console at `/h2-console`.
- Build JAR: Windows `mvnw.cmd -DskipTests clean package`; Unix `./mvnw -DskipTests clean package`.
- Run locally:
  - Maven: Windows `mvnw.cmd spring-boot:run`; Unix `./mvnw spring-boot:run`.
  - JAR: `java -jar target/backend-0.0.1-SNAPSHOT.jar` (uses `server.port=${PORT:8080}`).
- Tests: `mvnw.cmd test` (Windows) / `./mvnw test` (Unix). Unit tests are minimal.

## Configuration & Env
- Properties in `src/main/resources/application.properties`:
  - `app.jwt.secret` and `app.jwt.expiration-ms` feed `JwtUtils`.
  - `server.port` binds to `${PORT:8080}` for platforms like Render/Heroku.
  - CORS sample list in properties is informational; effective origins live in code configs noted above.
- Override via env vars using Spring mapping (e.g., `APP_JWT_SECRET`, `APP_JWT_EXPIRATION_MS`, `PORT`).

## Repositories & Data Access Patterns
- Prefer custom `@Query` methods for targeted reads, and `@Modifying @Transactional` for batch/atomic updates (e.g., `PedidoRepository.actualizarEstado`).
- Services perform DTO ↔ entity mapping; avoid mapping in controllers or repositories.
- Use repository existence checks (`existePorId`) to guard updates/deletes instead of loading full entities.

## Deployment Hooks
- Docker: multi-stage build in `Dockerfile` (Maven builder → Temurin JRE). Entry: `java -jar /app/app.jar`.
- Procfile: `web: java -jar target/backend-0.0.1-SNAPSHOT.jar` (Heroku-style).
- Render notes: see `Dockerfile-RENDER-GUIDE` for health checks and env guidance.

## Gotchas & Tips
- CORS: update BOTH `SecurityConfig` and `WebConfig` when adding origins; ensure no trailing slashes.
- Response shape: keep business endpoints on `ResponseDTO<T>`; Auth may return raw maps for simplicity.
- Auth-required by default: only `/api/auth/**` is public. Always attach `Authorization: Bearer` when testing product/pedido routes.
- H2 dev DB resets on restart; seed users are re-created by `DataInitializer`.

If any section is unclear or missing (e.g., additional endpoints or non-obvious workflows), tell me what to expand and I’ll refine this file.
