# AGENTS

## Resumen
- Plataforma de reservas de cine construida con Spring Boot 3.3.2 y Java 17.
- Renderiza vistas publica y admin con Thymeleaf, persistencia en PostgreSQL y cache/hold en Redis.
- Maneja autenticacion JWT, reservas con expiracion de asientos y cobros sandbox mediante Wompi.

## Tecnologias Clave
- Java 17 (Temurin) y Maven 3.9.x.
- Spring Boot starters: web, thymeleaf, data-jpa, security, validation, actuator, data-redis.
- Flyway para migraciones SQL (`backend/src/main/resources/db/migration`).
- PostgreSQL 15 y Redis 7 segun `docker-compose.yml` en la raiz.
- JWT via `io.jsonwebtoken:jjwt-*` y ZXing 3.5.3 para codigos QR.

## Estructura de Carpetas
- `backend/pom.xml`: definicion de dependencias, plugins y Java 17.
- `backend/src/main/java/com/cine/`: codigo fuente principal con paquetes `config`, `controller`, `domain`, `dto`, `jobs`, `repo`, `security`, `service`.
- `backend/src/main/resources/`: configuraciones (`application.yml` y perfiles), migraciones Flyway y plantillas Thymeleaf.
- `docker-compose.yml`: orquesta app + PostgreSQL + Redis para desarrollo local.
- `backend/Dockerfile`: build multi-stage que empaca `cine-app-0.0.1-SNAPSHOT.jar`.

## Configuracion y Variables de Entorno
- Base de datos: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (por defecto `postgres`/`admin`).
- Redis: `REDIS_HOST`, `REDIS_PORT`.
- Seguridad JWT: `JWT_SECRET`, `JWT_EXP_MINUTES`.
- Integracion Wompi: `WOMPI_PUBLIC_KEY`, `WOMPI_INTEGRITY_KEY`, `WOMPI_EVENTS_SECRET`, `WOMPI_REDIRECT_URL`, `WOMPI_CHECKOUT_BASE`, `WOMPI_CURRENCY`.
- Perfil dev activa devtools y SQL logging (`application-dev.yml`).

## Flujo Principal de Reserva
1. Usuario ve cartelera (`GET /`) y detalles de pelicula (`GET /peliculas/{id}`) desde `PublicController`.
2. Selecciona funcion y asientos (`FuncionController`, `FunctionPageController`).
3. Autenticacion via `POST /api/auth/register` o `POST /api/auth/login` (`AuthController` + `AuthService`).
4. Hold de asientos y creacion de reserva con TTL Redis (`POST /api/reservas/hold`, `HoldService`, `ReservaService`).
5. Inicio de checkout (`POST /api/reservas/checkout`, `PagoService`) y redireccion a Wompi (`CheckoutController`).
6. Webhook `POST /webhooks/wompi` actualiza `Pago` y marca reserva pagada (`WebhookController`, `ReservaService`).
7. Usuario consulta compras y tickets QR (`ComprasController`, `TicketController`).

## Seguridad
- `SecurityConfig` define app stateless con `JwtAuthFilter` y excepcion custom `HtmlRedirectEntryPoint` para redirigir a `/login` en vistas HTML.
- Roles: `USER` y `ADMIN` (`Enums.RolUsuario`), rutas admin requieren `ROLE_ADMIN`.
- Tokens firmados con `JwtUtil`; se aceptan en header Bearer o cookie `AUTH`.

## Jobs y Procesos en Segundo Plano
- `CineApplication` aplica `@EnableScheduling`.
- `HoldCleanupJob` ejecuta cada 60s, libera holds expirados tanto en Redis como en la base (`HoldService.releaseExpiredHolds`).

## Persistencia y Migraciones
- `V1__init.sql` crea tablas: `categoria`, `pelicula`, `sala`, `asiento`, `funcion`, `asiento_funcion`, `usuario`, `reserva`, `reserva_asientos`, `pago`.
- `DataInit` asegura usuario admin (`admin@cine.local`/`admin123`) al arrancar.
- Estados y enums centralizados en `domain/Enums.java` (asientos, reservas, pagos, roles, clasificacion, formato).

## Templates y Frontend Server-Side
- Paginas publicas en `templates/cartelera.html`, `templates/pelicula.html`, `templates/funcion_seats.html`.
- Flujos de autenticacion en `templates/login.html` y `templates/register.html`.
- Consola admin en `templates/admin/...` para crear salas, peliculas, funciones, usuarios.
- Estilos simples en `static/css/styles.css`.

## Setup Local
- Con Docker: `docker-compose up --build`. Levanta PostgreSQL, Redis y backend (puerto 8080). Las migraciones Flyway y seeds se aplican al iniciar.
- Desarrollo hot reload: usar servicio `app-dev` del compose (monta codigo y ejecuta `mvn spring-boot:run` con perfil `dev`).
- Manual sin contenedores:
  1. Instalar Java 17 y Maven.
  2. Configurar PostgreSQL y Redis segun `application.yml`.
  3. Ejecutar `mvn clean package` (opcional `-DskipTests`).
  4. Correr `mvn spring-boot:run` o `java -jar target/cine-app-0.0.1-SNAPSHOT.jar`.

## Testing
- Dependencia `spring-boot-starter-test` incluida, pero actualmente no hay clases bajo `src/test/java`.
- Recomendado agregar pruebas para: autenticacion (`AuthService`), holds/reservas (`HoldService`, `ReservaService`), y webhooks (`WebhookController`).

## Observabilidad y Ops
- Actuator habilitado; endpoints principales disponibles bajo `/actuator` (asegurar configuracion y proteccion en despliegues).
- `spring.jpa.open-in-view=false` y logs SQL controlados por `application.yml`.
- Dockerfile produce imagen lista para produccion; ajustar `JAVA_OPTS` para memoria/monitoring.

## Despliegue
- Build CI sugerido: `mvn -DskipTests clean package` seguido de `docker build -t cine-app .`.
- Variables sensibles deben inyectarse via entorno (no commitear `.env`).
- Considerar manejar colas de webhooks y reintentos en ambientes productivos.
