# Run LifeLink locally (step by step)

This guide walks through starting the full stack on your machine. Each step explains **what it does** and **why the order matters**.

For architecture and design, see [ARCHITECTURE.md](./ARCHITECTURE.md). For a shorter overview, see [README.md](./README.md).

---

## What you are starting

LifeLink is several Spring Boot services plus a React app. They talk over **HTTP** (through one gateway) and **Kafka** (events between services). **Postgres** holds each service’s data; **Keycloak** issues login tokens.

| Component | Port | Role |
|-----------|------|------|
| **api-gateway** | 8080 | Single HTTP entry point for the UI and `curl` |
| **surplus-service** | 8081 | Hospitals/donors post surplus |
| **need-service** | 8082 | NGOs post needs |
| **coordination-service** | 8083 | Approve/reject matches, logistics |
| **notification-service** | 8084 | Email/SMS-style notifications from Kafka (optional for demo) |
| **matching-service** | 8085 | Kafka Streams — finds surplus/need pairs (no REST API) |
| **surplus-portal** (Vite) | 5173 | React UI |
| **Kafka** | 9092 | Event bus |
| **Postgres** | 5432 | Databases `surplus_db`, `need_db`, `coordination_db` |
| **Keycloak** | 8180 | Login (`lifelink` realm) |
| **Kafka UI** | 8090 | Browse topics (debugging) |

---

## Prerequisites

Install once:

- **Java 21** and **Maven 3.9+** — compile and run backend services
- **Node 20+** and **npm** — run the React portal
- **Docker Desktop** (or Docker Engine + Compose) — easiest way to run Kafka, Postgres, Keycloak, Redis

From the **repository root** (`lifelink-redistribution-network/`), run all commands below unless a path is given.

---

## Step 1 — Start infrastructure (Docker Compose)

```bash
docker compose up -d
```

**What this does**

- **Kafka** — message broker; services publish/consume topics like `surplus.events`, `match.proposed`, `match.decisions`, `logistics.events`.
- **Postgres** — runs init scripts from `infra/postgres-init/` and creates the three app databases with user `lifelink` / password `lifelink`.
- **Keycloak** — imports `infra/keycloak/lifelink-realm.json` (dev users and roles).
- **Kafka UI** — web UI to watch messages (not required for the app to work).
- **Redis** — present for future caching; not on the critical demo path.

**Check**

```bash
docker compose ps
```

Wait until `kafka` and `postgres` are healthy (or give them ~30 seconds on first boot).

---

## Step 2 — Build the backend (Maven)

```bash
cd backend
mvn clean install -DskipTests
```

**What this does**

- Compiles all modules (`common-events`, `common-security`, `common-outbox`, six services).
- Installs JARs into your local Maven cache so `spring-boot:run` resolves sibling modules.
- `-DskipTests` speeds the first build; run `mvn verify` when you want tests (some need Docker for Testcontainers).

You only need to repeat this after pulling code changes or editing backend modules.

---

## Step 3 — Start backend services (six terminals)

Run each command in its **own terminal** (or use your IDE’s Spring Boot run configs). Stay in `backend/<module>` for each.

### 3a — Surplus service (port 8081)

```bash
cd backend/surplus-service
mvn spring-boot:run
```

**What this does:** REST API for posting/listing surplus; writes to `surplus_db`; publishes `surplus.events` via the transactional outbox.

### 3b — Need service (port 8082)

```bash
cd backend/need-service
mvn spring-boot:run
```

**What this does:** REST API for needs; `need_db`; publishes `need.events`.

> **Why surplus + need before matching:** Kafka Streams in `matching-service` expects source topics `surplus.events` and `need.events` to exist (or it can fail on startup). Starting these two first (or posting once) creates those topics.

### 3c — Matching service (port 8085)

```bash
cd backend/matching-service
mvn spring-boot:run
```

**What this does:** Continuously joins surplus and need streams by category/location and emits **`match.proposed`** when it finds a candidate pair. No HTTP API — only Kafka and actuator.

### 3d — Coordination service (port 8083)

```bash
cd backend/coordination-service
mvn spring-boot:run
```

**What this does:** Stores match proposals from Kafka; exposes `/api/matches` and `/api/logistics`; on approve, reserves surplus in `allocated_surplus` and emits decisions/logistics events.

### 3e — Notification service (port 8084) — optional

```bash
cd backend/notification-service
mvn spring-boot:run
```

