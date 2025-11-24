Agenda online para profesionales independientes
===============================================

Objetivo: blueprint backend y base de datos para una SaaS de agenda/turnos para profesionales (psicologos, coaches, etc.) con despliegue en un VPS.

1) Modelo de datos (PostgreSQL sugerido)
----------------------------------------
- users: id PK; name; email UNIQUE NOT NULL; password_hash; role ENUM('professional','admin'); is_active BOOL; created_at; updated_at.
- professionals: id PK; user_id FK UNIQUE->users.id; public_slug UNIQUE NOT NULL; business_name; phone; timezone; bio; address; created_at; updated_at.
- professional_settings: id PK; professional_id FK UNIQUE->professionals.id; allow_public_booking BOOL; booking_advance_days INT; cancellation_policy_hours INT; default_appointment_status ENUM('pending','confirmed'); email_notification_enabled BOOL; reminder_time_before_appointment_minutes INT; created_at; updated_at.
- clients: id PK; professional_id FK->professionals.id; name NOT NULL; email; phone; notes; created_at; updated_at.
- services: id PK; professional_id FK->professionals.id; name; description; duration_minutes INT; price DECIMAL(10,2); is_active BOOL; created_at; updated_at.
- availability_blocks: id PK; professional_id FK; weekday SMALLINT NULL; specific_date DATE NULL; start_time TIME; end_time TIME; is_recurring BOOL; created_at; updated_at. CHECK((is_recurring AND weekday IS NOT NULL) OR (NOT is_recurring AND specific_date IS NOT NULL)).
- appointments: id PK; professional_id FK; client_id FK NULL; service_id FK; start_datetime TIMESTAMPTZ; end_datetime TIMESTAMPTZ; status ENUM('pending','confirmed','cancelled_by_professional','cancelled_by_client','completed'); notes; created_from_public_link BOOL; cancellation_token UNIQUE; created_at; updated_at.
- appointment_requests: id PK; professional_id FK; service_id FK; requested_datetime TIMESTAMPTZ; client_name; client_email; client_phone; status ENUM('pending','accepted','rejected','expired'); created_at; updated_at.
- notifications: id PK; professional_id FK; appointment_id FK NULL; channel ENUM('email','sms','whatsapp'); type TEXT; recipient TEXT; payload JSONB; sent_at TIMESTAMPTZ; status ENUM('sent','failed'); error_message TEXT; created_at TIMESTAMPTZ.
- audit_logs: id PK; user_id FK NULL; action TEXT; entity_type TEXT; entity_id BIGINT; metadata JSONB; created_at TIMESTAMPTZ.
- password_resets: id PK; user_id FK; token UNIQUE; expires_at TIMESTAMPTZ; used_at TIMESTAMPTZ NULL; created_at TIMESTAMPTZ.
- public_links (opcional): id PK; appointment_id FK UNIQUE; token UNIQUE; type ENUM('cancel','reschedule'); expires_at TIMESTAMPTZ; created_at TIMESTAMPTZ.

Relaciones clave:
- users 1:1 professionals (user_id UNIQUE).
- professionals 1:N services, clients, availability_blocks, appointments, appointment_requests, notifications; 1:1 professional_settings.
- services 1:N appointments, appointment_requests.
- clients 1:N appointments.
- appointments 1:N notifications; 1:1 public_links.
- users 1:N audit_logs, password_resets.

Indices recomendados:
- users: email UNIQUE; (role, is_active).
- professionals: public_slug UNIQUE; user_id UNIQUE; (business_name).
- professional_settings: professional_id UNIQUE.
- clients: (professional_id, email) UNIQUE NULLS NOT DISTINCT; (professional_id, name); (professional_id, phone).
- services: (professional_id, is_active); (professional_id, name).
- availability_blocks: professional_id; UNIQUE(professional_id, weekday, start_time, end_time) cuando is_recurring; UNIQUE(professional_id, specific_date, start_time, end_time) cuando no es recurrente.
- appointments: (professional_id, start_datetime); (professional_id, status, start_datetime DESC); (client_id, start_datetime); (service_id, start_datetime); cancellation_token UNIQUE; parcial status IN ('pending','confirmed').
- appointment_requests: (professional_id, status, requested_datetime); (professional_id, client_email).
- notifications: (professional_id, sent_at); (status, sent_at DESC); (appointment_id).
- audit_logs: (entity_type, entity_id); (user_id, created_at DESC).
- password_resets: (user_id, expires_at).

Restricciones y reglas:
- NOT NULL en claves y campos criticos (name, start/end, status).
- CHECK start_time < end_time en availability_blocks; start_datetime < end_datetime en appointments.
- FK con ON DELETE CASCADE para settings/clients/services/availability; appointments suele ser RESTRICT en professional_id y service_id, SET NULL o CASCADE opcional en client_id. Validar coherencia de owner (client/service pertenece al professional) en capa de servicio o con FKs compuestas.

