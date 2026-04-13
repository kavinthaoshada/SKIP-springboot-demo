# SkipsShop — Spring Boot E-Commerce Platform

> A full-stack, production-grade e-commerce web application built with Spring Boot.  
> Designed as an educational demonstration of real-world enterprise Spring Boot development.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Architecture & Design Patterns](#architecture--design-patterns)
- [Security](#security)
- [Database Design](#database-design)
- [API Design](#api-design)
- [Best Practices Implemented](#best-practices-implemented)
- [Real-World Problems & Solutions](#real-world-problems--solutions)
- [Alternative Approaches](#alternative-approaches)
- [Key Spring Boot Concepts](#key-spring-boot-concepts)
- [Getting Started](#getting-started)
- [Configuration](#configuration)

---

## Project Overview

SkipShop supports three distinct user roles — **Buyer**, **Seller**, and **Admin** — each with their own dedicated interface and capabilities.

The platform covers the complete e-commerce lifecycle:

- User registration and authentication
- Product listing and discovery
- Order placement and tracking
- Inventory management
- Administrative oversight

All features are backed by a relational **MySQL** database and secured with **JWT-based authentication**.

Every architectural decision reflects patterns used in real-world production systems. The goal was not just to build something that works, but something that works **the right way**.

---

## Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| **Backend** | Spring Boot 3.4.x | Application framework |
| **Database** | MySQL 8 | Relational data store |
| **ORM** | JPA / Hibernate | Object-relational mapping |
| **Security** | Spring Security + JWT | Authentication & authorization |
| **Frontend** | Thymeleaf | Server-side templating |
| **Build Tool** | Maven | Dependency management & build lifecycle |
| **Utilities** | Lombok | Boilerplate elimination via annotation processing |

---

## Project Structure

```
src/main/java/com/ozzz/skip/demo/
│
├── model/              # JPA entity classes (database tables)
├── repository/         # Data access layer (Spring Data JPA)
├── dto/
│   ├── request/        # Incoming API payloads with validation
│   └── response/       # Outgoing API contracts
├── service/
│   └── impl/           # Business logic (interface + implementation)
├── controller/
│   └── web/            # MVC controllers for Thymeleaf pages
├── security/           # JWT utils, filters, and Spring Security config
├── exception/          # Custom exception classes
└── config/             # App configuration, data seeding, static resources
```

### Package Explanations

#### `model` — Entities

JPA entity classes that map directly to database tables. Core entities:

- **`User`** — Authentication credentials, contact info, and role (`BUYER`, `SELLER`, `ADMIN`). Role is stored as a string enum for human-readable database records.
- **`Product`** — Listed by a seller. Supports soft deletion via a `DELETED` status to preserve historical order integrity.
- **`Category`** — Self-referencing hierarchy (e.g. `Electronics > Smartphones > iPhones`).
- **`Order`** — A purchase transaction belonging to a buyer, containing a list of `OrderItem`s.
- **`OrderItem`** — Line-item bridge between `Order` and `Product`. Stores `unitPrice` **at the time of purchase** — not the current price — ensuring receipt accuracy.

#### `repository` — Data Access Layer

Interfaces extending `JpaRepository`. Spring Data JPA generates all SQL at runtime.

- **Method name queries** — `findByUsername()`, `existsByEmail()` — Spring interprets these automatically.
- **JPQL queries** — `@Query` annotation for joins, aggregations, and complex conditions. Database-agnostic.
- **Pagination** — `Pageable` parameter automatically generates `LIMIT`/`OFFSET` SQL and wraps results in `Page<T>`.

#### `dto` — Data Transfer Objects

DTOs create a deliberate boundary between internal entities and external API contracts.

- **Request DTOs** — Annotated with `@NotBlank`, `@Email`, `@Min`, `@Size`. Validated automatically before service code runs. Invalid input returns a `400 Bad Request` with field-level errors.
- **Response DTOs** — No response DTO contains a `password` field. This is an architectural guarantee that password hashes can never leak through the API.
- **`ApiResponse<T>`** — Wraps every API response in a consistent envelope: `success`, `message`, `data`, `timestamp`.
- **`PageResponse<T>`** — Wraps paginated results with `page`, `totalElements`, `totalPages` metadata.

#### `service` — Business Logic

All business logic lives here. Controllers call services; services call repositories.

- Interface-plus-implementation pattern (e.g. `ProductService` + `ProductServiceImpl`) enables dependency injection by abstraction, trivial unit testing with mocks, and swappable implementations.
- `@Transactional` ensures atomicity — all operations in a method succeed together or are fully rolled back.
- `@RequiredArgsConstructor` (Lombok) generates constructor injection, the recommended Spring injection method.

#### `controller` — HTTP Entry Points

Controllers handle routing, call services, and return responses. **No business logic in controllers.**

- `@RestController` — Returns JSON responses for the REST API.
- `@Controller` (`controller/web`) — Returns Thymeleaf template names for HTML pages.
- `@PathVariable`, `@RequestParam`, `@RequestBody`, `@AuthenticationPrincipal` — standard Spring MVC annotations.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) — Intercepts all unhandled exceptions and converts them to clean `ApiResponse` objects with correct HTTP status codes.

#### `security` — Cross-Cutting Security

- **`JwtUtils`** — Generates, validates, and parses JSON Web Tokens. Secret key injected via `@Value`, never hardcoded.
- **`UserDetailsServiceImpl`** — Bridges the `User` entity with Spring Security's `UserDetails` system via `loadUserByUsername()`.
- **`JwtAuthenticationFilter`** — Extends `OncePerRequestFilter`. Extracts and validates JWT on every request, then stores authentication in `SecurityContextHolder`.
- **`SecurityConfig`** — Configures public vs protected endpoints, role-based rules, CSRF strategy, and hybrid session/JWT support.

#### `exception` — Custom Exceptions

| Exception | HTTP Status |
|---|---|
| `ResourceNotFoundException` | 404 Not Found |
| `BusinessException` | 400 Bad Request |
| `UnauthorizedException` | 403 Forbidden |

No scattered `try-catch` blocks. Throw the right exception; the global handler handles the rest.

#### `config` — Configuration

- **`SecurityConfig`** — Spring Security configuration.
- **`WebMvcConfig`** — Registers static resource locations for uploaded product images.
- **`DataSeeder`** — Implements `ApplicationRunner` to populate the database with realistic demo data on first startup.

---

## Architecture & Design Patterns

### Layered Architecture (N-Tier)

```
HTTP Request
     │
     ▼
┌──────────────┐
│  Controller  │  ← Routes requests, calls services, returns responses
└──────┬───────┘
       │
       ▼
┌──────────────┐
│   Service    │  ← All business logic, transactions
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Repository  │  ← Data access only
└──────┬───────┘
       │
       ▼
┌──────────────┐
│    Model     │  ← JPA entities / database tables
└──────────────┘
```

Each layer communicates **only with the layer directly below it**. No layer skips another.

### Design Patterns Used

| Pattern | Where Used | Why |
|---|---|---|
| **Repository Pattern** | `repository` package | Abstracts data access behind an interface |
| **DTO Pattern** | `dto` package | Controls API contract, prevents data leakage |
| **Builder Pattern** | Lombok `@Builder` on entities/DTOs | Readable, safe object construction |
| **Strategy Pattern** | `CustomAuthenticationSuccessHandler` | Role-based redirect logic decoupled from auth |
| **Template Method Pattern** | `JwtAuthenticationFilter` | Parent class defines filter structure; subclass fills in JWT logic |

---

## Security

### JWT Authentication Flow (REST API)

```
Client Login Request
        │
        ▼
AuthenticationManager
        │
        ▼
DaoAuthenticationProvider  →  UserDetailsServiceImpl  →  Database
        │
  (BCrypt compare)
        │
        ▼
JwtUtils.generateToken()
        │
        ▼
Return JWT to Client

─────────────────────────────────────────

Subsequent Request (Bearer <token>)
        │
        ▼
JwtAuthenticationFilter
        │
  (validate signature & expiry)
        │
        ▼
SecurityContextHolder (auth stored for request)
```

### Form Login Flow (Thymeleaf Frontend)

Thymeleaf form submission → `UsernamePasswordAuthenticationFilter` → `DaoAuthenticationProvider` → `CustomAuthenticationSuccessHandler` (role-based redirect) → Session cookie issued.

### CSRF Strategy

| Surface | CSRF Protection | Reason |
|---|---|---|
| Thymeleaf forms | **Enabled** | Session cookies are vulnerable to CSRF |
| REST API (`/api/**`) | **Disabled** | Header-based tokens are not accessible cross-origin |

### Password Security

All passwords are hashed with **BCrypt** before storage. BCrypt is adaptive — its work factor increases with hardware improvements, maintaining brute-force resistance over time. Passwords are never stored, logged, or transmitted in plain text.

### Role-Based Access Control

Access is enforced at **two levels**:

1. **URL level** — `SecurityConfig` restricts entire URL patterns to specific roles.
2. **Method level** — `@PreAuthorize` on controller methods provides a second line of defense.

---

## Database Design

### Key Decisions

**Why `users` instead of `user`?**  
`USER` is a reserved SQL keyword. Naming the table `users` avoids quoting requirements and cross-database compatibility issues.

**Why `BigDecimal` for money?**  
`float` and `double` cannot exactly represent decimal values like `0.1` in binary. Rounding errors in financial calculations cause incorrect billing and reconciliation failures. `BigDecimal` uses arbitrary-precision decimal arithmetic. All monetary values are stored as `DECIMAL(10,2)`.

**Why `EnumType.STRING` for enum storage?**  
Integer ordinal storage is compact but opaque (`role = 2` is meaningless). String storage is human-readable and immune to ordering bugs — if a new enum value is inserted in the middle of the declaration, string-stored enums are unaffected. Integer ordinals would shift and corrupt existing data.

**Self-Referencing Category Hierarchy**  
`Category` has a `parent` field referencing another `Category`. This supports arbitrarily deep hierarchies (`Electronics > Smartphones > iPhones`) using a single table with a self-referential foreign key.

---

## API Design

### RESTful URL Conventions

URLs identify **resources**, not actions. The HTTP method expresses the action.

```
GET     /api/products          → List products (paginated)
POST    /api/products          → Create a product
GET     /api/products/{id}     → Get a product
PUT     /api/products/{id}     → Update a product
DELETE  /api/products/{id}     → Delete a product
```

Avoid procedural URLs like `/getProducts`, `/createProduct`, `/deleteProductById`.

### HTTP Status Codes

| Code | Meaning |
|---|---|
| `200 OK` | Request succeeded |
| `201 Created` | Resource created |
| `400 Bad Request` | Invalid input |
| `401 Unauthorized` | Authentication required |
| `403 Forbidden` | Authenticated but lacks permission |
| `404 Not Found` | Resource does not exist |
| `500 Internal Server Error` | Unexpected server error |

### Standard Response Envelope

Every API response — success or error — uses the same shape:

```json
{
  "success": true,
  "message": "Product retrieved successfully",
  "data": { ... },
  "timestamp": "2025-01-01T00:00:00Z"
}
```

Paginated responses include additional metadata:

```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "totalElements": 248,
    "totalPages": 25
  }
}
```

---

## Best Practices Implemented

| Practice | Description |
|---|---|
| **BCrypt Password Hashing** | No plain text passwords ever stored or logged |
| **Environment-Based Configuration** | Credentials in `application-local.properties`, excluded from version control via `.gitignore` |
| **Soft Delete** | Products marked `DELETED` rather than removed — preserves historical order integrity |
| **Price Snapshotting** | `OrderItem` stores `unitPrice` at purchase time — receipts always reflect what was paid |
| **Pagination by Default** | All list endpoints paginated from day one — no unbounded queries |
| **Input Validation at Entry Point** | Bean Validation on request DTOs, validated before service layer runs |
| **Role-Based Access Control** | Enforced at both URL level (SecurityConfig) and method level (@PreAuthorize) |
| **Consistent API Response Structure** | Every endpoint uses `ApiResponse<T>` — predictable, uniform contract |
| **REST/MVC Separation** | JSON controllers and HTML controllers never mixed |
| **Global Exception Handling** | All exceptions handled in one place — no scattered try-catch blocks |
| **SLF4J Parameterized Logging** | `log.info("User '{}' logged in", username)` — performant and injection-safe |
| **Transactional Boundaries** | `@Transactional` on all write operations — atomic, rollback-safe |

---

## Real-World Problems & Solutions

### Race Conditions in Inventory

**Problem:** Two buyers simultaneously attempt to purchase the last unit. Both read stock = 1, both decrement, both succeed — resulting in stock = -1.

**Solution:** `@Transactional` on `placeOrder()`. Stock check and decrement happen within a single database transaction, preventing concurrent inconsistency. For higher concurrency, optimistic locking (`@Version`) or pessimistic locking (`SELECT FOR UPDATE`) can be applied.

### Historical Data Integrity

**Problem:** Prices change. Products get deleted. Orders that reference live data become inaccurate over time.

**Solution:** Two patterns working together — **price snapshotting** (copy price into `OrderItem` at purchase time) and **soft deletion** (mark records `DELETED` rather than removing them).

### Credential Leakage via Source Control

**Problem:** Developers accidentally commit secrets to Git. Once committed, they exist in history forever.

**Solution:** Secrets live in `application-local.properties` (`.gitignore`d). The committed `application.properties` uses placeholder expressions like `${DB_USERNAME}` resolved at runtime.

### Unbounded Query Results

**Problem:** `SELECT * FROM products` on a million-record table exhausts memory and crashes clients.

**Solution:** Pagination enforced from day one using Spring Data JPA's `Pageable`. No endpoint can return more records than the configured page size cap.

### API Inconsistency

**Problem:** Different endpoints return different shapes. Clients must handle each case differently.

**Solution:** `ApiResponse<T>` wraps every single response. One shape, always. Client code is uniform.

### Security Configuration Conflicts

**Problem:** REST API needs stateless JWT (no sessions, no CSRF). Thymeleaf frontend needs sessions (form login, CSRF protection). These requirements conflict.

**Solution:** Hybrid approach — `SessionCreationPolicy.IF_REQUIRED`, CSRF disabled only for `/api/**` via `csrf.ignoringRequestMatchers("/api/**")`, JWT for API calls, form login for the frontend. Both coexist in one filter chain.

---

## Alternative Approaches

### Frontend: REST API + React/Vue Instead of Thymeleaf

A pure REST backend with a JavaScript frontend (React, Vue, Angular) provides a richer UI, independent deployment to a CDN, and the same backend serving web, mobile, and third-party clients simultaneously.  
**Trade-off:** Two codebases, two deployment pipelines, CORS configuration required, and token storage security must be managed client-side.

### Authentication: OAuth2 / OpenID Connect Instead of Custom JWT

Delegate authentication to an external identity provider (Google, Auth0, Keycloak). Never handle passwords yourself — eliminates an entire class of vulnerabilities. Users get SSO and MFA for free.  
**Trade-off:** External dependency, higher implementation complexity.

### Data Access: jOOQ or MyBatis Instead of JPA/Hibernate

jOOQ generates type-safe Java DSL from your schema. Every query is compile-time safe; schema changes become compilation errors. MyBatis sits between raw JDBC and a full ORM. Both provide complete SQL control.  
**Trade-off:** More verbose for simple operations; manual result-to-object mapping.

### Architecture: Microservices Instead of Monolith

Split by domain into independently deployable services, each with its own database, communicating via REST or a message broker (Kafka, RabbitMQ). Independent scaling, independent deployment, team autonomy.  
**Trade-off:** Distributed system complexity, dramatically higher operational overhead. A well-structured monolith outperforms premature microservices for most teams.

### Frontend Enhancement: HTMX Instead of Full-Page Thymeleaf

HTMX adds partial page updates via AJAX through HTML attribute declarations — interactivity without writing JavaScript. Server continues returning HTML fragments; no JSON required.  
**When to use:** Modern interactive UI without the overhead of React or Vue.

### Database: PostgreSQL Instead of MySQL

PostgreSQL offers native JSON column indexing, advanced full-text search, superior standards compliance, CTEs, window functions, and PostGIS for geospatial data.  
**Migration effort:** Change the driver dependency and JDBC URL. All JPA/JPQL queries remain unchanged.

---

## Key Spring Boot Concepts

| Concept | Description |
|---|---|
| **Auto-configuration** | Spring Boot configures the application automatically based on classpath dependencies — e.g. adding `spring-boot-starter-data-jpa` auto-configures `DataSource`, `EntityManagerFactory`, and `TransactionManager`. |
| **Starter Dependencies** | Curated, compatible dependency bundles — e.g. `spring-boot-starter-web` includes Spring MVC, embedded Tomcat, and Jackson. |
| **Dependency Injection** | Spring IoC container creates beans, resolves their dependencies, and injects them. Every `@Service`, `@Repository`, `@Controller`, and `@Component` is a managed bean. |
| **Application Context** | Central container holding all Spring beans. Created at startup by scanning for annotated classes. |
| **Profiles** | Different configurations per environment (`application-dev.properties`, `application-prod.properties`) activated via environment variable. |
| **ApplicationRunner** | Interface called after full context initialization but before the app starts serving requests — used by `DataSeeder` to populate demo data. |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8

### Clone & Build

```bash
git clone https://github.com/kavinthaoshada/SKIP-springboot-demo.git
cd skipshop
mvn clean install
```

### Database Setup

```sql
CREATE DATABASE skipshop;
CREATE USER 'skipuser'@'localhost' IDENTIFIED BY 'yourpassword';
GRANT ALL PRIVILEGES ON skipshop.* TO 'skipuser'@'localhost';
FLUSH PRIVILEGES;
```

### Run

```bash
mvn spring-boot:run
```

On first startup, `DataSeeder` populates the database with demo data automatically.

The application starts at `http://localhost:8080`.

---

## Configuration

Create `src/main/resources/application-local.properties` or .env file in root (this file is `.gitignore`d and never committed):

```properties
DB_USERNAME=skipuser
DB_PASSWORD=yourpassword
JWT_SECRET=your-very-long-secret-key-at-least-256-bits
```

The committed `application.properties` references these via placeholders:

```properties
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
app.jwt.secret=${JWT_SECRET}
```

> **Never commit secrets to version control.** In production, use a dedicated secrets manager (AWS Secrets Manager, HashiCorp Vault) or environment variables injected by your deployment platform.

---

## License

This project is for educational purposes. See `LICENSE` for details.
