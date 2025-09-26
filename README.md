# Cine App (Spring Boot 3, Java 17)

Aplicacion demo de reservas de cine con Spring Boot 3.3.2, Java 17 y Thymeleaf. Incluye autenticacion JWT, manejo de asientos con hold temporal en Redis y cobros sandbox con Wompi.

## Caracteristicas principales
- API y vistas server-side con Spring Boot, Spring MVC y Thymeleaf.
- Persistencia en PostgreSQL 15 con migraciones gestionadas por Flyway.
- Almacenamiento temporal de holds en Redis 7 para liberar asientos expirados.
- Seguridad stateless con JWT y roles USER/ADMIN.
- Integracion de pagos sandbox Wompi, redireccion y webhook de confirmacion.
- Codigos QR de tickets generados con ZXing.

## Arquitectura y bases de datos
- **PostgreSQL**: base relacional definida en `backend/src/main/resources/db/migration/V1__init.sql`. Tablas para catalogo (pelicula, sala, asiento, funcion), usuarios, reservas, pagos y asociaciones intermedias.
- **Redis**: guarda holds de asientos con TTL; `HoldCleanupJob` limpia expirados cada 60 segundos.
- **Flyway**: aplica las migraciones al iniciar la aplicacion.
- **Docker Compose**: levanta contenedores para backend, PostgreSQL y Redis.

## Estructura del proyecto
- `backend/src/main/java/com/cine/` contiene configuracion, controladores, servicios, repositorios y dominios.
- `backend/src/main/resources/templates/` almacena vistas Thymeleaf publicas y admin.
- `backend/src/main/resources/application*.yml` define configuraciones por perfil (dev, prod).
- `backend/pom.xml` declara dependencias (Spring Boot starters, jjwt, ZXing) y plugins Maven.
- `docker-compose.yml` orquesta los servicios para desarrollo.
- `backend/Dockerfile` crea la imagen de produccion del backend.

## Tecnologias
- Java 17 (Temurin) y Maven 3.9.x.
- Spring Boot 3.3.2: web, data-jpa, security, validation, thymeleaf, redis, actuator.
- PostgreSQL 15 + Flyway.
- Redis 7.
- JWT via `io.jsonwebtoken`.
- ZXing 3.5.3 para QR.
- Docker y Docker Compose.

## Configuracion y variables de entorno
Variables principales (se pueden definir en `docker-compose.yml` o entorno):

- Base de datos: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.
- Redis: `REDIS_HOST`, `REDIS_PORT`.
- JWT: `JWT_SECRET`, `JWT_EXP_MINUTES`.
- Wompi: `WOMPI_PUBLIC_KEY`, `WOMPI_INTEGRITY_KEY`, `WOMPI_EVENTS_SECRET`, `WOMPI_REDIRECT_URL`, `WOMPI_CHECKOUT_BASE`, `WOMPI_CURRENCY`.

El perfil `dev` habilita devtools y trazas SQL (`application-dev.yml`).

## Flujo funcional
1. El usuario consulta la cartelera en `/`.
2. Selecciona pelicula y funcion para ver el mapa de asientos.
3. Se registra o inicia sesion (`/api/auth/register`, `/api/auth/login`).
4. Crea un hold de asientos (`/api/reservas/hold`) que se guarda con TTL en Redis y en la base.
5. Inicia el checkout (`/api/reservas/checkout`) y es redirigido a Wompi.
6. Webhook `/webhooks/wompi` actualiza el estado del pago y libera o confirma la reserva.
7. Consulta compras y tickets QR en `/mis-compras`.

## Puesta en marcha rapida (Docker)
Requisitos: Docker y Docker Compose instalados.

```
docker compose up --build
```

Servicios expuestos:
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432 (DB `cine`, usuario `postgres`, clave `admin`)
- Redis: localhost:6379

Flyway aplica las migraciones y se carga un usuario admin (`admin@cine.local` / `admin123`) junto con datos demo (pelicula, sala y funcion).

Configura el webhook de Wompi apuntando a `http://localhost:8080/webhooks/wompi`.

## Ejecucion local sin contenedores
1. Instala Java 17, Maven 3.9.x, PostgreSQL 15 y Redis 7.
2. Crea la base `cine` y credenciales definidas por variables de entorno.
3. Exporta variables (`DB_*`, `REDIS_*`, `JWT_*`, `WOMPI_*`).
4. Desde `backend/` ejecuta:

```
mvn clean package
mvn spring-boot:run
```

Opcional: `java -jar target/cine-app-0.0.1-SNAPSHOT.jar`.

## Administracion
- Consola admin disponible en `/admin`.
- Endpoints REST protegidos con rol `ADMIN` (por ejemplo `/api/admin/salas`, `/api/admin/peliculas`, `/api/admin/funciones`).
- Autenticacion via JWT en header Bearer o cookie `AUTH`.

## Testing y monitoreo
- Dependencia `spring-boot-starter-test` disponible; se recomienda agregar pruebas para `AuthService`, `HoldService`, `ReservaService` y `WebhookController`.
- Actuator habilitado en `/actuator`; protege los endpoints en despliegues publicos.
- `spring.jpa.open-in-view=false` y logging SQL configurable en `application.yml`.

## Siguientes pasos sugeridos
- Configurar despliegue CI/CD con `mvn -DskipTests clean package` y `docker build -t cine-app .`.
- Ajustar `JAVA_OPTS` y variables sensibles mediante secretos o gestor de configuracion.
