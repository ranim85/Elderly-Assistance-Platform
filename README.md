# Elderly Assistance Platform

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular 17](https://img.shields.io/badge/Angular-17+-DD0031.svg)](https://angular.io/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)
[![CI/CD](https://github.com/ranim85/Elderly-Assistance-Platform/actions/workflows/ci.yml/badge.svg)](https://github.com/ranim85/Elderly-Assistance-Platform/actions)

---

## Project overview

The **Elderly Assistance Platform** is a full-stack web application for coordinating elderly care: dashboards, alerts, elderly profiles, user management, and summary reports. The **Angular** frontend talks to a **Spring Boot** REST API secured with **JWT** and **role-based rules**. Data is stored in **MySQL** via **Spring Data JPA**.

Typical users in the model: **administrator**, **caregiver**, and (in the domain enum) **elderly** and **family member** for future or partial UI flows.

Developed as a capstone / internship project with [Bee Coders](https://www.beecoders.tn/).

**Full technical documentation** (API, architecture, diagrams, defense notes): see the **[`docs/`](docs/)** folder and its [index](docs/README.md).

---

## Features

| Area | Description |
|------|-------------|
| **Authentication** | Login/register; JWT stored client-side; `HttpInterceptor` attaches `Authorization: Bearer`. |
| **Dashboard** | KPIs (assisted count, caregivers, unresolved alerts) scoped by **admin** vs **caregiver**. |
| **Alerts** | List and “recent” alerts; **resolve** with server-side RBAC (**admin** or **assigned caregiver** only). |
| **Elderly persons** | List with nested **caregiver** summary in JSON (matches Angular table). |
| **Users** | **`GET /api/users/me`** for any authenticated role; **list / create / delete** require **`ADMIN`**. Creating a user with role **`ELDERLY`** also creates an **`ElderlyPerson`** so they show on **Elderly persons**; **`FAMILY_MEMBER`** is account-only (no elderly row). |
| **Reports** | Summary JSON for **admin** or **caregiver** (`SecurityConfig`). |
| **Demo data** | `DatabaseSeeder` runs when the Spring profile **`test` is not active**; wipes and re-seeds MySQL for repeatable demos. |

---

## Technology stack

| Layer | Technologies |
|-------|----------------|
| **Frontend** | Angular 17+ (standalone components), Angular Material, HTTP client + functional **JWT interceptor**, **route guards** (`authGuard`, `roleGuard`). |
| **Backend** | Java 17, Spring Boot 3, Spring Security 6, Spring Data JPA, Hibernate, **JWT** (jjwt), Lombok, **MapStruct** (used where mappers exist in the codebase). |
| **API docs** | Springdoc OpenAPI (Swagger UI) — see backend `pom.xml`; default UI path is usually `/swagger-ui/index.html` on port **8080**. |
| **Database** | MySQL 8 (`application.yml` default URL `jdbc:mysql://localhost:3306/elderly_assistance_db`). |

---

## Architecture (summary)

```text
Browser (Angular)
    → HTTP + Bearer JWT
    → Spring Security filter chain (JWT + `authorizeHttpRequests`)
    → REST controllers
    → Optional small services (e.g. alert permission checks)
    → JPA repositories
    → MySQL
```

- **DTOs** are immutable **Java records** returned as JSON. **Nested summaries** (`ElderlySummaryDTO`, `CaregiverSummaryDTO`) keep contracts explicit and aligned with the Angular templates.
- **Exceptions** are normalized through **`GlobalExceptionHandler`** → **`ErrorResponse`** (including **403** for forbidden alert resolution).
- **Read endpoints** for alerts, elderly persons, dashboard stats, and reports use **`@Transactional(readOnly = true)`**; alert **resolve** uses a read-write transaction.

More detail: **[docs/architecture.md](docs/architecture.md)** and **[docs/api-reference.md](docs/api-reference.md)**.

---

## API versioning note

- **Authentication** is under **`/api/v1/auth`** (e.g. `POST /api/v1/auth/authenticate`).
- **Business resources** use **`/api/...`** without `v1` (e.g. `/api/alerts`, `/api/dashboard/stats`).

The Angular dev server proxies **`/api`** to the backend. This split is intentional in the current code; unifying under a single version prefix is a possible future refactor (see [docs/architecture.md](docs/architecture.md)).

---

## How to run

### Prerequisites

- **Local:** Java 17, Node.js 18+, MySQL 8 on port **3306**, database **`elderly_assistance_db`** (or adjust `Backend-Projet-Elderly-Assistance-Platform/src/main/resources/application.yml`).
- **Docker:** Docker Compose — `docker-compose.yml` at the **repository root**.

### Backend

```bash
cd Backend-Projet-Elderly-Assistance-Platform
mvn clean install
mvn spring-boot:run
```

API base: `http://localhost:8080`

### Frontend

```bash
cd Frontend-Projet-Elderly-Assistance-Platform
npm install
npm run start
```

App: `http://localhost:4200`. Relative calls such as `/api/alerts` are proxied to **8080** via `src/proxy.conf.json`.

---

## Demo accounts (`DatabaseSeeder`)

Seeded when the app runs with a profile **other than** `test` (see `@Profile("!test")` on `DatabaseSeeder`).

| Role | Email | Password |
|------|-------|----------|
| **Admin** | `admin@health.org` | `admin123` |
| **Caregiver** | `sarah.caregiver@health.org` | `care123` |
| **Caregiver** | `john.caregiver@health.org` | `care123` |

After login, use **Dashboard**, **Alerts**, **Elderly persons**, **Users** (admin routes), and **Reports** as appropriate for the role.

---

## Documentation index

| Document | Purpose |
|----------|---------|
| [docs/README.md](docs/README.md) | Index of all `/docs` files |
| [docs/api-reference.md](docs/api-reference.md) | Endpoints, roles, **real JSON** examples, errors |
| [docs/architecture.md](docs/architecture.md) | Layers, transactions, RBAC, lazy loading, trade-offs |
| [docs/uml_diagrams.md](docs/uml_diagrams.md) | Mermaid diagrams |
| [docs/defense_preparation.md](docs/defense_preparation.md) | Oral defense Q&A |
| [docs/rapport_de_stage.md](docs/rapport_de_stage.md) | Internship report (French) |

---

## Author

**Ranim Rtimi** — ISSAT Sousse — Bee Coders (2024/2025).