**What this does:** Listens to match/logistics topics and logs or sends notifications. The happy path works without it.

### 3f — API gateway (port 8080) — start last among backends

```bash
cd backend/api-gateway
mvn spring-boot:run
```

**What this does:** Routes `/api/surplus/**`, `/api/needs/**`, `/api/matches/**`, `/api/logistics/**`, `/api/organizations/**`, `/api/admin/**` to the services above; validates JWTs from Keycloak.

**Quick health check**

```bash
curl -s http://localhost:8080/actuator/health
curl -s http://localhost:8081/actuator/health
```

---

## Step 4 — Start the React portal

```bash
cd frontend/surplus-portal
npm install
export VITE_API_BASE_URL=http://localhost:8080
npm run dev
```

**What this does**

- Serves the UI at **http://localhost:5173**.
- Login uses **Keycloak** at `http://localhost:8180` (defaults in code).
- API calls go to **`VITE_API_BASE_URL`** with the bearer token from login.

Open **http://localhost:5173** in the browser.

| Dev user | Password | Use in UI |
|----------|----------|-----------|
| `provider1` | `provider1` | Create surplus |
| `ngo1` | `ngo1` | Create/list needs |
| `coordinator1` | `coordinator1` | Match review & logistics |
| `admin1` | `admin1` | Admin APIs (usually via `curl`/Postman through gateway) |

On the login screen, pick the role that matches the user (Provider / NGO / Coordinator).

---

## Step 5 — Walk through the happy path

**What you are proving:** data flows from UI → gateway → services → Kafka → matching → coordination → back to surplus/need via events.

1. **Provider** — Log in as provider → create surplus (category, location, expiry).
2. **NGO** — Log in as NGO → create a need (same category, nearby lat/lon).
3. **Kafka** — Open http://localhost:8090 → confirm **`match.proposed`** (matching-service found a pair).
4. **Coordinator** — Log in as coordinator → **Match review** → approve.
5. **Coordinator** — **Logistics** → mark picked up → delivered (or failed to test reopen + allocation release).

Surplus and need statuses update via Kafka consumers in surplus/need services (CLAIMED/MATCHED → COMPLETED/FULFILLED).

---

## Step 6 — Optional: call the API with curl

Get a token from Keycloak (password grant, same as the UI):

```bash
TOKEN=$(curl -s -X POST 'http://localhost:8180/realms/lifelink/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&client_id=lifelink-web&username=provider1&password=provider1' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

curl -s http://localhost:8080/api/surplus?providerId=11111111-1111-1111-1111-111111111111 \
  -H "Authorization: Bearer $TOKEN"
```

**What this does:** Confirms gateway + Keycloak + surplus-service without the browser.

---

## Startup order (cheat sheet)

```text
1. docker compose up -d
2. mvn clean install -DskipTests   (in backend/, once)
3. surplus-service
4. need-service
5. matching-service
6. coordination-service
7. notification-service   (optional)
8. api-gateway
9. npm run dev              (surplus-portal, with VITE_API_BASE_URL)
```

---

## Troubleshooting

| Symptom | Likely cause | What to do |
|---------|----------------|------------|
| `matching-service` fails on startup | Missing `surplus.events` / `need.events` | Start surplus + need first, or create topics manually |
| UI login fails | Keycloak not up | `docker compose ps`, open http://localhost:8180 |
| UI API calls fail / wrong host | Missing API base URL | Set `VITE_API_BASE_URL=http://localhost:8080` |
| 401 on API | Expired token | Sign out and sign in again |
| Postgres connection refused | Compose not running | `docker compose up -d` |
| Surplus create forbidden | Org not verified | Default orgs are VERIFIED in Flyway; check coordination logs |

**Legacy stub login** (no Keycloak): set `VITE_LEGACY_LOGIN=true` on the frontend and `lifelink.auth.legacy-login-enabled=true` on the gateway — dev only, not recommended.

---

## Stopping everything

```bash
# Stop Spring Boot apps: Ctrl+C in each terminal

# Stop infra
docker compose down
```

To wipe Postgres data: `docker compose down -v` (destroys the volume; databases are recreated on next `up`).

---

## Without Docker

You can run **Postgres** and **Kafka** via Homebrew and skip Keycloak only if you enable legacy login — see [README.md](./README.md) § “Without Docker”. The supported path for auth and CI parity is **Docker Compose including Keycloak**.
