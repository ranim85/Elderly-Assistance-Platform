# Architecture — validation notes

This document matches the **current** codebase (not aspirational). Use it with [api-reference.md](api-reference.md) for demos and defense.

---

## 1. Layering

| Layer | Responsibility in this project |
|-------|----------------------------------|
| **Controllers** | HTTP mapping, role-scoped queries (`Authentication` + `ROLE_ADMIN`), entity → DTO mapping for alerts and elderly persons. |
| **Repositories** | Spring Data JPA interfaces (`AlertRepository`, `ElderlyPersonRepository`, etc.). |
| **Services / Cron** | **Selective:** `AlertPermissionService` centralizes **who may resolve an alert**. `MedicationSchedulerService` natively embeds a chronomatic CRON firing every 30 minutes. `LocationService` executes geospatial physics (Haversine format). |
| **RealTime Eng.** | `WebSocketConfig` integrates `spring-boot-starter-websocket` via STOMP broker, distributing immediate DTO streams onto `/user/queue/alerts`. JWT is checked via a ChannelInterceptor. |
| **Security** | `SecurityConfig` + `JwtAuthenticationFilter` + `User` implements `UserDetails` (`ROLE_*` authorities). |
| **Exception handling** | `GlobalExceptionHandler` → `ErrorResponse` record. Unhandled exceptions are **logged at ERROR** server-side; clients still get a generic **500** payload (no stack traces). |

This is **lean**, not over-engineered: nested DTOs, a resilient STOMP layer, and a small permission service add clarity without a heavy domain layer.

---

## 1b. User account vs elderly care record

- A **`User`** is a login account (`users` table) with a **`Role`** (`ADMIN`, `CAREGIVER`, `ELDERLY`, `FAMILY_MEMBER`).
- An **`ElderlyPerson`** is a care profile (`elderly_persons` table) shown on the **Elderly persons** screen; it is **not** the same row as `User`.
- When an account is created with role **`ELDERLY`** (admin UI or public register), **`UserAccountService`** also creates a matching **`ElderlyPerson`** with placeholder demographics so the person appears in the elderly list; a **caregiver** can be assigned later. **`FAMILY_MEMBER`** and other roles do **not** create an `ElderlyPerson` (by design).

---

## 2. DTOs and mapping

| DTO | Purpose |
|-----|---------|
| `AlertDTO` | Outbound alert; nests `ElderlySummaryDTO elderlyPerson` (`id`, `firstName`, `lastName`). |
| `ElderlyPersonDTO` | Outbound elderly row; nests `CaregiverSummaryDTO caregiver` or **`null`** if unassigned. |
| `ElderlySummaryDTO` / `CaregiverSummaryDTO` | Small, stable projections reused in parent DTOs. |

Mapping is **explicit** in controllers (`mapToDTO` private methods). That is easy to audit for defense and keeps JSON aligned with Angular bindings.

---

## 3. RBAC (two levels)

1. **`SecurityConfig`** — URL/method rules: public auth; **`GET /api/users/me`** → any authenticated user; **`GET /api/users`**, **`POST /api/users`**, **`DELETE /api/users/*`** → **`ADMIN`**; `/api/reports/**` → `ADMIN` or `CAREGIVER`; everything else under `/api/**` → **authenticated**.
2. **`AlertPermissionService`** — **Resolve alert:** only **`ROLE_ADMIN`** or the **user whose email equals** the elderly person’s assigned **caregiver** may resolve. Others receive **`AccessDeniedException`** → **403** via `GlobalExceptionHandler`.

---

## 4. Transactions

| Endpoint | Annotation | Rationale |
|----------|------------|-----------|
| `GET /api/alerts`, `GET /api/alerts/recent` | `@Transactional(readOnly = true)` | Single persistence context for lazy loads during mapping. |
| `GET /api/elderly-persons` | `@Transactional(readOnly = true)` | Same; touches lazy `caregiver` on `ElderlyPerson`. |
| `GET /api/dashboard/stats` | `@Transactional(readOnly = true)` | Read-only aggregate queries. |
| `GET /api/reports/summary` | `@Transactional(readOnly = true)` | Read-only report. |
| `PUT /api/alerts/{id}/resolve` | `@Transactional` | Authorization touches associations; then **write** (`save`). |

---

## 5. Lazy loading and `LazyInitializationException`

- `Alert.elderlyPerson` and `ElderlyPerson.caregiver` use **`FetchType.LAZY`**.
- `application.yml` does **not** set `spring.jpa.open-in-view: false`, so Spring Boot’s **default Open EntityManager In View** remains **enabled**: the persistence context typically spans the **HTTP request**, which works with controller-level mapping in dev.
- If you **disable OIV** in production, prefer **`JOIN FETCH`** repository methods or a **facade service** with `@Transactional` for each use case so mapping never runs outside a session.

---

## 6. Frontend ↔ backend alignment

- Angular calls **`/api/alerts`**, **`/api/timeline`**, **`/api/medications`**, **`/api/elderly-persons`**, **`/api/dashboard/stats`**, **`/api/v1/auth/authenticate`** — consistent with controllers and `SecurityConfig`.
- Live feeds are channeled on **`subscribe('/user/queue/alerts')`** in `WebsocketService`.
- **`Alert`** / templates expect **`elderlyPerson.firstName`** etc.; backend **`AlertDTO`** provides nested **`elderlyPerson`**.
- Elderly table expects **`caregiver.firstName`**; backend sends **`caregiver`** object or **`null`**.

---

## 7. API versioning

- **`/api/v1/auth`** vs **`/api/...`** resources is a **documented split**. Standardizing on `/api/v1/...` everywhere would be a **cross-cutting** change (controllers + Angular + proxy + docs).

---

## 8. Optional high-value follow-ups

Reasoning for future work (not required for the current demo): service layer extraction for growth, `@PreAuthorize` as a second line of defense alongside URL rules, unified `/api/v1` prefix, `JOIN FETCH` if `spring.jpa.open-in-view` is disabled, and business rules such as “prevent deleting the last admin user.”
