# 🎳 Bowling Lane Reservation System

REST API for managing bowling lanes and time-slot reservations, with overlap
validation, concurrency-safe booking, versioned database schema and
interactive API documentation.

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)
![Flyway](https://img.shields.io/badge/Flyway-migrations-red.svg)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)
![OpenAPI](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D.svg)
![Tests](https://img.shields.io/badge/tests-JUnit5%20%7C%20Mockito%20%7C%20MockMvc-brightgreen.svg)

## About

Backend application that allows managing bowling lanes and creating
reservations for time slots. The core business rule: **two active
reservations of the same lane can never overlap in time** — enforced in the
service layer and protected against race conditions by pessimistic locking.

Features:
- Full CRUD for bowling lanes with unique lane-number validation
- Reservations with per-lane time-overlap validation, protected against
  double booking by pessimistic locking on the lane row
- Reservation lifecycle: `ACTIVE` → `CANCELLED` (client cancels) or
  `COMPLETED` (time window passes); only active reservations occupy a slot
- Consistent error model based on Problem Details (RFC 7807)
- Database schema versioned with Flyway migrations
- Interactive API documentation (Swagger UI / OpenAPI 3)
- One-command setup with Docker Compose (multi-stage Docker build)
- Unit tests (Mockito) and web-layer contract tests (MockMvc)

## Tech stack

| Category    | Technology                                              |
|-------------|---------------------------------------------------------|
| Backend     | Java 21, Spring Boot 3, Spring Web, Spring Data JPA     |
| Database    | PostgreSQL 16, Flyway (migrations), Hibernate           |
| Validation  | Jakarta Bean Validation                                 |
| API docs    | springdoc-openapi (Swagger UI, OpenAPI 3)               |
| Testing     | JUnit 5, Mockito, AssertJ, MockMvc (`@WebMvcTest`)      |
| Containers  | Docker, Docker Compose (multi-stage build)              |
| Tools       | Maven, Git, Lombok, IntelliJ IDEA                       |

## Architecture

Layered architecture with clear separation of concerns:

```
HTTP request
     ↓
Controller   REST endpoints, payload validation, HTTP status codes
     ↓
Service      business rules, locking, domain exceptions, transactions
     ↓
Repository   Spring Data JPA: derived queries, JPQL, lock modes
     ↓
PostgreSQL   schema managed by Flyway
```

```
src/main/java/pl/bowling/reservation/
├── controller/     LaneController, ReservationController
├── service/        LaneService, ReservationService
├── repository/     LaneRepository, ReservationRepository
├── entity/         Lane, Reservation
├── enums/          Status, ReservationStatus
├── dto/            request/response records with bean validation
├── exception/      domain exceptions + GlobalExceptionHandler
└── config/         OpenApiConfig
```

## Data model

```
+-------------+ 1        N +-------------------+
|    Lane     +------------+    Reservation    |
+-------------+            +-------------------+
| id          |            | id                |
| lane_number |            | lane_id      (FK) |
| status      |            | start_time        |
| version     |            | end_time          |
+-------------+            | reservation_status|
                           | customer_name     |
                           | customer_email    |
                           | version           |
                           +-------------------+
```

Reservation lifecycle:

```
               +-- client cancels -------> CANCELLED
               |
    ACTIVE ----+
               |
               +-- time window passes ---> COMPLETED
```

Only `ACTIVE` reservations occupy a time slot; `CANCELLED` and `COMPLETED`
ones release it. Both entities use `@Version` (optimistic locking). Enums
are persisted as strings (`@Enumerated(EnumType.STRING)`) for readable data
and safe debugging.

## API endpoints

### Lanes

| Method | Path             | Description               | Success | Errors        |
|--------|------------------|---------------------------|---------|---------------|
| GET    | /api/lanes       | List all lanes            | 200     |               |
| GET    | /api/lanes/{id}  | Get lane by id            | 200     | 404           |
| POST   | /api/lanes       | Create a lane             | 201     | 400, 409      |
| PUT    | /api/lanes/{id}  | Update lane number/status | 200     | 400, 404, 409 |
| DELETE | /api/lanes/{id}  | Delete a lane             | 204     | 404           |

### Reservations

| Method | Path                        | Description                   | Success | Errors        |
|--------|-----------------------------|-------------------------------|---------|---------------|
| POST   | /api/reservations           | Create a reservation          | 201     | 400, 404, 409 |
| GET    | /api/reservations/lane/{id} | Active reservations of a lane | 200     | 404           |
| DELETE | /api/reservations/{id}      | Cancel a reservation (soft)   | 204     | 404           |

### Error model

All domain errors are returned as Problem Details (RFC 7807):

```json
{
  "type": "about:blank",
  "title": "Time Slot Already Reserved",
  "status": 409,
  "detail": "Time slot already reserved for lane 1",
  "instance": "/api/reservations"
}
```

| Exception                        | HTTP status |
|----------------------------------|-------------|
| LaneDoesntExistException         | 404         |
| LaneAlreadyExistsException       | 409         |
| TimeSlotAlreadyReservedException | 409         |
| ReservationNotFoundException     | 404         |
| Bean validation failures         | 400         |

## Business rules

**Lane number uniqueness.** A lane number must be unique across all lanes.
On update, the lane may keep its own number — the check excludes the edited
entity (`existsByLaneNumberAndIdNot`).

**Reservation overlap.** Two time intervals overlap iff
`start1 < end2 AND end1 > start2`. The validation is implemented as a JPQL
query against active reservations of the given lane:

```jpql
SELECT r FROM Reservation r
WHERE r.lane.id = :laneId
  AND r.reservationStatus = :status
  AND r.startTime < :endTime
  AND r.endTime > :startTime
```

Only `ACTIVE` reservations block a slot — `CANCELLED` and `COMPLETED` ones
release it. Adjacent slots (e.g. 14:00–16:00 and 16:00–18:00) do not
conflict.

**Database-level guarantees (defense in depth):**
- `CHECK (start_time < end_time)` constraint on the reservation table
- composite index on `(lane_id, start_time, end_time)` for the overlap query

## Concurrency control

**Pessimistic locking on reservation creation.** Creating a reservation is
a check-then-act flow: first an overlap query, then an insert. Under
concurrent requests, two transactions could both observe the slot as free
and both insert — a race condition that `@Version` cannot catch, because
there is no shared row to conflict on yet. To eliminate double booking, the
service locks the lane row for the whole transaction:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT l FROM Lane l WHERE l.id = :id")
Optional<Lane> findByIdWithLock(@Param("id") Long id);
```

`createReservation` runs inside `@Transactional` and loads the lane via
`findByIdWithLock`, which issues `SELECT ... FOR UPDATE`. Concurrent
reservations of the same lane are serialized at this lock: the second
transaction waits for the first to commit, then its overlap query sees the
freshly inserted reservation and rejects the request with 409.

**Optimistic locking on updates.** Both entities carry a `@Version` column,
so concurrent edits of the same row fail fast instead of silently
overwriting each other.

The two mechanisms complement each other: pessimistic locking guards the
insert race (new rows, no shared version), optimistic locking guards
concurrent updates of existing rows.

## API documentation (Swagger UI)

The project ships with springdoc-openapi. Once the application is running:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

Every endpoint can be explored and executed directly from the browser,
including request schemas and error responses.

## Testing

```bash
mvn test
```

| Test class                | Layer | Tools            | Covers                                          |
|---------------------------|-------|------------------|-------------------------------------------------|
| LaneServiceTest           | unit  | Mockito, AssertJ | happy paths, 404/409 branches, `never()` verifications |
| ReservationServiceTest    | unit  | Mockito, AssertJ | overlap rejection, missing lane, invalid time range, cancellation |
| LaneControllerTest        | web   | MockMvc          | status codes, JSON payloads, validation errors  |
| ReservationControllerTest | web   | MockMvc          | 201/200/204/400/404/409 contract                |

Service tests verify both outcomes and side effects (e.g. that `save()` is
never called when a business rule rejects the operation). Unit and web tests
require no database — they run fully in-memory.

## Running locally

Prerequisites: Java 21, Maven, Docker with Compose.

### Option 1 — full stack in containers

```bash
docker compose up --build -d
```

PostgreSQL starts first (healthchecked), then the application is built
(multi-stage Dockerfile) and connects to it. Flyway migrates the schema on
startup. API: http://localhost:8080, Swagger UI: /swagger-ui.html.

### Option 2 — database in container, app from IDE

```bash
docker compose up -d db     # only the database
```

The compose service publishes PostgreSQL on `localhost:5432`, so the default
datasource configuration works unchanged. Then start the application either
from IntelliJ (Run configuration of the Spring Boot main class) or from
Maven:

```bash
mvn spring-boot:run
```

Tip: IntelliJ can manage the compose services directly from the **Services**
tool window (Docker node) — start/stop/logs without leaving the IDE.

### Useful commands

```bash
mvn test                    # run the test suite
docker compose logs -f app  # follow application logs
docker compose down -v      # stop everything and remove the volume
```


## Known limitations & roadmap

- **Completion:** `COMPLETED` is not assigned automatically yet.
  Planned: a scheduled job moving past reservations out of `ACTIVE`.
- No pagination or filtering on list endpoints (planned).
- No authentication/authorization (planned).

## Author

Jakub Świderski — linkedin.com/in/jakub-świderski-49b048283/ 
