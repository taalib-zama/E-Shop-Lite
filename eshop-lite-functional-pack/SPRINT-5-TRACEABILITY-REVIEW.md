# Sprint 5 Traceability - Review One-Pager

**Project:** E-Shop Lite  
**Sprint:** Sprint 5 (Continuous Delivery and Kubernetes Lift)  
**Date:** 2026-03-26

## 1) Sprint Scope at a Glance
- **Stories:** `US-21` to `US-25`
- **Theme:** Containerise, deploy to K8s, CD pipeline, mTLS service mesh
- **Services:** all Sprint 1-4 services wrapped and deployed
- **Outcome:** production-style deployment baseline on free tooling

## 2) FR to US Mapping (Condensed)

| FR | Requirement | Story | Validation |
|---|---|---|---|
| `FR-S5-01` | Multi-stage images + GHCR CI | `US-21` | `TC-S5-IMG-*` |
| `FR-S5-02` | K8s manifests + K8s DNS | `US-22` | `TC-S5-K8S-*` |
| `FR-S5-03` | Ingress + gateway routing | `US-23` | `TC-S5-ING-*` |
| `FR-S5-04` | CD pipeline to Fly.io | `US-24` | `TC-S5-CD-*` |
| `FR-S5-05` | Linkerd mTLS + ServiceProfiles | `US-25` | `TC-S5-MESH-*` |

## 3) Architecture Transition Summary

| Layer | Before Sprint 5 | After Sprint 5 |
|---|---|---|
| Discovery | Eureka (local) | K8s DNS (staging); Eureka kept for local dev |
| Images | JAR only | Multi-stage Docker, tagged in GHCR |
| Deployment | Docker Compose | Kustomize K8s manifests + Docker Compose |
| Ingress | Gateway on host port | nginx-ingress -> api-gateway only |
| mTLS | None | Linkerd transparent mTLS |
| CD | Manual | GitHub Actions -> Fly.io |

## 4) Critical Quality Evidence
- Images: health probe smoke on all services
- K8s: `kubectl rollout status` all Deployments
- Ingress: Sprint 1-4 Postman smoke via Ingress hostname
- CD: clean pipeline run + secrets masked
- Mesh: `linkerd check` + `linkerd viz stat` 100% mTLS

## 5) DoD Status Snapshot

| DoD Item | Status |
|---|---|
| Images and CI validated | Planned/In Progress |
| K8s stack on Kind validated | Planned/In Progress |
| Ingress smoke green | Planned/In Progress |
| CD pipeline validated | Planned/In Progress |
| Linkerd mTLS verified | Planned/In Progress |

## 6) Top Risks and Next Actions
1. Validate Linkerd proxy overhead on Kind (target p99 `< 2ms` per hop).
2. Confirm Fly.io free tier is sufficient for all services (consider gateway-only deploy initially).
3. Ensure Resilience4j and Linkerd retry policies are tuned to avoid amplification.
4. Keep Docker Compose fully functional for devs not using K8s.

