# Architecture Overview

This document describes the **system architecture**, **service responsibilities**, **data flow**, and **cross‑cutting concerns** for E‑Shop Lite — with **Service Discovery** and **Feign-based service‑to‑service (S2S)** included.

## 1. Context & Goals

- Production-style microservices backend using 100% free/open tools.
- Clear separation via **API Gateway**, **Config Server**, **Service Discovery**.
- Observability (metrics/logs/traces) from day one.
- **Service-to-Service** calls enabled using **OpenFeign + Spring Cloud LoadBalancer** with **Eureka** for local/dev.
- Upgrade path to **Kubernetes DNS** (and optionally **Istio/Linkerd** mesh) without code changes.

## 2. High-Level Diagram

```mermaid
flowchart LR
  Client[Client (Postman/UI)] -->|HTTP| Gateway[API Gateway]
  subgraph Platform
    Gateway --> Eureka[(Service Registry - Eureka)]
    Gateway --> Config[Config Server]
  end

  Gateway --> U[User Service]
  Gateway --> C[Catalog Service]

  %% Future services (S2S via Feign)
  O[Order Service] -.Feign.-> C
  O -.Feign.-> I[Inventory Service]

  U -->|JPA| PG[(PostgreSQL users_db)]
  C -->|JPA| PG2[(PostgreSQL catalog_db)]

  subgraph Observability
    Prom[Prometheus]:::obs --> Graf[Grafana]:::obs
    Loki[Loki]:::obs
    Jaeg[Jaeger]:::obs
  end

  U -.metrics.-> Prom
  C -.metrics.-> Prom
  Gateway -.metrics.-> Prom

  U -.logs.-> Loki
  C -.logs.-> Loki
  Gateway -.logs.-> Loki

  U -.traces.-> Jaeg
  C -.traces.-> Jaeg
  Gateway -.traces.-> Jaeg

  classDef obs fill:#eef,stroke:#88f,stroke-width:1px;
```

> **Local/dev:** Eureka provides discovery; Feign references logical service names and uses **Spring Cloud LoadBalancer**.
>
> **Kubernetes later:** switch to **K8s DNS** (`http://inventory-service.default.svc.cluster.local`) or a **service mesh** — no code changes to business logic required.

## 3. Service Responsibilities

### API Gateway
- Single entry point; routes via **logical service IDs** (`lb://USER-SERVICE`).
- JWT validation, correlation ID injection, basic rate limiter (dev).

### Config Server
- Git-backed configuration per service; secrets via environment variables.

### Service Registry (Eureka)
- Service registration & discovery (dev/local).
- Gateway and Feign clients resolve service locations dynamically.

### User Service
- Register, Login (JWT), `/users/me`.
- BCrypt hashing; Problem‑Details error model.

### Catalog Service
- Product create (ADMIN), list/search, get by id.
- ILIKE search, pagination/sort; Problem‑Details error model.

### Future: Order, Inventory, Payment, Notification
- **Synchronous S2S** lookups via **OpenFeign** (e.g., order→inventory check).
- **Asynchronous workflows** via RabbitMQ events for decoupled processing.

## 4. API & S2S (Feign) Design

### Gateway routing (logical names)
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://USER-SERVICE
          predicates: [ Path=/users/**, /auth/** ]
        - id: catalog-service
          uri: lb://CATALOG-SERVICE
          predicates: [ Path=/products/** ]
```

### Feign client (example, future)
```java
@FeignClient(name = "INVENTORY-SERVICE", configuration = FeignCommonConfig.class)
public interface InventoryClient {
  @GetMapping("/inventory/availability/{sku}")
  AvailabilityDto getAvailability(@PathVariable String sku);
}
```

### Feign defaults (timeouts, retries, CB)
- **OpenFeign** + **Spring Cloud LoadBalancer**
- **Resilience4j** for `retry` and `circuitbreaker` per client
- **RequestInterceptor** to propagate `Authorization` (JWT) & `X-Request-Id`

## 5. Data & Persistence

- PostgreSQL per service; Flyway migrations.
- Indices: `users.email` (unique), `products.sku` (unique), `products.category`.

## 6. Security

- AuthN: JWT, 15m TTL; secrets from env/config.
- AuthZ: Gateway + method-level checks in services.
- Input validation + Problem‑Details error schema.
- Header propagation (correlation ID + JWT) for Feign calls.

## 7. Observability

- Metrics: Micrometer → Prometheus → Grafana.
- Logs: JSON → Promtail → Loki.
- Traces: Micrometer Tracing/OTel → Jaeger; `traceparent` propagated by Gateway and Feign interceptor.

## 8. Deployment Choices

- **Local**: Docker Compose + Eureka (discovery) — recommended for development.
- **Kubernetes**: Use **K8s DNS** for discovery. Optional: **Service Mesh** (Istio/Linkerd) for resilience/mTLS.

## 9. NFRs & Performance

- p95 read < 250ms; write < 500ms (dev).
- Timeouts on Feign clients; small retries with backoff; circuit breakers.

## 10. Roadmap (excerpt)

- **Sprint 1**: Identity, Catalog, Platform (Eureka).
- **Sprint 2**: Orders, Inventory; **add Feign clients**; RabbitMQ for async flows.
- **Later**: Payments, Notifications, CI/CD to free hosting; option to migrate to K8s.
