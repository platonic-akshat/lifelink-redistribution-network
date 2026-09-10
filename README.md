# LifeLink — Surplus-to-Need Redistribution Network

Matches surplus medicine, blood, and food from hospitals/pharmacies/blood
banks/restaurants to nearby NGOs and patients in need, in near-real-time,
before it expires or is wasted.

See [ARCHITECTURE.md](./ARCHITECTURE.md) for the full system design (services,
Kafka topics, data model, deployment topology).

## Stack

- **Backend**: Java 21, Spring Boot 3, Spring Cloud Gateway, Kafka + Kafka
  Streams, Postgres + PostGIS, Flyway, Maven multi-module
- **Frontend**: React + TypeScript + Vite (`surplus-portal`), Angular
  (`ngo-dashboard`)
- **Infra**: Docker Compose (Kafka KRaft, Postgres, Redis, Kafka-UI) for local
  dev; Kubernetes manifests sketch in `infra/k8s` for production

## Repo layout

```
backend/            Maven multi-module: common-events + 6 Spring Boot services
frontend/
  surplus-portal/    React app — hospitals/donors post & track surplus
  ngo-dashboard/      Angular app — NGOs review, approve matches, logistics
infra/               docker-compose, Postgres init scripts, k8s manifests
```

## Prerequisites

- Java 21, Maven 3.9+
- Node 20+, npm
- Docker + Docker Compose

## Running locally

### 1. Start infra

```bash
docker compose up -d
```

This brings up Kafka (localhost:9092), Kafka-UI (http://localhost:8090),
Postgres (localhost:5432, databases `surplus_db`/`need_db`/`coordination_db`),
and Redis (localhost:6379).

### 2. Build the backend

```bash
cd backend
mvn clean install
```

### 3. Run backend services (separate terminals, or see each module's README)

```bash
cd backend/surplus-service       && mvn spring-boot:run   # :8081
cd backend/need-service          && mvn spring-boot:run   # :8082
cd backend/matching-service      && mvn spring-boot:run   # :8085 (actuator only, no REST API — pure Kafka Streams)
cd backend/coordination-service  && mvn spring-boot:run   # :8083
cd backend/notification-service  && mvn spring-boot:run   # :8084
cd backend/api-gateway           && mvn spring-boot:run   # :8080  <- single entry point
```

### 4. Run the frontends

```bash
cd frontend/surplus-portal && npm install && npm run dev   # http://localhost:5173
cd frontend/ngo-dashboard  && npm install && npm start     # http://localhost:4200
```

Both frontends talk to the API through `api-gateway` on `http://localhost:8080`.

## Watching the event flow

Open Kafka-UI (http://localhost:8090) to watch `surplus.events`, `need.events`,
`match.proposed`, `match.decisions`, and `logistics.events` populate in
real time as you post surplus / needs and approve matches.

### 5. Get a token and try the API

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"hospital1","password":"anything","role":"PROVIDER"}'
# -> {"token": "..."}

curl -X POST http://localhost:8080/api/surplus \
  -H "Content-Type: application/json" -H "Authorization: Bearer <token>" \
  -d '{"category":"BLOOD","subType":"O-negative","quantity":2,"unit":"units",
       "latitude":19.076,"longitude":72.877,"expiresAt":"2026-09-15T00:00:00Z",
       "providerId":"11111111-1111-1111-1111-111111111111","providerName":"City Hospital"}'
```

Post a matching need via `need-service` (through the gateway at `/api/needs`)
in the same category/area and watch `match.proposed` appear in Kafka-UI within
a few seconds — that's `matching-service`'s Kafka Streams topology finding it.

## Status

This is a **starter repo**, not a finished product. Every service and both
frontends are implemented end-to-end and compile/build cleanly (`mvn install`
across the whole `backend/` reactor, `npm run build` for both frontends), but
several things are deliberately left for the next iteration rather than
gold-plated here — see ARCHITECTURE.md for the reasoning behind each:

- **Outbox pattern** — services publish to Kafka right after a DB commit, not
  atomically with it (§7).
- **PostGIS radius queries** — surplus/need-service use a geohash column +
  in-memory haversine filter instead of `ST_DWithin` (§6).
- **Real auth** — `api-gateway` issues a self-signed JWT for any non-blank
  username/password; no user directory. Swap for Keycloak/Auth0 (§4.6, §8).
- **Geohash neighbor fan-out** — matching-service joins within one geohash
  cell (~5km); an item just across a cell boundary can be missed (§4.3, §9).
- **Observability** — Prometheus/Grafana/tracing are documented, not wired.
