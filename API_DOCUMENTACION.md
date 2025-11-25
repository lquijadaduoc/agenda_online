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
- `GET /availability/slots?serviceId&date=YYYY-MM-DD` devuelve slots libres considerando duración del servicio y turnos confirmados/pendientes; respeta `bookingAdvanceDays` de `professional_settings`.

## Turnos (privado)
- `GET /appointments` lista turnos del profesional.
- `POST /appointments` crea turno: serviceId, clientId?, startDateTime, endDateTime, notes?. Usa `defaultAppointmentStatus` de `professional_settings`.
- `GET /appointments/{id}` detalle.
- `PUT /appointments/{id}` actualiza status y/o notes.
- `DELETE /appointments/{id}` cancela (status `CANCELLED_BY_PROFESSIONAL`); respeta `cancellationPolicyHours`.
- `POST /appointments/cancel/{token}` cancela por token público (status `CANCELLED_BY_CLIENT`) respetando `cancellationPolicyHours`.

## Turno público
- `POST /public/professionals/{slug}/appointments` crea turno público directo: serviceId, clientName, clientEmail?, clientPhone?, notes?, startDateTime (end se calcula con duración del servicio). Devuelve 204 y solo si `allowPublicBooking` está activo.
- `POST /public/professionals/{slug}/appointment-requests` crea una solicitud pendiente: serviceId, clientName, clientEmail?, clientPhone?, requestedDateTime. Devuelve 204.

## Solicitudes de turno (privado)
- `GET /appointment-requests` lista solicitudes pendientes del profesional.
- `POST /appointment-requests/{id}/accept` confirma una solicitud y genera turno (usa duración del servicio y default status).
- `POST /appointment-requests/{id}/reject` rechaza solicitud (body opcional `{"reason":"..."}`).

## Admin
- `GET /admin/professionals` lista profesionales.
- `GET /admin/professionals/{id}` detalle.
- `PUT /admin/professionals/{id}/status` body: `{"active": true|false}` activa/desactiva usuario.
- `GET /admin/notifications` lista todas las notificaciones registradas.
- `GET /admin/stats` placeholder (por implementar).

## Notificaciones
- `GET /notifications` lista notificaciones del profesional autenticado.

## Notas
- Todas las rutas excepto `/health`, `/auth/*` y `/public/**` requieren JWT en `Authorization: Bearer <accessToken>`.
- Seguridad: `/admin/**` solo rol `ADMIN`; el resto de rutas autenticadas requieren `PROFESSIONAL` o `ADMIN`.
- Zona horaria: si el profesional no tiene timezone se usa UTC.
- Validaciones de solapamiento de turnos activas para estados pending/confirmed.
- Recordatorios: se programa un recordatorio de notificación (`APPOINTMENT_REMINDER`) a `reminderTimeBeforeAppointmentMinutes` si está configurado en `professional_settings`.