2) Arquitectura backend
-----------------------
Stack sugerido: Node.js (NestJS/Express + Prisma/TypeORM) o Python (FastAPI + SQLAlchemy). DB PostgreSQL; Redis opcional para rate limit, cola y locks. Despliegue en VPS tras Nginx.

Capas:
- Rutas/controladores: definen endpoints, parsean input, aplican autorizacion inicial.
- Servicios (dominio): logica de negocio, reglas de disponibilidad, estados de cita, integracion con colas y notificaciones.
- Repositorios/ORM: acceso a datos, mapeo a SQL, transacciones.
- DTO/validadores: esquemas de entrada/salida (p.ej. Zod, Pydantic, class-validator).
- Jobs/colas: envio de notificaciones y recordatorios fuera del request.
- Config/env: carga y validacion de variables (DB, SMTP, JWT_SECRET, etc.).
- Logging y errores: logger estructurado JSON; middleware global de errores a HTTP.

3) Modulos funcionales
----------------------
- Auth: registro profesional (crea user+professional+settings); login email/password; JWT access corto + refresh rotado o sesiones con cookie HttpOnly; reset password con password_resets; rate limit login/reset; bloqueo tras N intentos; hash con Argon2id/bcrypt.
- Profesionales: CRUD perfil; settings CRUD; perfil publico por slug.
- Servicios: CRUD con is_active como soft delete.
- Disponibilidad: CRUD de availability_blocks; calculo de slots combinando bloques (recurrentes y puntuales), exclusiones por citas activas (pending/confirmed), booking_advance_days, timezone; validar overlaps.
- Clientes: CRUD acotado al professional; busqueda por name/email (ILIKE).
- Citas: crear manual y publica (slug) validando disponibilidad y reglas; estados: pending, confirmed, cancelled_by_*, completed; reprogramar validando solape; cancelar por professional o via cancellation_token/public_links; filtros por rango/estado/cliente/servicio.
- Notificaciones: encolar y enviar (SMTP hoy, SMS/WA futuro); registros en notifications; plantillas para confirmacion, recordatorio (settings.reminder_time_before_appointment_minutes), cancelacion.
- Admin: listado de profesionales, activar/desactivar user.is_active, estadisticas basicas (citas por estado, clientes, servicios activos).

4) Endpoints REST (sugeridos)
-----------------------------
- Auth: POST /auth/register; POST /auth/login; POST /auth/refresh; POST /auth/forgot-password; POST /auth/reset-password; POST /auth/logout.
- Perfil: GET /me; PUT /me/profile; GET /professionals/:slug/public.
- Settings: GET /settings; PUT /settings.
- Servicios: GET /services; POST /services; PUT /services/:id; DELETE /services/:id.
- Disponibilidad: GET /availability/blocks; POST /availability/blocks; PUT /availability/blocks/:id; DELETE /availability/blocks/:id; GET /availability/slots?service_id&date o rango.
- Clientes: GET /clients?name&email; POST /clients; GET /clients/:id; PUT /clients/:id.
- Citas: GET /appointments?from&to&status&client_id&service_id; POST /appointments; GET /appointments/:id; PUT /appointments/:id; DELETE /appointments/:id; POST /public/professionals/:slug/appointments; POST /appointments/cancel/:token; POST /appointments/reschedule/:token (si se habilita).
- Notificaciones (solo lectura/admin): GET /notifications?status&from&to.
- Admin: GET /admin/professionals; GET /admin/professionals/:id; PUT /admin/professionals/:id/status; GET /admin/stats.
- Health/Metrics: GET /health; GET /metrics (protegido).

5) Seguridad y NF
-----------------
- Validacion y sanitizacion de inputs; limitar tamaños de payload.
- SQL parametrizado (ORM); proteger contra inyeccion; cabeceras de seguridad; CORS restringido; TLS obligatorio.
- Autorizacion por rol: middleware admin/professional; scoping por professional_id en consultas.
- Rate limiting en Nginx y app (login, endpoints publicos).
- CSRF: si cookies, SameSite=Lax + token CSRF en mutaciones; si bearer no aplica.
- Logging sin datos sensibles; request-id para trazabilidad; audit_logs para acciones clave.
- Backups de DB; rotacion de logs; monitoreo de colas y jobs; migraciones versionadas.
- Escalabilidad inicial: indices propuestos; caching de slots en Redis si crece; posibilidad de particionar por professional_id a futuro.

6) Roadmap de implementacion
----------------------------
1. Bootstrapping: configurar repo, lint/format, health check, carga de env.
2. Auth: registro/login/refresh/reset + tests.
3. Profesionales + settings + perfil publico.
4. Servicios + clientes CRUD.
5. Disponibilidad + calculo de slots + tests de solapes.
6. Citas (manual/publica) + cancel/reschedule token + estados.
7. Notificaciones con cola + plantillas + recordatorios.
8. Admin y estadisticas; hardening (rate limit, CORS, headers); despliegue Nginx + PM2/Uvicorn.
