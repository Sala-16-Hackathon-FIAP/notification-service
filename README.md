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

### Prerequisites

- Docker and Docker Compose installed
- Java 21 and Maven installed
- auth-service running on port 8080 (needed to generate valid JWT tokens)

### 1. Start infrastructure

```bash
cd notification-service
docker-compose up -d
```

This starts:
- **PostgreSQL** on port `5437` (database `fiapx_notification`, user `fiapx`, password `fiapx123`)
- **RabbitMQ** on port `5672` (AMQP) and `15672` (Management UI — login: `fiapx` / `fiapx123`)

Verify both containers are healthy:

```bash
docker-compose ps
```

### 2. Run the application

**Option A — Maven (terminal):**

```bash
mvn spring-boot:run
```

**Option B — IDE (IntelliJ / Eclipse):**

Run the main class `NotificationServiceApplication` directly using the Run/Debug button in your IDE. No extra configuration is needed — all defaults in `application.yml` already point to `localhost`.

The application starts on **port 8085**. Flyway automatically runs the database migration on startup.

Verify the service is running:

```
http://localhost:8085/actuator/health
```

### 3. Obtain a JWT token (via auth-service)

The auth-service must be running on port `8080`. Authenticate to get a token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"useradmin@email.com","password":"Admin@12345"}' | jq -r '.bearerToken')

echo $TOKEN
```

Extract the `userId` (JWT subject) — you will need it for the RabbitMQ test payloads:

```bash
echo $TOKEN | cut -d'.' -f2 | tr '_-' '/+' | awk '{while(length%4)$0=$0"=";print}' | base64 -d | jq -r '.sub'
```

### 4. Test the API endpoint (empty list)

```bash
curl -s http://localhost:8085/api/v1/notifications \
  -H "Authorization: Bearer $TOKEN" | jq
```

Expected response: `[]` (no notifications yet).

You can also test via **Swagger UI** at `http://localhost:8085/swagger-ui.html`. Click **Authorize** and enter `Bearer <your-token>`.

### 5. Test without a token (should return 401/403)

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8085/api/v1/notifications
```

Expected: `401` or `403`.

## Local Testing with RabbitMQ Events

Notifications are created by consuming RabbitMQ events. You can publish test events using the **RabbitMQ Management UI** at `http://localhost:15672`.

Navigate to **Exchanges** → `fiapx.events` → **Publish message**.

### Test 1 — Processing completed event

- **Routing key:** `video.processing.completed`
- **Properties:** `content_type` = `application/json`
- **Payload** (replace `<USER_ID>` with the UUID from Step 3):

```json
{
  "jobId": "a1b2c3d4-1111-2222-3333-444455556666",
  "uploadId": "b2c3d4e5-5555-6666-7777-888899990000",
  "userId": "<USER_ID>",
  "userEmail": "useradmin@email.com",
  "filename": "meu-video.mp4",
  "resultS3Key": "processed/abc123/meu-video.zip",
  "status": "COMPLETED",
  "errorMessage": null,
  "timestamp": "2026-07-06T12:00:00"
}
```

**Expected console output:**

```
Notification: processing completed for uploadId=b2c3d4e5-..., user=...

[EMAIL SIMULATION]
To:      useradmin@email.com
Subject: Your video 'meu-video.mp4' has been processed successfully!
Body:    Good news! Your video 'meu-video.mp4' has been processed.
         The extracted frames are available at: processed/abc123/meu-video.zip

         FIAP-X Video Processing Platform
```

After publishing, call the API again to verify the notification was persisted:

```bash
curl -s http://localhost:8085/api/v1/notifications \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Test 2 — Processing failed event

- **Routing key:** `video.processing.failed`
- **Payload:**

```json
{
  "jobId": "f1a2b3c4-aaaa-bbbb-cccc-dddd11112222",
  "uploadId": "e5f6a7b8-3333-4444-5555-666677778888",
  "userId": "<USER_ID>",
  "userEmail": "useradmin@email.com",
  "filename": "video-erro.mp4",
  "resultS3Key": null,
  "status": "FAILED",
  "errorMessage": "Codec not supported",
  "timestamp": "2026-07-06T12:05:00"
}
```

**Expected console output:**

```
Notification: processing failed for uploadId=e5f6a7b8-...: Codec not supported

[EMAIL SIMULATION]
To:      useradmin@email.com
Subject: Video processing failed for 'video-erro.mp4'
```

### Test 3 — Email fallback (empty userEmail)

- **Routing key:** `video.processing.completed`
- **Payload:**

```json
{
  "jobId": "c9d8e7f6-1234-5678-9abc-def012345678",
  "uploadId": "d4e5f6a7-abcd-ef01-2345-678901234567",
  "userId": "<USER_ID>",
  "userEmail": "",
  "filename": "sem-email.mp4",
  "resultS3Key": "processed/xyz/sem-email.zip",
  "status": "COMPLETED",
  "errorMessage": null,
  "timestamp": "2026-07-06T12:10:00"
}
```

**Expected:** The `To:` field in the email simulation log should show `user-<USER_ID>@fiapx.local` (fallback when `userEmail` is empty).

### Verification — List all notifications

After all three tests, the API should return 3 notifications:

```bash
curl -s http://localhost:8085/api/v1/notifications \
  -H "Authorization: Bearer $TOKEN" | jq
```

You can also verify directly in the database:

```bash
docker exec -it fiapx-notification-postgres psql -U fiapx -d fiapx_notification \
  -c "SELECT id, email, subject, type, sent, created_at FROM notifications;"
```

## Automated Tests

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
