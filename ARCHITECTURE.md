# LifeLink — Surplus-to-Need Redistribution Network

Real-time matching of surplus medicine, blood units, and food from hospitals,
pharmacies, blood banks, and restaurants to nearby NGOs and patients in need —
before it expires or goes to waste.

## 1. Problem & Goals

- Hospitals/pharmacies discard expiring medicine; blood banks have units nearing
  shelf-life limits; restaurants have surplus edible food daily — while NGOs and
  patients nearby have an active, often urgent, need for exactly that.
- Matching today is manual (phone calls, WhatsApp groups) — slow, not geo-aware,
  and not urgency-aware. Time-critical items (blood, insulin, perishable food)
  expire before a match is found.
- **Goal**: event-driven system that matches surplus to need in near-real-time,
  ranked by geo-proximity + urgency + expiry window, with a human approval step
  before logistics are committed (safety-critical domain — no fully automated
  hand-off of medicine/blood).

## 2. Domain Model

| Concept | Description |
|---|---|
| **Surplus** | An item a provider (hospital/pharmacy/blood bank/restaurant) has in excess: category (MEDICINE / BLOOD / FOOD), sub-type, quantity, unit, expiry timestamp, location, provider ID. |
| **Need** | A request from an NGO or patient: category, sub-type, quantity needed, urgency (CRITICAL / HIGH / NORMAL), location, requester ID. |
| **Match** | A system-proposed pairing of one or more Surplus items to a Need, scored by distance + urgency + time-to-expiry. Requires NGO/coordinator approval. |
| **Logistics** | The pickup/delivery lifecycle of an approved match: SCHEDULED → PICKED_UP → DELIVERED / FAILED. |

## 3. High-Level Architecture

```
                    ┌─────────────────┐        ┌──────────────────┐
                    │  surplus-portal │        │   ngo-dashboard   │
                    │   (React, TS)   │        │     (Angular)     │
                    │ hospitals/donors│        │  NGOs/coordinators│
                    └────────┬────────┘        └─────────┬─────────┘
                             │  HTTPS/JSON                │ HTTPS/JSON
                             └──────────────┬──────────────┘
                                             │
                                   ┌─────────▼─────────┐
                                   │    api-gateway     │  Spring Cloud Gateway
                                   │  (JWT auth, rate   │  routes + auth for all
                                   │   limit, routing)  │  downstream services
                                   └─────────┬─────────┘
              ┌───────────────┬──────────────┼──────────────┬───────────────┐
              ▼               ▼              ▼              ▼               ▼
      ┌───────────────┐┌─────────────┐┌──────────────┐┌────────────────┐┌────────────┐
      │surplus-service ││need-service ││matching-svc  ││coordination-svc││notification│
      │  (CRUD + geo)  ││ (CRUD + geo)││(Kafka Streams)││(approve/logis.)││  -service  │
      └───────┬────────┘└──────┬──────┘└──────┬───────┘└────────┬───────┘└─────┬──────┘
              │                │               │                 │              │
              │ surplus.events │ need.events    │ match.proposed  │match.decisions│
              └───────────────►│◄───────────────┴────────────────┴──────────────┘
                                │          Kafka (KRaft, 3 brokers in prod)
                                │  topics: surplus.events, need.events,
                                │  match.proposed, match.decisions, logistics.events
                                ▼
                     ┌─────────────────────┐
                     │  Postgres + PostGIS  │  database-per-service:
                     │  Redis (geo cache)   │  surplus_db, need_db, coordination_db
                     └─────────────────────┘
```

## 4. Services

### 4.1 `surplus-service`
- Owns Surplus lifecycle: POSTED → CLAIMED → EXPIRED / COMPLETED.
- REST API for providers to create/update/withdraw postings.
- On create/update/expire, publishes to Kafka topic `surplus.events`.
- Storage: `surplus_db` (Postgres + PostGIS for `location GEOGRAPHY(Point)`).
- Background job (Spring `@Scheduled`) sweeps near-expiry items and emits
  `SURPLUS_EXPIRING_SOON` events so matching-service can prioritize them.

### 4.2 `need-service`
- Owns Need lifecycle: POSTED → MATCHED → FULFILLED / CANCELLED.
- REST API for NGOs/patients to create/update needs, with urgency field.
- Publishes to `need.events`.
- Storage: `need_db` (Postgres + PostGIS).

### 4.3 `matching-service` (the core, non-trivial piece)
- Stateless Kafka Streams application (no REST API, no DB — Kafka is the source
  of truth for matching state via KTables).
- Topology:
  1. Consume `surplus.events` and `need.events` into `KTable`s keyed by
     `geohash5` (item location truncated to ~5km grid cell) + `category`.
  2. Windowed join (`JoinWindows.of(Duration.ofHours(...))`) surplus-need pairs
     that share a geohash neighborhood (checks the cell + 8 neighbors) and
     category.
  3. Score each candidate pair: `score = w1*urgencyWeight + w2*(1/distanceKm) +
     w3*(1/hoursToExpiry)`.
  4. Emit the top-scoring candidate(s) per Need to `match.proposed`.
- Deliberately **does not auto-commit** — a human (NGO coordinator) must approve
  via `coordination-service`, since misrouted medicine/blood has real safety
  consequences.

### 4.4 `coordination-service`
- Consumes `match.proposed`, persists proposals for coordinator review.
- REST API: list pending matches, approve/reject, schedule logistics.
- On approve, publishes `match.decisions` (APPROVED) and creates a Logistics
  record; on pickup/delivery confirmation (via app), publishes to
  `logistics.events`.
