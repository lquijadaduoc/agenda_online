Agenda online para profesionales independientes
===============================================

Backend Spring Boot para gestionar agenda, servicios, clientes y turnos de profesionales. Expone una API REST con autenticación JWT y endpoints públicos para tomar turnos por slug.

Resumen rápido
--------------
- Stack: Java 17, Spring Boot 3.2, JPA/Hibernate, MySQL 8, Flyway, JWT.
- Seguridad: JWT Bearer; rutas públicas: `/health`, `/auth/*`, `/public/**`.
- Documentación de endpoints: `API_DOCUMENTACION.md`.
- Build: Maven Wrapper incluido (`./mvnw`).
- Despliegue local: docker-compose con app + MySQL.

Cómo correr con Docker
----------------------
1) Construir y levantar:
```
docker-compose up -d --build
```
2) App en `http://localhost:8080`, MySQL en `localhost:3307` (user/pass `agenda`).
3) Logs app:
```
docker-compose logs -f app
```
4) Bajar:
```
docker-compose down
```

Build local (sin Docker)
------------------------
Requiere JDK 17. El wrapper descarga Maven si hay red.
```
MAVEN_USER_HOME=.m2 ./mvnw clean -DskipTests package
```
El jar queda en `target/agenda-online-0.0.1-SNAPSHOT.jar`.

Variables relevantes (docker-compose usa valores por defecto)
-------------------------------------------------------------
- `DB_URL`: JDBC MySQL (`jdbc:mysql://mysql:3306/agenda_online?...`)
- `DB_USERNAME` / `DB_PASSWORD`
- `JWT_SECRET`: clave base64 para firmar tokens
- `SPRING_PROFILES_ACTIVE`: `prod` en contenedor

Flujo básico de uso
-------------------
1) Registrar profesional: `POST /auth/register` → guarda usuario, profesional y settings y devuelve JWT.
2) Autenticarse: `POST /auth/login` → access/refresh tokens.
3) Crear servicios: `POST /services`.
4) Definir disponibilidad: `POST /availability/blocks`.
5) Crear clientes: `POST /clients`.
6) Crear turnos internos: `POST /appointments`.
7) Turnos públicos por slug: `POST /public/professionals/{slug}/appointments`.
8) Cancelación por token: `POST /appointments/cancel/{token}`.

Documentación de la API
-----------------------
Ver `docs/api_documentacion.md` para detalle de cada endpoint, cuerpos y notas.

Notas de seguridad
------------------
- JWT en `Authorization: Bearer <token>` para todas las rutas protegidas.
- Validación de solapamientos de turnos para estados pending/confirmed.
- Timezone: si no se configura en el profesional, se usa UTC.

Estructura de proyecto (carpetas clave)
---------------------------------------
- `src/main/java/com/agendaonline/controller`: controladores REST.
- `src/main/java/com/agendaonline/service`: lógica de negocio.
- `src/main/java/com/agendaonline/dto`: DTOs de entrada/salida.
- `src/main/java/com/agendaonline/security`: JWT y usuario actual.
- `src/main/resources/db/migration`: migraciones Flyway.
- `docs/api_documentacion.md`: referencia de endpoints.
