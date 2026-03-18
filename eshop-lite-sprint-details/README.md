# E‑Shop Lite — Free & Open‑Source Microservices Backend

Cloud‑ready, production‑style **microservices** using **only free/OSS** tools.

## Tech Stack

Java 21 · Spring Boot 3 · Spring Cloud (Gateway, **Eureka**, Config) · OpenFeign · Spring Cloud LoadBalancer · Resilience4j · PostgreSQL · JPA/Hibernate · Flyway · RabbitMQ · Prometheus · Grafana · Loki/Promtail · Jaeger · Docker Compose

## Architecture Highlights

- **API Gateway**: routes to services via **logical names** (`lb://SERVICE-ID`).
- **Service Discovery**: **Eureka** for local/dev; future **K8s DNS** (and mesh) without code changes.
- **S2S Calls**: **OpenFeign** + **LoadBalancer** (+ Resilience4j timeouts/retries/circuit breakers).
- **Async**: RabbitMQ for event-driven flows (orders/payments/notifications).
- **Security**: Spring Security + JWT; password hashing with BCrypt.
- **Observability**: Actuator/Micrometer, JSON logs, distributed tracing.

## Local Development

1. **Infra**
   ```bash
   cd infra
   docker compose up -d
   ```

2. **Run platform & services (order)**
   ```bash
   # config & discovery
   mvn -q -pl platform/config-server spring-boot:run
   mvn -q -pl platform/service-registry spring-boot:run

   # business services
   mvn -q -pl services/user-service spring-boot:run
   mvn -q -pl services/catalog-service spring-boot:run

   # gateway (uses discovery)
   mvn -q -pl platform/api-gateway spring-boot:run
   ```

3. **Gateway**: `http://localhost:8080`

## Discovery & Feign

- **Eureka**: services register with `service-registry (:8761)`.
- **Gateway routing example**:
  ```yaml
  uri: lb://USER-SERVICE
  ```
- **Feign** (future services):
  ```java
  @FeignClient(name = "INVENTORY-SERVICE")
  interface InventoryClient { ... }
  ```
- **Resilience** (timeouts, retries, circuit breaker) configured per Feign client.

## Upgrade Path

- **Kubernetes**: replace Eureka with **native K8s DNS** and (optionally) a **service mesh** (Istio/Linkerd). No app code changes required; Feign can target service DNS names or keep logical names with LoadBalancer.

See `docs/ARCHITECTURE.md` for details.
