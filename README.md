# Cine App (Spring Boot 3, Java 17)

Proyecto demo de cine con backend Spring Boot + Thymeleaf minimal, PostgreSQL, Redis, seguridad JWT, reservas de asientos con hold (TTL) y pagos con Wompi (sandbox). Se entrega listo para ejecutar con docker-compose.

Servicios:
- Backend: :8080
- PostgreSQL 15: :5432 (DB: cine, user: postgres, pass: admin)
- Redis 7: :6379

Requisitos:
- Docker y Docker Compose
- Variables Wompi (sandbox) en docker-compose.yml: WOMPI_PUBLIC_KEY, WOMPI_INTEGRITY_KEY, WOMPI_EVENTS_SECRET

Arranque:
docker-compose up --build

El backend aplicará migraciones Flyway y cargará datos demo:
- Película demo, Sala 1 (5x8), una función, asientos libres.
- Usuario admin: admin@cine.local / password: admin123 (para API admin vía JWT)

Flujo (resumen):
1) Ver cartelera: http://localhost:8080/
2) Obtener asientos de una función: GET /funcion/{id}/asientos
3) Autenticarse (JWT): POST /api/auth/register o POST /api/auth/login
4) Hacer hold y crear reserva: POST /api/reservas/hold con { funcionId, asientoIds[] }
5) Ir a checkout Wompi: abrir GET /checkout/start/{reservaId} (redirige a Wompi)
6) Wompi redirige a /checkout/return y envía webhook a /webhooks/wompi
7) Ver compras y QR: http://localhost:8080/mis-compras

Webhooks (Wompi):
Configura en el panel de Wompi (sandbox) el webhook apuntando a http://localhost:8080/webhooks/wompi y usa el WOMPI_EVENTS_SECRET correspondiente.

Admin (API):
- Crear sala: POST /api/admin/salas  body { "nombre":"Sala 2", "filas":5, "columnas":8 }
- Crear película: POST /api/admin/peliculas  body { "titulo":"Matrix", "sinopsis":"...", "duracionMin":136 }
- Crear función: POST /api/admin/funciones  body { "peliculaId":1, "salaId":1, "horaInicio":"2025-09-08T20:00:00", "precioBase":15000 }
Requiere rol ADMIN via JWT.

Notas:
- Holds: se guardan en Redis con TTL y espejo en DB. Un job cada 60s libera expirados.
- Integración Wompi: checkout redirigido; firma signature:integrity = SHA-256(reference + amountInCents + currency + integrityKey).
- Seguridad: JWT simple. Usa JWT_SECRET para firmar tokens.
- QR: generado con ZXing a partir del id de la reserva.

## Configuración Wompi (SV)

Variables disponibles en `application.yml` (puedes sobre-escribir por env):

- `WOMPI_PUBLIC_KEY`
- `WOMPI_INTEGRITY_KEY`
- `WOMPI_EVENTS_SECRET`
- `WOMPI_REDIRECT_URL`
- `WOMPI_CHECKOUT_BASE` (por defecto `https://checkout.wompi.sv/p/`)
- `WOMPI_CURRENCY` (por defecto `USD`)

Si usas otra región (p. ej. CO), ajusta `WOMPI_CHECKOUT_BASE` a `https://checkout.wompi.co/p/` y la moneda.
