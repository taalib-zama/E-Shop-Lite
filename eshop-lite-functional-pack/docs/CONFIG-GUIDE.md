# Config & Environments Guide

**Project:** E‑Shop Lite  
**Version:** 1.0.0  
**Date:** 2026-03-03

---

## 1. Environments
| Env | Purpose | Config Source | Notes |
|-----|---------|---------------|-------|
| local | Developer machine | Spring Cloud **Config Server** (git‑backed local folder) | `.env` for DB & JWT secrets |

## 2. Config Server Layout (example)
```
config-repo/
  application.yml            # common defaults
  api-gateway.yml
  service-registry.yml
  config-server.yml
  user-service.yml
  catalog-service.yml
```

## 3. Required Properties (per service)
- **Database**: `spring.datasource.url`, `username`, `password`
- **Flyway**: `spring.flyway.enabled=true`
- **Security/JWT**: `security.jwt.secret` (dev HMAC), `security.jwt.expMinutes=15`
- **CORS**: `cors.allowed-origins`
- **Logging**: JSON encoder on; include `traceId`
- **Actuator**: expose health/info/metrics/prometheus

## 4. Ports (defaults)
- `api-gateway:8080`  
- `user-service:8081`  
- `catalog-service:8082`

## 5. Secrets Handling (local)
- Use environment variables or `.env` (not committed).  
- Never store real secrets in repo.  
- For HMAC dev secret: use a long random string ≥ 32 chars.

## 6. Observability Wiring
- **Prometheus** scrapes `/actuator/prometheus` on services (targets configured in `infra/prometheus.yml`).
- **Loki/Promtail** ingest Docker logs; if unavailable, configure Logback to file appender and update Promtail paths.
- **Jaeger** receives OTLP from services (gRPC :4317 / HTTP :4318).

## 7. Local Run Order
1) Start infra: `docker compose up -d` in `infra/`  
2) Start **config-server**, then **service-registry**, then **api-gateway**  
3) Start **user-service** and **catalog-service**  
4) Verify health endpoints; run Postman smoke tests

## 8. Troubleshooting
- **Reusing db ports**: stop local Postgres; ensure `5432` free.  
- **No logs in Loki**: check Promtail mounts; fallback to file appender.  
- **No traces**: ensure OTel exporter enabled; Jaeger ports open.
