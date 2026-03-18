# System Design — Addendum (Service Discovery & Feign)

**Date:** 2026-03-04

### Service Discovery Strategy

- **Local/dev**: Spring Cloud **Eureka** for dynamic discovery; Gateway and Feign resolve `lb://SERVICE-ID`.
- **Production/Kubernetes**: Prefer **Kubernetes DNS**; optional **Service Mesh** (Istio/Linkerd) for mTLS, retries, outlier detection, and traffic policy — without code changes.

### Feign S2S Best Practices

- Use **OpenFeign** + **LoadBalancer** with **logical service names**.
- Configure **timeouts, retries, circuit breakers** (Resilience4j) per client.
- **Propagate headers**: `Authorization` (JWT) & `X-Request-Id` for traceability.
- Avoid chatty chains; prefer **events** (RabbitMQ) for workflows.
