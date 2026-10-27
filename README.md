# FizjoBook

![CI](https://github.com/virusxar/PhysioBook/actions/workflows/ci.yml/badge.svg)

Booking and treatment-history system for a physiotherapy clinic. Patients book free slots online; physiotherapists manage their schedule, appointments and clinical notes.

Built as a portfolio project with **Java 25** and **Spring Boot 4**.

## Features

**Patient**
- registers and logs in (JWT)
- picks a physiotherapist and a day, sees free slots and books one
- sees own appointments and cancels upcoming ones
- sees own treatment history: episodes, completed visits and notes the physiotherapist shared

**Physiotherapist**
- has weekly working hours (set by the admin); free slots are generated from them
- sees appointments for a day or all of them
- confirms, completes, cancels or marks a no-show
- runs treatment episodes and writes clinical notes, each either private or visible to the patient

**Admin**
- sees all physiotherapists and creates new ones (account and profile in one step)
- edits each physiotherapist's weekly working hours, including breaks and visit length
- sees the list of patients

**Demo data**
- on the first start the app adds 3 physiotherapists with different schedules, 4 patients, upcoming appointments and a sample treatment history
- appointment dates are calculated from the day of the first start, so they are always in the future

## Tech stack

| Area | Technology |
|---|---|
| Backend | Java 25, Spring Boot 4 (Web MVC, Data JPA, Validation) |
| Security | Spring Security, OAuth2 Resource Server, JWT (HS256), BCrypt |
| Database | PostgreSQL 18, Flyway migrations, Hibernate 7 |
| Mapping | MapStruct, Lombok |
| Tests | JUnit 5, MockMvc, Testcontainers |
| Frontend | Vanilla JavaScript (ES modules), HTML, CSS, served by Spring Boot |
| CI | GitHub Actions |

## Highlights

- **No double bookings, enforced by the database.** Besides the check in the service, a PostgreSQL `EXCLUDE` constraint on `tstzrange(start_time, end_time)` rejects overlapping active appointments, even under concurrent requests.
- **Role-based access plus ownership checks.** URL rules in `SecurityConfig` decide which role may call an endpoint; `AccessGuard` in the services makes sure a patient only sees their own data and a physiotherapist only changes their own schedule.
- **Free slots computed from working hours** minus active appointments, in the clinic's time zone (`Europe/Warsaw`), with daylight-saving changes handled.
- **Appointment state machine** in the entity (`BOOKED → CONFIRMED → COMPLETED`, plus `CANCELLED` and `NO_SHOW`); invalid transitions return `409 Conflict`.
- **Consistent errors** as RFC 9457 `ProblemDetail` responses, including field-level validation messages.
- **Schema under version control** with Flyway; Hibernate only validates it (`ddl-auto=validate`).
- **Integration tests on a real PostgreSQL** started by Testcontainers, run on every push by GitHub Actions.

## Getting started

### Requirements

- Java 25
- PostgreSQL (local), with an empty database called `fizjobook`
- Docker (only for running the tests)

### Configuration

Settings live in `src/main/resources/application.properties`. Secrets can be overridden with environment variables:

| Variable | Purpose | Default |
|---|---|---|
| `DB_PASSWORD` | PostgreSQL password | `postgres` |
| `JWT_SECRET` | Base64 key for signing tokens (min. 32 bytes) | development key, **change it in production** |
| `ADMIN_EMAIL` | first admin account, created on startup | `admin@fizjobook.local` |
| `ADMIN_PASSWORD` | password of that account | `admin123!` |
| `DEMO_DATA` | add sample physiotherapists, patients and appointments on the first start | `true` |

### Run

```bash
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd spring-boot:run`, or run `PhysioBookApplication` from IntelliJ.

Open http://localhost:8080. On the first start the app fills the empty database with demo data, so you can log in right away (password for all demo accounts: `Demo1234`):

| Role | E-mail |
|---|---|
| Admin | `admin@fizjobook.local` (password `admin123!`) |
| Physiotherapist | `katarzyna.mazur@fizjobook.demo` |
| Physiotherapist | `michal.kaczmarek@fizjobook.demo` |
| Physiotherapist | `agnieszka.zajac@fizjobook.demo` |
| Patient (with treatment history) | `magdalena.wojcik@fizjobook.demo` |
| Patient | `piotr.lewandowski@fizjobook.demo` |
| Patient | `krzysztof.kaminski@fizjobook.demo` |
| Patient | `joanna.dabrowska@fizjobook.demo` |

Demo data is added only once (the app skips it when the demo accounts already exist) and can be turned off with `DEMO_DATA=false`. The admin account is created only when no admin exists yet, so changing `ADMIN_PASSWORD` later does not change an existing admin. New patients can also register themselves on the login page.

### Tests

```bash
./mvnw verify
```

Docker must be running: Testcontainers starts a throwaway `postgres:18-alpine` container, Flyway applies all migrations and the tests call the API through MockMvc with real JWT tokens. Demo data is turned off in tests, and the admin login is read from `app.admin.*`, so the tests follow whatever credentials are configured.

## API overview

All endpoints are under `/api`. Errors use `ProblemDetail`.

| Method | Endpoint | Who |
|---|---|---|
| POST | `/auth/register`, `/auth/login` | public |
| GET | `/auth/me` | logged in |
| GET | `/physiotherapists`, `/physiotherapists/{id}/availability`, `/physiotherapists/{id}/slots?date=` | public |
| POST | `/physiotherapists` | admin |
| PUT | `/physiotherapists/{id}/availability` | physiotherapist (own), admin |
| GET | `/physiotherapists/{id}/appointments[?date=]` | physiotherapist (own), admin |
| POST | `/appointments` | patient (for self), staff |
| PATCH | `/appointments/{id}/confirm`, `/complete`, `/no-show` | staff |
| PATCH | `/appointments/{id}/cancel` | patient (own), staff |
| GET | `/patients`, `/patients/{id}` | staff |
| GET | `/patients/{id}/appointments`, `/patients/{id}/history` | patient (own), staff |
| POST | `/patients/{id}/episodes`, `/patients/{id}/notes` | physiotherapist |

