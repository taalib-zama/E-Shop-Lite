# Operations Runbook — E‑Shop Lite

**Date:** 2026-03-03

---

## 1. Bring Up Local Infra

```bash
cd infra
docker compose up -d
```

**Dashboards & Ports**

- RabbitMQ: http://localhost:15672 (guest/guest)
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)
- Jaeger: http://localhost:16686
- Postgres: localhost:5432 (user/pass: eshop/eshop)

## 2. Starting Services

Recommended order: config-server → service-registry → api-gateway → user-service → catalog-service.

```bash
# examples once projects exist
mvn spring-boot:run -pl platform/config-server
mvn spring-boot:run -pl platform/service-registry
mvn spring-boot:run -pl platform/api-gateway
mvn spring-boot:run -pl services/user-service
mvn spring-boot:run -pl services/catalog-service
```

## 3. Health & Metrics

- Health: `GET /actuator/health`
- Metrics: `GET /actuator/prometheus` (scraped by Prometheus)

## 4. Logs & Traces

- Logs: View in Grafana → Explore → Loki data source (filter by service/container).
- Traces: Open Jaeger UI; search by service.

## 5. Troubleshooting

- **No logs in Loki**: Promtail may not read Docker logs on your OS. Switch services to file appender and update `promtail-config.yml` paths.
- **Prometheus not scraping**: Check targets in `infra/prometheus.yml`; ensure `/actuator/prometheus` enabled.
- **JWT errors**: Confirm `security.jwt.secret` env var is set and identical for gateway & user-service.
- **DB conflicts**: Ensure port 5432 free; or change host port mapping in compose.

## 6. Maintenance

- **Rotate JWT secret** (dev): Restart services with new env var; invalidate old tokens by reducing TTL temporarily.
- **DB migrations**: New DDL changes go to Flyway scripts `V2__*.sql` per service.
- **Backups (local)**: Snapshot Docker volume (`pgdata`) if needed.
