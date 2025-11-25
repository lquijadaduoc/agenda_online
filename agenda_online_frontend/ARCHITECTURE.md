# Agenda Online Frontend — Arquitectura

Documento que resume la arquitectura completa del frontend (Vite + React + Firebase + React Query) para el proyecto "Agenda Online para Profesionales Independientes".

Contenido:
- Mapa de rutas
- Route Guards
- Flujo de autenticación completo
- Estado global y React Query keys
- Componentes reutilizables principales
- Organización del código (estructura de carpetas)
- Manejo de errores y redirecciones
- Variables de entorno y seguridad
- Testing, CI y despliegue

---

## 1. Mapa de rutas (React Router)

- Público:
  - `/` — Landing / Home
  - `/login` — Login (Email/Password + Google)
  - `/register` — Registro (opcional)
  - `/p/:slug` — Perfil público del profesional / reserva
  - `/cancel/:token` — Cancelación pública de cita por token

- Privado (panel profesional):
  - `/dashboard`
  - `/appointments` (y `/appointments/:id`)
  - `/calendar`
  - `/clients` (y `/clients/:id`)
  - `/services`
  - `/availability`
  - `/profile`
  - `/settings`

- Admin (opcional):
  - `/admin/login`
  - `/admin/professionals`
  - `/admin/professionals/:id`

Notas:
- Usar layouts distintos: `AuthLayout`, `MainLayout`, `AdminLayout`.
- Mantener `next` query param en redirecciones a login para volver al destino original.

## 2. Route Guards

- `ProtectedRoute`:
  - Requiere: usuario Firebase autenticado + JWT backend válido (o cookie `HttpOnly` valida).
  - Comportamiento: mostrar loader mientras se verifica; redirect a `/login?next=` si no autenticado.

- `AdminRoute`:
  - Requiere: todo lo anterior + rol `admin` en claims del JWT o verificación backend.
  - Comportamiento: mostrar 403 o redirect según política.

Edge-cases:
- Evitar flash de contenido: no renderizar hasta que `AuthContext.loading === false`.
- Reintentar refresh del token una sola vez por petición para evitar loops.

## 3. Flujo de autenticación (end-to-end)

1. Usuario inicia sesión en Firebase (email/password o Google).
2. Firebase emite `User` y cliente obtiene ID Token con `getIdToken()`.
3. Frontend llama `POST /auth/firebase` enviando el ID Token.
   - Backend valida con Firebase Admin y responde:
     - Opción A: `{ token: '<JWT_BACKEND>', user: {...} }` (frontend guarda token en memoria).
     - Opción B (recomendada): backend setea `Set-Cookie` con JWT `HttpOnly` (frontend no lo guarda).
4. Para llamadas API:
   - Si token en memoria → `Authorization: Bearer <JWT>`.
   - Si cookie `HttpOnly` → `apiClient.withCredentials = true` y backend maneja sesión.
5. Manejo de expiración:
   - On 401: obtener `getIdToken(true)` y re-executar intercambio contra `/auth/firebase`; reintentar la petición.
   - Si falla, forzar `logout` (Firebase signOut + limpiar cache queries) y redirect a login.

Consideraciones de seguridad:
- Evitar `localStorage` para JWT; preferir cookie `HttpOnly` o memoria.
- Si se usan cookies, backend debe habilitar CORS con `Access-Control-Allow-Credentials`.

## 4. Estado global y React Query keys

- `AuthContext` (shape recomendado):
  - `user` (Firebase user | null)
  - `backendToken` (string | null) — si no se usan cookies
  - `profile` / `professional` (object | null)
  - `roles` (array)
  - `loading`, `error`
  - Métodos: `loginEmailPassword`, `loginGoogle`, `getFirebaseIdToken`, `logout`, `refreshBackendToken`

- React Query keys centrales (`src/query/keys.js`):
  - `['auth','me']` o `['professional','me']`
  - `['appointments']`, `['appointments','list', filters]`, `['appointments','detail', id]`
  - `['clients']`, `['clients','detail', id]`
  - `['services']`, `['availability','blocks']`
  - `['public','professional', slug]`

