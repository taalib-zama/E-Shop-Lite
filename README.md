## What is E-Shop Lite?
A production-style microservices backend built entirely with free/open-source tools. It's designed for learning and portfolio building — not a toy app, but structured like a real system with proper security, observability, and scalability patterns.

> Client (Postman/UI)
↓
API Gateway (8080)          ← single entry point
↓
┌────────────────┐
│ User Service   │ (8081)   ← register, login, JWT
│ Catalog Service│ (8082)   ← products CRUD
└────────────────┘
↓
PostgreSQL (per service)


## Supporting platform:

Config Server (8888) — serves config to all services from config-repo/ folder

Service Registry / Eureka (8761) — services register here; gateway routes via lb://USER-SERVICE instead of hardcoded IPs


## What Each Service Does
API Gateway

Routes all traffic to the right service

Validates JWT tokens before forwarding requests

Injects correlation/trace IDs

Rate limiting (dev only)



## User Service

POST /users — register (BCrypt password, role=USER)

POST /auth/login — returns a 15-minute JWT

GET /users/me — returns current user (requires token)


## Catalog Service

POST /products — create product (ADMIN only)

GET /products — list/search with filters, pagination, sorting

GET /products/{id} — get single product or 404

## Config Server

Serves application.yml configs to all services at startup from the local config-repo/ folder


## Service Registry

Eureka server — all services register on startup, gateway discovers them dynamically


## Data Model

### users_db
users: id(UUID), name, email(unique), password_hash, role, status, created_at, updated_at

### catalog_db
products: id(UUID), name, sku(unique), price, currency, description, category, attributes(JSONB), created_at, updated_at

### Migrations are handled by Flyway — V1__init.sql in each service creates the tables on first run.

## Security Flow
Client calls POST /auth/login → user-service issues a JWT (HMAC-signed, 15min expiry)

Client sends Authorization: Bearer <token> on subsequent requests

Gateway validates the JWT and forwards the request with role info

Services also validate the JWT independently for method-level security (e.g. ROLE_ADMIN for product creation)


## Observability Stack
Concern	Tool	How
Metrics	Prometheus → Grafana	/actuator/prometheus scraped every 15s
Logs	Loki + Promtail	JSON logs with traceId field
Traces	Jaeger	OTel exporter sends spans to localhost:4318
All infra runs via Docker Compose in the infra/ folder.


## Startup Order
docker compose up -d          ← postgres, jaeger, prometheus, grafana, loki
config-server                 ← must be first
service-registry              ← eureka
api-gateway                   ← needs eureka
user-service                  ← registers with eureka
catalog-service               ← registers with eureka
