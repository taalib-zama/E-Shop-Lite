# E-Shop Lite — Starter Skeleton

**Date:** 2026-03-17

## Modules
- platform/config-server (8888)
- platform/service-registry (Eureka 8761)
- platform/api-gateway (8080)
- services/user-service (8081)
- services/catalog-service (8082)

## Prerequisites
- Java 21, Maven 3.9+
- Docker + Docker Compose

## Infra
```bash
cd infra
docker compose up -d
```

## Run (new terminals or run configs)
```bash
mvn -q -pl platform/config-server spring-boot:run
mvn -q -pl platform/service-registry spring-boot:run
mvn -q -pl platform/api-gateway spring-boot:run
mvn -q -pl services/user-service spring-boot:run
mvn -q -pl services/catalog-service spring-boot:run
```

## JWT
Secret from env: `JWT_SECRET` (default dev-secret).

## Postman
Import `postman/E-Shop-Lite.postman_collection.json` and set `baseUrl=http://localhost:8080`.
