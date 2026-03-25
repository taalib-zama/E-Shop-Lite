# Sprint 2 Plan - Orders, Inventory, and Reliability

**Project:** E-Shop Lite  
**Sprint:** Sprint 2  
**Date:** 2026-03-26

---

## 1. Sprint Goal
Deliver a reliable Order and Inventory foundation with:
- order placement and status query
- inventory reserve/release
- resilient service-to-service integration (Feign + Resilience4j)
- reliable async eventing (RabbitMQ + Outbox)

## 2. Scope
### In Scope
- `order-service` and `inventory-service` service foundations
- Gateway routes for `/orders/**` and `/inventory/**`
- Feign call path from order to inventory
- Resilience policies (timeouts, retries, circuit breaker)
- Outbox-based reliable event publication
- Testcontainers-based integration tests (Postgres + RabbitMQ)

### Out of Scope
- Payment settlement and refund logic
- Notification service workflows
- Multi-warehouse allocation strategy
- CDC/Debezium (can come later)

## 3. Story Set and Sizing
- US-06 Place Order (8)
- US-07 Reserve and Release Inventory (5)
- US-08 Order Status Projection (3)
- US-09 Reliable Event Publish (Outbox) (8)
- US-10 Resilient S2S Failure Handling (5)

**Total:** 29 story points

## 4. Technical Deltas to Current Architecture
- Add services:
  - `services/order-service`
  - `services/inventory-service`
- Add config entries:
  - `config-repo/order-service.yml`
  - `config-repo/inventory-service.yml`
- Extend gateway routes:
  - `/orders/**` -> `lb://ORDER-SERVICE`
  - `/inventory/**` -> `lb://INVENTORY-SERVICE`
- Introduce OpenFeign in order-service
- Add Resilience4j policies per downstream client
- Add RabbitMQ exchanges/queues/routing keys for order events
- Add outbox table and dispatcher in order-service

## 5. Execution Plan (10 working days)

### Week 1
- **Day 1-2**: Service skeletons, Flyway schemas, core entities and repositories
- **Day 3-4**: Order create/get APIs and inventory reserve/release APIs
- **Day 5**: Feign integration + header propagation + basic resilience settings

### Week 2
- **Day 6-7**: Outbox table, writer, dispatcher; RabbitMQ consumer idempotency
- **Day 8-9**: Integration testing (API + async + resilience), observability checks
- **Day 10**: Hardening, docs update, sprint demo prep

## 6. Definition of Done (Sprint 2)
- Order and inventory critical APIs implemented and tested
- Outbox flow verified end-to-end with retries
- Resilience behavior observable and validated
- OpenAPI updated for new endpoints
- Metrics/logs/traces verified across gateway -> order -> inventory
- Sprint 2 smoke suite green through gateway

## 7. Risks and Mitigations
- **Risk:** Event duplication in async flow  
  **Mitigation:** idempotency keys and processed-event tracking.

- **Risk:** Inventory timeout cascading failures  
  **Mitigation:** strict timeout/retry/circuit-breaker configs and fallback status.

- **Risk:** Scope growth into payment/notifications  
  **Mitigation:** enforce sprint out-of-scope boundaries.

## 8. Deliverables
- Story docs: `US-6.md` to `US-10.md`
- Test plan: `docs/TEST-PLAN-S2.md`
- Story index: `docs/USER-STORIES-S2.md`
- Sprint traceability: `SPRINT-2-TRACEABILITY.md` (to be created)
- OpenAPI updates and Postman additions for new routes

