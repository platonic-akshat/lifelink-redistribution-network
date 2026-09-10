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
- Docker + Docker Compose **or** Homebrew (see below) — either works, no
  admin/root privileges required for the Homebrew path

## Running locally

### 1. Start infra

**With Docker:**

```bash
docker compose up -d
```

This brings up Kafka (localhost:9092), Kafka-UI (http://localhost:8090),
Postgres (localhost:5432, databases `surplus_db`/`need_db`/`coordination_db`),
and Redis (localhost:6379).

**Without Docker (e.g. locked-down/work laptop with no Docker Desktop access)
— plain Homebrew formulae, no privileged install step:**

```bash
brew install postgresql@16 kafka

# Kafka (KRaft mode, single broker) - Homebrew's post-install already
# formats the storage dir, so just start it:
/opt/homebrew/opt/kafka/bin/kafka-server-start /opt/homebrew/etc/kafka/server.properties &

# Postgres needs LC_ALL set on Apple Silicon or it fails to start:
LC_ALL="en_US.UTF-8" /opt/homebrew/opt/postgresql@16/bin/postgres \
  -D /opt/homebrew/var/postgresql@16 &

# Create the app role + the three per-service databases:
createuser -h localhost -U "$(whoami)" lifelink --createdb
psql -h localhost -U "$(whoami)" -d postgres -c "ALTER ROLE lifelink WITH PASSWORD 'lifelink';"
for db in surplus_db need_db coordination_db; do
  createdb -h localhost -U "$(whoami)" -O lifelink "$db"
done
```

Topics (`surplus.events`, `need.events`, `match.proposed`, `match.decisions`,
`logistics.events`) don't need to be created manually — Kafka auto-creates
them on first publish, **except** `matching-service` will crash on startup
with `MissingSourceTopicException` if `surplus.events`/`need.events` don't
exist yet (Kafka Streams needs its source topics to exist before it can
build its physical plan). Start `surplus-service`/`need-service` first (or
create the topics explicitly with `kafka-topics --create`), *then*
`matching-service`.

This repo skips Redis and PostGIS in the no-Docker path — neither is on the
critical path for the demo (see ARCHITECTURE.md §6 and the Status section
below).

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
- **Surplus/need status sync on decision** — `match.decisions` is documented
  as consumed by surplus-service/need-service (§5) to flip a claimed item to
  `CLAIMED`/`MATCHED`, but that consumer isn't implemented yet; today only
  coordination-service's own `MatchProposal`/`Logistics` records reflect the
  decision. A real approval currently leaves the original surplus/need row
  showing `POSTED` even after it's been matched and delivered.

Verified end-to-end on 2026-09-10 by running the full stack (Kafka + Postgres
via Homebrew, all 6 services, both frontends) and completing the whole happy
path: post surplus → post need → Kafka Streams match found → approve →
logistics SCHEDULED → PICKED_UP → DELIVERED. Three real bugs were caught and
fixed in that pass: missing CORS support on the gateway, the JWT filter
blocking CORS preflight on protected routes, and a Kafka Streams header
propagation issue that made `match.proposed` messages fail deserialization
in coordination-service/notification-service.
