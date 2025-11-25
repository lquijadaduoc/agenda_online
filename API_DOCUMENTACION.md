# Documentación rápida de API

## Autenticación
- `POST /auth/register` registra profesional (name, email, password, publicSlug, businessName). Devuelve accessToken/refreshToken.
- `POST /auth/login` email/password. Devuelve tokens.
- `POST /auth/forgot-password` body: `{"email": "..."}`. Devuelve 204 (si existe, crea token de reset).
- `POST /auth/reset-password` body: `{"token": "...", "newPassword": "..."}`. Devuelve 204.

## Perfil de profesional
- `GET /me` perfil del profesional autenticado.
- `PUT /me/profile` actualiza datos (name, businessName, phone, timezone, bio, address).
- `GET /professionals/{slug}/public` perfil público por slug.

## Servicios
- `GET /services` lista servicios activos del profesional.
- `POST /services` crea servicio: name, description?, durationMinutes, price?, active?.
- `PUT /services/{id}` actualiza servicio (mismos campos).
- `DELETE /services/{id}` desactiva servicio.

## Clientes
- `GET /clients` lista clientes del profesional.
- `POST /clients` crea cliente: name, email?, phone?, notes?.
- `GET /clients/{id}` detalle.
- `PUT /clients/{id}` actualiza.

## Disponibilidad
- `GET /availability/blocks` lista bloques.
- `POST /availability/blocks` crea bloque: weekday? (0-6) o specificDate, startTime, endTime, recurring (bool).
- `PUT /availability/blocks/{id}` actualiza bloque.
- `DELETE /availability/blocks/{id}` elimina.
- `GET /availability/slots?serviceId&date=YYYY-MM-DD` devuelve slots libres considerando duración del servicio y turnos confirmados/pendientes.

## Turnos (privado)
- `GET /appointments` lista turnos del profesional.
- `POST /appointments` crea turno: serviceId, clientId?, startDateTime, endDateTime, notes?.
- `GET /appointments/{id}` detalle.
- `PUT /appointments/{id}` actualiza status y/o notes.
- `DELETE /appointments/{id}` cancela (status `CANCELLED_BY_PROFESSIONAL`).
- `POST /appointments/cancel/{token}` cancela por token público (status `CANCELLED_BY_CLIENT`).

## Turno público
- `POST /public/professionals/{slug}/appointments` crea turno público: serviceId, clientName, clientEmail?, clientPhone?, notes?, startDateTime (end se calcula con duración del servicio). Devuelve 204.

## Admin
- `GET /admin/professionals` lista profesionales.
- `GET /admin/professionals/{id}` detalle.
- `PUT /admin/professionals/{id}/status` body: `{"active": true|false}` activa/desactiva usuario.
- `GET /admin/stats` placeholder (por implementar).

## Notas
- Todas las rutas excepto `/health`, `/auth/*` y `/public/**` requieren JWT en `Authorization: Bearer <accessToken>`.
- Zona horaria: si el profesional no tiene timezone se usa UTC.
- Validaciones de solapamiento de turnos activas para estados pending/confirmed.