Sincronización:
- On login → invalidate `['appointments','clients','services']`.
- On logout → `queryClient.clear()`.

## 5. Componentes reutilizables

- `MainLayout` — header + sidebar + outlet.
- `AuthLayout` — layout para login/register.
- `AdminLayout` — layout admin.
- `Sidebar` — items nav, responsive.
- `Header` — search, user menu, notifications.
- `ProtectedRoute` / `AdminRoute` — guards.
- `Modal` — confirm/ forms.
- `Table` — columns, data, pagination, sort.
- `Form` base — wrappers para `react-hook-form`.
- `Button`, `Input`, `Select`, `DatePicker`, `TimePicker`.
- `Toast/Notification` — central notification UI.

Cada componente debe ser lo más presentacional posible; lógica (fetching, state) en hooks o servicios.

## 6. Organización del código (propuesta)

- `src/`
  - `api/` — `apiClient.js`, `authService.js`, `appointmentsService.js`, `clientsService.js`, `servicesService.js`, `availabilityService.js`
  - `auth/` — `authManager.js` (coordinador token)
  - `context/` — `AuthContext.jsx`, `ToastContext.jsx`, `UiContext.jsx`
  - `hooks/` — `useAuth.js`, `useAppointments.js`, `useClients.js`, `useServices.js`, `useAvailability.js`
  - `pages/` — `public/`, `auth/`, `private/`, `admin/`
  - `components/` — `layout/`, `ui/`, `navigation/`
  - `router/` — `index.jsx`, `ProtectedRoute.jsx`, `AdminRoute.jsx`
  - `query/` — `queryClient.js`, `keys.js`
  - `firebase/` — `config.js`, `index.js`
  - `styles/`, `utils/`, `i18n/`, `assets/`, `tests/`

Convenciones:
- `camelCase` para funciones, `PascalCase` para componentes.
- Services exportan funciones CRUD; hooks usan React Query y services.

## 7. Manejo de errores y redirecciones

- 401: intentar refresh; si falla → logout + redirect `/login?next=`.
- 403: página 403 o toast + redirect.
- 404: Not Found page.
- 500+: mostrar error genérico y registrar en Sentry.

UX:
- Toaster para mensajes.
- Confirmaciones en operaciones destructivas.
- Loaders locales para acciones y loader global durante verificación de sesión.

## 8. Variables de entorno (Vite - prefijo `VITE_`)

- `VITE_API_URL`
- `VITE_FIREBASE_API_KEY`
- `VITE_FIREBASE_AUTH_DOMAIN`
- `VITE_FIREBASE_PROJECT_ID`
- `VITE_FIREBASE_STORAGE_BUCKET`
- `VITE_FIREBASE_MESSAGING_SENDER_ID`
- `VITE_FIREBASE_APP_ID`
- `VITE_FIREBASE_MEASUREMENT_ID` (opcional)
- `VITE_SENTRY_DSN` (opcional)

No almacenar secrets sensibles en el repo; usar `.env.local` y secrets manager en CI/CD.

## 9. Testing, CI y despliegue

- Tests: Jest + React Testing Library + msw para servicios; Cypress para E2E.
- CI: lint + tests + build en PRs.
- Despliegue: `vite build` → CDN/hosting (Vercel, Netlify) o servirse desde backend.
- Observability: Sentry para errores y metrics.

## 10. Plan de implementación (próxima etapa)

1. Implementar UI de Auth (Login/Register) con `react-hook-form` y conectar `AuthContext`.
2. Implementar `ProtectedRoute` y `AdminRoute` y providers en `src/main.jsx`.
3. Construir servicios por dominio y hooks React Query.
4. Implementar pantallas principales del panel (Dashboard, Appointments, Calendar, Clients).
5. Tests unitarios y de integración; añadir CI.

---

Este documento es la guía para la implementación. Si quieres, en la próxima acción puedo:

- A) Crear los archivos placeholder para cada carpeta (`pages/auth/Login.jsx`, `components/layout/MainLayout.jsx`, etc.) sin UI completo.
- B) Implementar la UI de `Login` completa (form, Firebase login, exchange y redirect).

Dime cuál prefieres y lo hago.
