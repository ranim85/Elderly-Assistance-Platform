# API reference — Elderly Assistance Platform

Base URL (local): `http://localhost:8080`

Send **`Authorization: Bearer <token>`** on protected routes. Exceptions: **`POST /api/v1/auth/authenticate`** and **`POST /api/v1/auth/register`**.

**Versioning:** Auth is under **`/api/v1/auth`**. Other REST resources use **`/api/...`** (no `v1`). The Angular app uses `src/proxy.conf.json` to forward **`/api`** to port **8080**.

---

## Authentication

| Method | Path | Auth |
|--------|------|------|
| `POST` | `/api/v1/auth/authenticate` | Public |
| `POST` | `/api/v1/auth/register` | Public |
| `WS` | `/ws/**` | Public (Token passed in Connect Headers via ChannelInterceptor) |

### Login response (`AuthResponse`)

Shape from `tn.beecoders.elderly.dto.AuthResponse` (Jackson camelCase). Example:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBoZWFsdGgub3JnIiwiaWF0IjoxNzE0MjAwMDAwLCJleHAiOjE3MTQyODY0MDB9.signature",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBoZWFsdGgub3JnIiwiaWF0IjoxNzE0MjAwMDAwLCJleHAiOjE3MTQyODY0MDB9.signature",
  "type": "Bearer",
  "email": "admin@health.org",
  "role": "ADMIN"
}
```

> The current implementation uses the same JWT string for `refreshToken` as a stub (`AuthService`).

---

## Roles and `SecurityConfig`

Authorities are **`ROLE_` +** enum name: `ROLE_ADMIN`, `ROLE_CAREGIVER`, `ROLE_ELDERLY`, `ROLE_FAMILY_MEMBER`.

| Rule | Configuration |
|------|----------------|
| Auth routes | `permitAll` for `/api/v1/auth/**` |
| Reports | `/api/reports/**` → `hasAnyRole("ADMIN", "CAREGIVER")` |
| Current user | `GET /api/users/me` → **authenticated** (any role) |
| User directory / admin CRUD | `GET /api/users`, `POST /api/users`, `DELETE /api/users/*` → **`hasRole("ADMIN")`** |
| Other `/api/**` | **Authenticated** only |

**Alert resolve** adds a second layer: `AlertPermissionService` (admin or **assigned caregiver** for that alert’s elderly person).

---

## Dashboard

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/dashboard/stats` | `DashboardStatsResponse`: `totalAssisted`, `activeCaregivers`, `urgentAlerts` (scoped admin vs caregiver). |

Example JSON:

```json
{
  "totalAssisted": 15,
  "activeCaregivers": 2,
  "urgentAlerts": 3
}
```

---

## Alerts

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/alerts` | Paginated alerts. Supports query params: `page`, `size`, `resolved` (boolean), `priority`, `from`, `to` (ISO 8601). Returns `AlertPageResponse` instead of a flat list. Scope: admin sees all, caregiver sees only their patients. |
| `GET` | `/api/alerts/recent` | Top 5 by `timestamp` descending (same scoping). |
| `POST` | `/api/alerts` | Create a new alert. Body: `AlertCreateRequest` (`elderlyPersonId`, `alertType`, `priority`, `description`). |
| `PUT` | `/api/alerts/{id}` | Update an existing alert. Body: `AlertUpdateRequest` (`alertType`, `description`, `priority`, `isResolved`). Admin or assigned caregiver only. |
| `PUT` | `/api/alerts/{id}/resolve` | Sets `isResolved` to `true`. **403** if not admin and not the assigned caregiver. |

### `alertType` values (enum `Alert.AlertType`)

`SOS`, `MEDICAL_EMERGENCY`, `FALL_DETECTED` (serialized as strings in JSON).

### Example `AlertDTO` (as produced by `AlertController`)

Seeded alerts use description **`Automated systematic alert triggered.`** — example:

```json
{
  "id": 2,
  "alertType": "MEDICAL_EMERGENCY",
  "priority": "HIGH",
  "description": "Automated systematic alert triggered.",
  "timestamp": "2026-04-12T09:15:30",
  "isResolved": false,
  "resolvedAt": null,
  "resolvedByEmail": null,
  "elderlyPerson": {
    "id": 2,
    "firstName": "Mary",
    "lastName": "Johnson"
  }
}
```

Nested type: **`ElderlySummaryDTO`** — `id`, `firstName`, `lastName`.

---

## Elderly persons

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/elderly-persons` | List: admin sees all; caregiver sees rows where `caregiver.email` matches principal. |

### Example `ElderlyPersonDTO` (with caregiver)

```json
{
  "id": 1,
  "firstName": "Robert",
  "lastName": "Smith",
  "dateOfBirth": "1945-03-12",
  "address": "742 Spring Ave, Suite 3",
  "medicalConditions": "Hypertension",
  "caregiver": {
    "id": 2,
    "firstName": "Sarah",
    "lastName": "Connor"
  }
}
```

### Example when no caregiver is assigned

```json
{
  "id": 99,
  "firstName": "Jane",
  "lastName": "Doe",
  "dateOfBirth": "1940-06-01",
  "address": "1 Oak Street",
  "medicalConditions": "None",
  "caregiver": null
}
```

The Angular template treats **`caregiver: null`** as “Unassigned”.  
Nested type: **`CaregiverSummaryDTO`** — `id`, `firstName`, `lastName`.

---

## Medications & Timeline

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/medications/elderly/{id}` | Listers les routines médicamenteuses avec infos de retard. |
| `POST` | `/api/medications` | Assigner un nouveau médicament planifié au patient. |
| `PUT` | `/api/medications/{id}/take` | Logguer une prise médicamenteuse par un soignant ("Taken"). |
| `GET` | `/api/timeline` | Polling polymorphique (Health, Alerts, Meds, Appts) fusionnés chronologiquement. |

---

## Geofencing & Location

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/location/settings/{elderlyId}`| Récupère la latitude/longitude d'isolation (Home) et son rayon en mètres. |
| `PUT` | `/api/location/settings` | Met à jour le calibrage d'origine DTO `ElderlySettingsDTO`. |
| `POST`| `/api/location/ping` | (Public) Endpoint simulé par une Smartwatch envoyant `LocationPingDTO`. Si distance > radius, déclenche `WANDERING_EMERGENCY`. |

---

## Users

| Method | Path | Auth |
|--------|------|------|
| `GET` | `/api/users/me` | Any **authenticated** user — current principal as `UserDTO`. |
| `GET` | `/api/users` | **`ADMIN` only** — list all users. |
| `POST` | `/api/users` | **`ADMIN` only** — body `RegisterRequest`. Duplicate email → **400**. Role is normalized (`trim` + uppercase); invalid role → **400** with a clear message. If `role` is **`ELDERLY`**, a matching **`ElderlyPerson`** row is created so they appear under **Elderly persons** (placeholder DOB/address until intake). **`FAMILY_MEMBER`** only creates a **user** account (no elderly row). |
| `PUT` | `/api/users/{id}` | **`ADMIN` only** — body `UserUpdateRequest`. Update existing user's information. |
| `DELETE` | `/api/users/{id}` | **`ADMIN` only** — remove user by id. |

---

## Reports & Export

| Method | Path | Auth |
|--------|------|------|
| `GET` | `/api/reports/summary` | `ADMIN` or `CAREGIVER` |
| `GET` | `/api/reports/elderly/{id}/export-pdf`| `ADMIN`, `CAREGIVER` or `FAMILY_MEMBER` (Returns `application/pdf` Blob) |

Response is a **map** (keys such as `totalElderly`, `unresolvedAlerts`, `status`, `message` — see `ReportController`).

---

## Error format (`ErrorResponse`)

Used by `GlobalExceptionHandler`. Null `validationErrors` are omitted (`@JsonInclude(NON_NULL)`).

### Example: forbidden alert resolution (HTTP 403)

```json
{
  "timestamp": "2026-04-12T14:30:00.123456789",
  "status": 403,
  "error": "Forbidden",
  "message": "Only an administrator or the assigned caregiver can resolve this alert",
  "path": "/api/alerts/5/resolve"
}
```

---

## Data flow

```text
MySQL → JPA repositories → Controllers → DTO records (JSON) → Angular HttpClient → components
```

Read-heavy alert/elderly/dashboard/report controller methods use **`@Transactional(readOnly = true)`** (see `AlertController`, `ElderlyPersonController`, `DashboardController`, `ReportController`).

---

## Demo data (`DatabaseSeeder`, profile `!test`)

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@health.org` | `admin123` |
| Caregiver | `sarah.caregiver@health.org` | `care123` |
| Caregiver | `john.caregiver@health.org` | `care123` |

Seeder creates elderly persons, health records, appointments, and alerts for UI testing.

---

## Troubleshooting: “Family Member” / `FAMILY_MEMBER` → 500 or generic error

The role string **`FAMILY_MEMBER`** is **13 characters** (longer than `ADMIN`, `ELDERLY`, or `CAREGIVER`). If the MySQL `users.role` column was created too short (for example `VARCHAR(10)`), inserts **fail** and the API used to respond with a generic **500**.

The JPA mapping now declares **`@Column(length = 32)`** on `User.role` so **`spring.jpa.hibernate.ddl-auto=update`** can widen the column on restart.

If Hibernate does not alter your table automatically, run manually (adjust database name as needed):

```sql
ALTER TABLE users MODIFY COLUMN role VARCHAR(32) NOT NULL;
```

If a **previous failed attempt** left a row with the same email, you will get **409** with *“This email is already registered.”* — remove the duplicate row or use another email.
