# notification-service

Listens for processing completion and failure events from RabbitMQ, persists a notification record, and simulates sending an email (logs to console). Exposes an endpoint for users to retrieve their notifications.

## Technology Stack

- **Java 21** + **Spring Boot 3.5.0**
- **Spring Security** with JWT (JJWT 0.12.6)
- **PostgreSQL 16** with **Flyway** migrations
- **RabbitMQ** via `rabbit-topic-lib` (choreographed saga)
- **SpringDoc OpenAPI** (Swagger UI)
- **JaCoCo** for code coverage (minimum 80%)
- **Hexagonal Architecture** (ports and adapters)

## Responsibility

- Consume `video.processing.completed` and `video.processing.failed` events
- Persist a `Notification` record per event
- Simulate email delivery by logging a formatted message to the console
- Expose `GET /api/v1/notifications` for users to list their notifications

## Architecture

Hexagonal (ports and adapters):

```
infrastructure/rest        -> HTTP layer (NotificationController)
application/port/input     -> NotificationUseCase interface
application/service        -> NotificationService (use-case implementation)
infrastructure/persistence -> Spring Data JPA + Flyway
infrastructure/messaging   -> RabbitMQ consumers (rabbit-topic-lib)
infrastructure/email       -> SimulatedEmailAdapter (logs to console)
infrastructure/security    -> JwtAuthFilter, SecurityConfig
```

## API Endpoints

All endpoints require `Authorization: Bearer <JWT>`.

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/notifications` | List all notifications for the authenticated user |

### List notifications

```bash
curl -s http://localhost:8085/api/v1/notifications \
  -H "Authorization: Bearer $TOKEN" | jq
```

Response:
```json
[
  {
    "id": "...",
    "userId": "...",
    "uploadId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "useradmin@email.com",
    "subject": "Your video 'video.mp4' has been processed successfully!",
    "message": "Good news! Your video 'video.mp4' has been processed...",
    "type": "PROCESSING_COMPLETED",
    "sent": true,
    "createdAt": "2025-05-28T10:00:42"
  }
]
```

### Swagger UI

http://localhost:8085/swagger-ui.html

## Email Simulation

No real email provider is integrated. Notifications are logged in this format:

```
[EMAIL SIMULATION]
To:      useradmin@email.com
Subject: Your video 'video.mp4' has been processed successfully!
Body:    Good news! Your video 'video.mp4' has been processed.
         The extracted frames are available at: processed/<jobId>/video.zip

         FIAP-X Video Processing Platform
```

When `userEmail` is absent from the event, the service falls back to `user-<userId>@fiapx.local`.

## Notification Types

| Type | Trigger | Subject pattern |
|---|---|---|
| `PROCESSING_COMPLETED` | `video.processing.completed` event | Your video '<filename>' has been processed successfully! |
| `PROCESSING_FAILED` | `video.processing.failed` event | Video processing failed for '<filename>' |

## RabbitMQ Events

**Exchange:** `fiapx.events` (topic)

| Direction | Routing key | Queue |
|---|---|---|
| **Consumes** | `video.processing.completed` | `notification.video.processing.completed` |
| **Consumes** | `video.processing.failed` | `notification.video.processing.failed` |

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5437/fiapx_notification` | JDBC connection URL |
| `DB_USERNAME` | `fiapx` | Database user |
| `DB_PASSWORD` | `fiapx123` | Database password |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `RABBITMQ_PORT` | `5672` | RabbitMQ AMQP port |
| `RABBITMQ_USER` | `fiapx` | RabbitMQ user |
| `RABBITMQ_PASS` | `fiapx123` | RabbitMQ password |
| `JWT_SECRET` | *(dev key)* | Must match auth-service |

## Running Locally

### 1. Start infrastructure

```bash
docker-compose up -d
```

This starts PostgreSQL (port 5437) and RabbitMQ (port 5672 / management 15672).

### 2. Run the application

Run from your IDE or with Maven:

```bash
mvn spring-boot:run
```

The application starts on **port 8085**. All defaults in `application.yml` point to `localhost`.

## Tests

```bash
mvn test
```

JaCoCo enforces **>= 80% instruction coverage**. Coverage report: `target/site/jacoco/index.html`.

## CI/CD

GitHub Actions workflow: build -> test -> SonarCloud -> GHCR push -> EKS deploy.

The `GITHUB_TOKEN` secret is required in CI to download `rabbit-topic-lib` from GitHub Packages. Docker image is **public** on GHCR.

## Database

- PostgreSQL 16, schema: `fiapx_notification`
- Migrations managed by Flyway (`src/main/resources/db/migration`)
