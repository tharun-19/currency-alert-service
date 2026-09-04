# Currency Alert Service

A real-time currency exchange rate alert service that monitors FX pairs and triggers notifications when exchange rates breach user-defined thresholds. Built with Spring Boot, Redis caching, Kafka event streaming, and PostgreSQL persistence.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Client (Postman / API)                      │
└────────────────────────┬────────────────────────────────────────────┘
                         │
                         ▼
           ┌─────────────────────────────┐
           │  Spring Boot Application    │
           │  (JWT Auth + REST API)      │
           │  :8081                      │
           └─────┬───────────────────────┘
                 │
        ┌────────┼────────┐
        ▼        ▼        ▼
    ┌────────┐ ┌─────────┬──────────┐
    │ Redis │ │ Kafka   │ Postgres │
    │ Cache │ │ Topics  │ Database │
    │:6379  │ │:9092    │ :15432   │
    └────────┘ └─────────┴──────────┘
        ▲              ▲
        │              │
        └──────────────┘
        
   ┌─────────────────────────────────────────┐
   │   Scheduler (Every 60s)                 │
   │   - Fetch active alert pair rates       │
   │   - Publish to rate-fetched topic       │
   └─────────────────────────────────────────┘
        │
        ▼
   ┌─────────────────────────────────────────┐
   │   Alert Evaluator Consumer              │
   │   - Listen to rate-fetched              │
   │   - Evaluate threshold breaches         │
   │   - Publish alert-triggered events      │
   └─────────────────────────────────────────┘
```

## Setup

### Prerequisites
- Docker & Docker Compose
- Java 25+
- Gradle 8+

### Step 1: Start Infrastructure

```bash
docker compose up -d
```

This starts:
- PostgreSQL (port 15432)
- Redis (port 6379)
- Kafka (port 9092)

### Step 2: Initialize Database Schema

```bash
psql -h localhost -p 15432 -U postgres -d currency_alert_db << 'EOF'
CREATE TABLE IF NOT EXISTS alert_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    currency_pair VARCHAR(10) NOT NULL,
    user_id UUID NOT NULL,
    direction VARCHAR(10) NOT NULL CHECK (direction IN ('ABOVE', 'BELOW')),
    threshold NUMERIC(15, 4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    triggered_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rate_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    base_currency VARCHAR(3) NOT NULL,
    target_currency VARCHAR(3) NOT NULL,
    rate NUMERIC(15, 4) NOT NULL,
    fetched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
EOF
```

### Step 3: Set Environment Variable

```bash
export EXCHANGE_RATE_API_KEY="your_api_key_from_exchangerate-api.com"
```

Or add to `src/main/resources/application.yaml` (for local dev only):

```yaml
exchange-rate:
  api-key: "your_api_key"
  base-url: "https://v6.exchangerate-api.com/v6"
```

### Step 4: Run the Application

```bash
./gradlew bootRun
```

The app starts on `http://localhost:8081`

---

## Quick Start Flow

### 1. Import Postman Collection

Load [src/main/postman/currency-alert-service-auth.json](src/main/postman/currency-alert-service-auth.json) into Postman.

### 2. Login (Captures JWT)

```bash
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "test",
  "password": "test123"
}
```

**Response**: JWT token is auto-captured for subsequent requests

### 3. Create Alert

```bash
POST http://localhost:8081/api/alerts
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "currencyPair": "USD/INR",
  "direction": "ABOVE",
  "threshold": 90.00
}
```

**Response**: 201 Created with alert ID

### 4. Check Rates (Cached)

```bash
GET http://localhost:8081/api/rates?base=USD&target=INR
Authorization: Bearer {{jwt_token}}
```

**Response**: Returns cached rate (kept fresh by scheduler)

### 5. Force Fresh API Fetch

```bash
GET http://localhost:8081/api/rates?base=USD&target=INR&fresh=true
Authorization: Bearer {{jwt_token}}
```

### 6. Watch Alert Trigger

When the **scheduler** runs (every 60s):
1. Fetches USD/INR rate from ExchangeRate API
2. Publishes to Kafka topic `rate-fetched`
3. **Alert Evaluator Consumer** checks: is 95.53 > 90.00? ✅ YES
4. Updates alert status to `TRIGGERED`
5. Publishes to Kafka topic `alert-triggered`

**Check app logs**:
```
Triggered alert for ruleId=... pair=USD/INR threshold=90.00 actualRate=95.5338 direction=ABOVE
```

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/login` | ❌ | Login & get JWT token |
| POST | `/api/alerts` | ✅ | Create new alert rule |
| GET | `/api/alerts` | ✅ | List all active alerts |
| GET | `/api/rates?base=USD&target=INR` | ✅ | Get cached exchange rate |
| GET | `/api/rates?base=USD&target=INR&fresh=true` | ✅ | Force fresh API fetch |

---

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `EXCHANGE_RATE_API_KEY` | (required) | ExchangeRate API key from exchangerate-api.com |
| `spring.redis.host` | localhost | Redis host |
| `spring.datasource.url` | localhost:15432 | PostgreSQL URL |
| `spring.kafka.bootstrap-servers` | localhost:9092 | Kafka broker |

---

## Project Structure

```
src/main/java/com/tharun/currencyalertservice/
├── CurrencyAlertServiceApplication.java    # Boot entry point
├── config/                                 # Spring and Kafka config
├── controller/                             # REST endpoints
├── dto/                                    # request objects
├── domain/                                 # JPA entities
├── event/                                  # Kafka payloads
├── external/                               # external API DTOs
├── properties/                             # app configuration props
├── repository/                             # database access
├── security/                               # JWT auth/filter
├── scheduler/                              # periodic tasks
├── service/                                # business logic
├── consumer/                               # Kafka listeners
└── [other support classes]
```

---

## Testing

Run unit tests:

```bash
./gradlew test
```

Run full integration test:

```bash
./gradlew bootRun &
sleep 5
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'
```

---

## Troubleshooting

### Alert not triggering?
1. Check scheduler logs: `Published rate event to topic=rate-fetched`
2. Verify consumer is listening: `Received rate event from Kafka`
3. Ensure alert status is `ACTIVE` in DB

### Getting cached data instead of fresh?
Use `?fresh=true` query parameter to bypass Redis cache.

### API key not working?
Set `EXCHANGE_RATE_API_KEY` environment variable or update `application.yaml`

---

## License

MIT
