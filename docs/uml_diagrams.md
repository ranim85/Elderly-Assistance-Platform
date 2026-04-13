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

    family --> UC1
    family --> UC4
    family --> UC5
    family --> UC7

    admin --> UC1
    admin --> UC2
    admin --> UC7
```

## 2. Entity-Relationship (ER) Diagram (aligned with JPA entities)

This reflects the **current** persistence model in the Spring Boot module including the HealthTech enhancements.

```mermaid
erDiagram
    USERS ||--o{ ELDERLY_PERSONS : "caregiver (ManyToOne)"
    ELDERLY_PERSONS ||--o{ USERS : "linked_elderly_person (Family)"
    ELDERLY_PERSONS ||--o{ ALERTS : "subject"
    USERS ||--o{ ALERTS : "resolved_by"
    ELDERLY_PERSONS ||--o{ HEALTH_RECORDS : has
    ELDERLY_PERSONS ||--o{ APPOINTMENTS : has
    ELDERLY_PERSONS ||--o{ MEDICATIONS : has
    ELDERLY_PERSONS ||--o| ELDERLY_SETTINGS : "geofence tracker"

    USERS {
        bigint id PK
        varchar first_name
        varchar last_name
        varchar email UK "UNIQUE, NOT NULL"
        varchar password "BCrypt"
        varchar role "ADMIN, CAREGIVER, ELDERLY, FAMILY_MEMBER"
        bigint linked_elderly_person_id FK "nullable -> elderly_persons.id"
    }

    ELDERLY_PERSONS {
        bigint id PK
        varchar first_name
        varchar last_name
        date date_of_birth
        varchar address
        varchar medical_conditions
        bigint caregiver_id FK "nullable -> users.id"
    }

    ALERTS {
        bigint id PK
        bigint elderly_id FK
        varchar alert_type "SOS, MEDICAL_EMERGENCY, FALL_DETECTED, WANDERING_EMERGENCY"
        varchar priority "LOW, MEDIUM, HIGH, URGENT"
        varchar description
        datetime timestamp
        boolean is_resolved
        datetime resolved_at
        bigint resolved_by FK "nullable -> users.id"
    }

    HEALTH_RECORDS {
        bigint id PK
        bigint elderly_id FK
        varchar blood_pressure
        int heart_rate
        double blood_sugar
        datetime recorded_at
    }

    APPOINTMENTS {
        bigint id PK
        bigint elderly_id FK
        varchar doctor_name
        varchar purpose
        datetime appointment_date
        varchar status "SCHEDULED, COMPLETED, CANCELLED"
    }

    MEDICATIONS {
        bigint id PK
        bigint elderly_id FK
        varchar name
        varchar dosage
        datetime scheduled_time
        boolean is_taken
        datetime time_taken
    }

    ELDERLY_SETTINGS {
        bigint id PK
        bigint elderly_id FK "UNIQUE"
        double home_latitude
        double home_longitude
        double safe_zone_radius
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
    App->>Interceptor: GET /api/dashboard/stats
    Interceptor->>Interceptor: Clone request & Inject 'Authorization: Bearer <token>'
    Interceptor->>API: Validated Request
    API-->>App: 200 OK (Data)
```

## 4. Component Diagram (HealthTech layering)

```mermaid
componentDiagram
    package "Frontend (Angular 17+)" {
        [AuthInterceptor] --> [AuthGuard] : protects state
        [AuthGuard] --> [UI Components]
        [WebsocketService] --> [UI Components] : Provides live StompJS feed
    }

    package "Backend (Spring Boot 3)" {
        [GlobalExceptionHandler] -.-> [Controllers] : @RestControllerAdvice
        [ChannelInterceptor] --> [WebSocket Broker] : Validates JWT for STOMP
        [WebSocket Broker] --> [WebsocketService] : Push Private Alerts
        [Controllers] --> [DTO records] : map entity to JSON-safe DTO
        [Controllers] --> [Domain services] : LocationService, PdfExportService
        [Domain services] --> [Repositories]
        [Controllers] --> [Repositories] : read queries where no service yet
        [MedicationScheduler] --> [Repositories] : Scans DB every 30min
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
