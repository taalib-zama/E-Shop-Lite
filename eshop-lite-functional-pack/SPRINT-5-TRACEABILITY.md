# Sprint 5 Traceability Matrix

**Project:** E-Shop Lite  
**Sprint:** Sprint 5 (Continuous Delivery and Kubernetes Lift)  
**Version:** 1.0.0  
**Date:** 2026-03-26

---

## 1. Purpose and Scope
End-to-end traceability for Sprint 5 covering containerisation, K8s deployment, Ingress, CD pipeline, and service mesh.

Source references:
- `eshop-lite-functional-pack/SPRINT-5-PLAN.md`
- `eshop-lite-functional-pack/docs/USER-STORIES-S5.md`
- `eshop-lite-functional-pack/docs/TEST-PLAN-S5.md`
- `eshop-lite-sprint-details/docs/ARCHITECTURE.md`
- `eshop-lite-sprint-details/docs/SYSTEM-DESIGN-ADDENDUM.md`

---

## 2. Requirement to User Story Mapping

| FR ID | Requirement Summary | Story | Primary Flow |
|---|---|---|---|
| FR-S5-01 | Multi-stage images built, tagged, pushed to GHCR on CI | US-21 | Docker build -> GHCR |
| FR-S5-02 | Full stack deploys on Kind with K8s DNS, Eureka disabled in K8s overlay | US-22 | `kubectl apply -k` |
| FR-S5-03 | Single Ingress exposes only gateway; all routes pass | US-23 | Ingress -> api-gateway |
| FR-S5-04 | CD pipeline deploys to free staging on main merge; idempotent | US-24 | GitHub Actions -> Fly.io |
| FR-S5-05 | Linkerd injection, mTLS 100%, ServiceProfiles applied | US-25 | Linkerd mesh |

---

## 3. Story Traceability Matrix

| US ID | Outcome | FR | Architecture Components | Core Test IDs |
|---|---|---|---|---|
| US-21 | Versioned images pass health probe; CI pushes to GHCR | FR-S5-01 | Docker multi-stage, GHCR, GitHub Actions | `TC-S5-IMG-01..03` |
| US-22 | Kind stack fully ready; K8s DNS resolves; Eureka off | FR-S5-02 | Kind, Kustomize, ConfigMaps, K8s DNS | `TC-S5-K8S-01..03` |
| US-23 | Smoke suite passes via Ingress; no direct port exposure | FR-S5-03 | nginx-ingress, api-gateway, Postman | `TC-S5-ING-01..03` |
| US-24 | CD deploys staging end-to-end; secrets masked | FR-S5-04 | GitHub Actions, Fly.io, GHCR | `TC-S5-CD-01..03` |
| US-25 | `linkerd check` clean; mTLS 100%; ServiceProfiles active | FR-S5-05 | Linkerd, ServiceProfile CRDs, Linkerd Viz | `TC-S5-MESH-01..03` |

---

## 4. Architecture Alignment

| Component | Sprint 5 Change | Notes |
|---|---|---|
| Service discovery | Eureka -> K8s DNS (staging overlay) | Docker Compose/Eureka kept for local dev |
| Routing | lb:// Feign -> K8s DNS URIs in K8s profile | No business logic change |
| Ingress | nginx-ingress or Traefik | Only api-gateway exposed externally |
| mTLS | Linkerd injection | Transparent to application code |
| CD | GitHub Actions + Fly.io | Secrets from GitHub encrypted secrets |

---

## 5. DoD Evidence Map

| DoD Item | Evidence | Status |
|---|---|---|
| Images build and pass health smoke | `TC-S5-IMG-*` | Planned/In Progress |
| Kind stack deploys cleanly | `TC-S5-K8S-*` | Planned/In Progress |
| Smoke suite passes via Ingress | `TC-S5-ING-*` | Planned/In Progress |
| CD pipeline idempotent | `TC-S5-CD-*` | Planned/In Progress |
| Linkerd mTLS verified | `TC-S5-MESH-*` | Planned/In Progress |

---

## 6. Sign-Off Checklist
- [ ] All `TC-S5-*` tests pass
- [ ] `linkerd check` output captured
- [ ] CD pipeline run evidence attached
- [ ] Postman smoke via Ingress recorded
- [ ] Kustomize overlays committed

