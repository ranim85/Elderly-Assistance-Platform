# UML Diagrams - Elderly Assistance Platform

This document outlines the architectural blueprints of the system, upgraded to align with the final Senior Implementation including Clean Architecture, Spring Security JWT paradigms, and Interceptor-driven security patterns.

## 1. Use Case Diagram

```mermaid
usecaseDiagram
    actor "Elderly" as elderly
    actor "Family Member" as family
    actor "Caregiver" as caregiver
    actor "Admin" as admin

    package "Elderly Assistance Platform" {
        usecase "Authenticate (JWT)" as UC1
        usecase "Manage Accounts (CRUD)" as UC2
        usecase "Trigger Emergency Alert" as UC3
        usecase "Acknowledge Alerts" as UC4
        usecase "Manage Medication Schedules" as UC5
        usecase "Monitor Daily Activities" as UC7
    }

    elderly --> UC1
    elderly --> UC3
    
    family --> UC1
    family --> UC4
    family --> UC5
    family --> UC7

    caregiver --> UC1
    caregiver --> UC4
    caregiver --> UC5
    caregiver --> UC7

    admin --> UC1
    admin --> UC2
    admin --> UC7
```

## 2. Entity-Relationship (ER) Diagram (Physical Mapping)

```mermaid
erDiagram
    USERS ||--o| PROFILES : has
    USERS ||--o{ ALERTS : triggers
    USERS ||--o{ MEDICATION_REMINDERS : manages

    USERS {
        bigint id PK
        varchar first_name
        varchar last_name
        varchar email "UNIQUE, NOT NULL"
        varchar password "BCRYPT HASH"
        varchar role "Enum: ADMIN, CAREGIVER, ELDERLY"
    }

    PROFILES {
        bigint id PK
        bigint user_id FK
        text medical_history
        varchar emergency_line
    }

    ALERTS {
        bigint id PK
        bigint user_id FK
        datetime created_at
        varchar status "PENDING, RESOLVED"
        point location_coordinates
    }

    MEDICATION_REMINDERS {
        bigint id PK
        bigint user_id FK
        varchar medication_name
        varchar dosage
        time schedule
    }
```

## 3. Sequence Diagram : Full Authentication & Route Guard Flow (AuthInterceptor)

This diagram outlines how the Angular Frontend correctly negotiates with the Spring Boot Backend using JSON Web Tokens.

```mermaid
sequenceDiagram
    actor User
    participant App as Angular (LoginComponent)
    participant Guard as Angular (AuthGuard)
    participant Interceptor as Angular (AuthInterceptor)
    participant API as Spring Boot (AuthController)
    participant JWT as Spring Boot (JwtService)
    participant DB as MySQL Database

    User->>App: Submits Email & Password
    App->>API: POST /api/v1/auth/authenticate {AuthRequest DTO}
    API->>DB: query by Email / Verify BCRYPT
    DB-->>API: User details
    API->>JWT: generateToken(User)
    JWT-->>API: returns Bearer Token
    API-->>App: 200 OK + {AuthResponse DTO}
    App->>App: authService.saveToken(token) (localStorage)

    User->>Guard: Attempts to access /dashboard
    Guard->>Guard: authService.isAuthenticated() == true
    Guard-->>User: Permits navigation to Dashboard UI

    User->>App: Requests Dashboard Data
    App->>Interceptor: GET /api/v1/dashboard/stats
    Interceptor->>Interceptor: Clone request & Inject 'Authorization: Bearer <token>'
    Interceptor->>API: Validated Request
    API-->>App: 200 OK (Data)
```

## 4. Component Diagram (Clean Architecture)

```mermaid
componentDiagram
    package "Frontend (Angular 17+)" {
        [AuthInterceptor] --> [AuthGuard] : protects state
        [AuthGuard] --> [UI Components]
    }

    package "Backend (Spring Boot 3)" {
        [GlobalExceptionHandler] -.-> [Controllers] : @RestControllerAdvice
        [Controllers] --> [MapStruct Mappers] : Convert to DTO
        [Controllers] --> [Services] : Delegate Logic
        [Services] --> [Repositories] : Pure JPA
    }

    [UI Components] --> [Controllers] : HTTP / REST JSON
    [Repositories] --> [MySQL container] : Hibernate JDBC
```

## 5. Deployment Diagram (Dockerized Infrastructure)

```mermaid
deploymentDiagram
    node "Docker Engine Host" {
        node "Nginx Server (Frontend Container)" {
            artifact "Angular 17 Build (Dist)"
        }
        
        node "Alpine JRE (Backend Container)" {
            artifact "Spring Boot JAR"
            artifact "Embedded Tomcat (Port 8080)"
        }
        
        node "MySQL 8.0 (Database Container)" {
            artifact "elderly_assistance_db"
            artifact "Volume: elderly-mysql-data"
        }
    }
    
    "Client Browser" --> "Nginx Server (Frontend Container)" : HTTP :80
    "Nginx Server (Frontend Container)" --> "Alpine JRE (Backend Container)" : REST API
    "Alpine JRE (Backend Container)" --> "MySQL 8.0 (Database Container)" : JDBC :3306 (with Healthchecks)
```
