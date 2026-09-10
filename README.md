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
cd backend/matching-service      && mvn spring-boot:run   # no HTTP port, Kafka Streams only
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

## Status

This is a **starter repo** — architecture and core matching logic
(surplus-service, need-service, matching-service) are fully wired; some
supporting services and frontend screens are intentionally minimal scaffolds
to build on. See each module's own README/TODOs for what's next.