- Storage: `coordination_db`.

### 4.5 `notification-service`
- Consumes `match.proposed`, `match.decisions`, `logistics.events`,
  `SURPLUS_EXPIRING_SOON`.
- Fans out to email/SMS/push (starter repo stubs this behind a
  `NotificationSender` interface — swap in Twilio/SES in production).
- Stateless, no DB (idempotency via Kafka consumer group offsets).

### 4.6 `api-gateway`
- Single entry point for both frontends. Spring Cloud Gateway.
- JWT validation (issued by an `auth` flow — starter repo uses a simple
  username/password + JWT issuer stub; production should delegate to
  Keycloak/Auth0).
- Path-based routing to the five backend services; per-route rate limiting.

## 5. Kafka Topics & Contracts

| Topic | Key | Value | Producers | Consumers |
|---|---|---|---|---|
| `surplus.events` | `surplusId` | `SurplusEvent` (POSTED/UPDATED/CLAIMED/EXPIRING_SOON/EXPIRED) | surplus-service | matching-service, notification-service |
| `need.events` | `needId` | `NeedEvent` (POSTED/UPDATED/MATCHED/FULFILLED/CANCELLED) | need-service | matching-service |
| `match.proposed` | `needId` | `MatchProposedEvent` (needId, surplusIds[], score, distanceKm, expiresInHours) | matching-service | coordination-service, notification-service |
| `match.decisions` | `matchId` | `MatchDecisionEvent` (APPROVED/REJECTED, decidedBy) | coordination-service | notification-service, surplus-service, need-service |
| `logistics.events` | `matchId` | `LogisticsEvent` (SCHEDULED/PICKED_UP/DELIVERED/FAILED) | coordination-service | notification-service |

Event schemas are defined once in `backend/common-events` and shared as a Maven
dependency — this is the contract every service compiles against, avoiding
schema drift. Production hardening: move to Confluent Schema Registry +
Avro/Protobuf for cross-language compatibility and schema evolution checks.

## 6. Data Storage

- **Database-per-service**: `surplus_db`, `need_db`, `coordination_db` — each
  service owns its schema, no cross-service joins. matching-service is stateless
  (Kafka Streams state stores only, backed by RocksDB + changelog topics).
- **PostGIS** `GEOGRAPHY(Point, 4326)` columns + GIST index for radius queries
  (`ST_DWithin`) — used by surplus/need-service for "near me" listings.
- **Redis**: geohash → active-postings cache to keep matching-service's REST
  lookups (if any UI needs live counts) fast without hammering Postgres.
- **Flyway** migrations per service, versioned in `src/main/resources/db/migration`.

## 7. Deployment Topology (production shape)

- Each service is an independently deployable container (Dockerfile provided
  per module). Local dev: `docker-compose.yml` at repo root (Kafka in KRaft
  mode, Postgres, Redis, Kafka-UI for topic inspection).
- Production target: Kubernetes — one Deployment + Service per microservice,
  HPA on CPU/consumer-lag, `api-gateway` behind an Ingress/LB. Namespace-scoped
  NetworkPolicies restrict DB access to the owning service only.
- Config: `application.yml` defaults for local dev; externalized via
  Kubernetes ConfigMaps/Secrets (or Spring Cloud Config) in production —
  never bake secrets into images.
- Observability (documented, not scaffolded in the starter): Micrometer →
  Prometheus, Grafana dashboards per service + Kafka consumer-lag panel,
  structured JSON logs → Loki/ELK, distributed tracing via
  Micrometer Tracing + Zipkin/Tempo (propagate trace ID through Kafka headers).
- Resilience: idempotent consumers (dedupe by event ID), retry topics +
  dead-letter topics per consumer group, outbox pattern for
  surplus/need-service so DB write + Kafka publish are transactionally
  consistent (starter repo notes this; full outbox implementation is a
  documented next step).

## 8. Security & Trust (domain-specific)

- All providers/NGOs are **verified accounts** (manual KYC step in production —
  out of scope for the starter, but the `Provider`/`Requester` entities carry a
  `verified` flag that gates posting/claiming).
- Coordinator approval step in `coordination-service` is the safety gate before
  any medicine/blood physically changes hands — the system never auto-approves
  medical items.
- Audit trail: every state transition is a Kafka event, so the full history of
  who posted/approved/moved an item is replayable from the log.

## 9. Why This Is Technically Non-Trivial

- Geo + urgency + time-decay multi-factor matching via Kafka Streams windowed
  joins (not just a simple pub/sub fan-out).
- Consistency between REST-owned state (Postgres) and the event log (outbox
  pattern) — a classic distributed-systems problem, not a CRUD demo.
- Human-in-the-loop approval on top of an otherwise automated pipeline —
  balances automation speed with real-world safety constraints.

## 10. Repo Layout

```
lifelink-redistribution-network/
├── ARCHITECTURE.md
├── README.md
├── docker-compose.yml
├── backend/
│   ├── pom.xml                  (parent)
│   ├── common-events/           (shared event DTOs + Kafka config)
│   ├── surplus-service/
│   ├── need-service/
│   ├── matching-service/
│   ├── coordination-service/
│   ├── notification-service/
│   └── api-gateway/
├── frontend/
│   ├── surplus-portal/          (React + TS + Vite)
│   └── ngo-dashboard/           (Angular)
└── infra/
    └── k8s/                     (illustrative manifests)
```
