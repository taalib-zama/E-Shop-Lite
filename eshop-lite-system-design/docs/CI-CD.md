# CI/CD Plan

**Date:** 2026-03-03

---

## 1. CI (GitHub Actions)

Pipeline stages:
1) Checkout
2) Setup JDK 21
3) Build & Unit Tests (`mvn -B verify`)
4) (Optional) Integration tests with Testcontainers
5) Publish test reports

## 2. Branching Strategy

- `main` — stable
- `feature/<topic>` — feature work
- `bugfix/<id>` — fixes
- `chore/<desc>` — docs/tooling

## 3. Quality Gates

- Unit coverage ≥ 70% on core
- Lint/static analysis (optional)
- OpenAPI updated for API changes

## 4. CD (Later)

- Container builds (Dockerfiles per service)
- Deploy to free hosts (Render/Railway/Fly.io) or local K8s (Kind/Minikube)
- Environment configs via Config Server + env vars
