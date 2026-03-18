# Delivery Plan (Updated for Service Discovery & Feign)

**Date:** 2026-03-04

## Sprint 0 — Platform & Infra (2–3 days)
- Config Server (git-backed local `config-repo`)
- **Eureka Server** (`service-registry`) — discovery for local/dev
- API Gateway (Spring Cloud Gateway) — correlation ID, JWT stub, rate limiter (dev)
- Docker Compose infra (Postgres, RabbitMQ, Prometheus, Grafana, Loki, Promtail, Jaeger)
- OpenAPI contracts (user, catalog); Flyway migrations
- CI seed

**Acceptance**:
- Services register with Eureka; visible at `http://localhost:8761`
- Gateway routes via `lb://USER-SERVICE` & `lb://CATALOG-SERVICE`

## Sprint 1 — Identity & Catalog (1–2 weeks)
- **user-service**: register, login (JWT), `/users/me`
- **catalog-service**: create (ADMIN), list/search, get-by-id
- Gateway enforcement (permit/secure routes), Problem‑Details error model
- Observability wiring (metrics/logs/traces)

**Acceptance**:
- Postman smoke suite green via Gateway
- Metrics at Prometheus; logs in Loki; traces in Jaeger

## Sprint 2 — Orders & Inventory (Start)
- **Introduce OpenFeign** for S2S
  - `order-service` → `inventory-service` (availability/reservation) via Feign
  - **Resilience4j**: timeouts, retries, circuit breaker
  - **Header propagation**: JWT (if needed), `X-Request-Id`
- **RabbitMQ** for async events (order.created, payment.*)
- **Outbox** pattern in `order-service` for reliable event publishing

**Acceptance**:
- S2S calls resolved via **Eureka** locally
- Circuit breaker and retry metrics observable
- Events published/consumed; idempotency validated

## Future Sprints
- Payments (mock), Notifications (mock)
- Security hardening, Rate limiting policies
- CI/CD to free hosts; optional migration to **Kubernetes + K8s DNS/Service Mesh**
