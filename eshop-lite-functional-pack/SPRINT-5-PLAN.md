# Sprint 5 Plan - Continuous Delivery and Kubernetes Lift

**Project:** E-Shop Lite  
**Sprint:** Sprint 5  
**Date:** 2026-03-26

---

## 1. Sprint Goal
Containerise all services, ship a GitHub Actions CD pipeline to free cloud staging, migrate to Kubernetes + K8s DNS, and add a Linkerd service mesh baseline for mTLS and traffic policies.

## 2. Scope
### In Scope
- Multi-stage Docker images for all services (per-service `Dockerfile`)
- GitHub Actions CI (build + push to GHCR)
- GitHub Actions CD (deploy to Fly.io staging)
- Kubernetes manifests (Kustomize base + local/staging overlays)
- K8s DNS migration for Feign clients (Eureka disabled in K8s profile)
- Nginx Ingress exposing only `api-gateway`
- Linkerd service mesh baseline (mTLS + `ServiceProfile` policies)

### Out of Scope
- Istio / full service mesh migration
- Multi-region or multi-cluster deployments
- Paid cloud environments
- Canary releases or progressive delivery tooling this sprint

## 3. Story Set and Sizing
- US-21 Multi-Stage Docker Images and GHCR Registry (3)
- US-22 Kubernetes Manifests and K8s DNS Migration (8)
- US-23 Kubernetes Ingress and API Gateway Routing (5)
- US-24 CD Pipeline to Free Cloud Host (8)
- US-25 Service Mesh Baseline - Linkerd mTLS and Traffic Policies (5)

**Total:** 29 story points

## 4. Architecture Deltas
- Add `Dockerfile` per service/platform module
- Add `.github/workflows/ci.yml` and `.github/workflows/cd.yml`
- Add `infra/k8s/` with Kustomize base and overlays
- Add K8s `Ingress` manifest
- Disable Eureka in K8s profile (`spring.cloud.eureka.client.enabled=false`)
- Update `config-repo` routes to use K8s DNS URIs in staging overlay
- Annotate namespace and deployments for Linkerd injection
- Add `ServiceProfile` CRDs per service pair

## 5. Execution Plan (10 working days)
### Week 1
- **Day 1-2:** Dockerfiles + CI image build and GHCR push
- **Day 3-4:** K8s manifests, K8s DNS migration, Eureka disable
- **Day 5:** Ingress config and smoke validation through K8s

### Week 2
- **Day 6-7:** CD pipeline + free cloud staging deploy + secrets
- **Day 8-9:** Linkerd injection, ServiceProfiles, mTLS verification
- **Day 10:** Docs, traceability, smoke demo prep

## 6. Definition of Done (Sprint 5)
- All service images build and pass health probe smoke
- `kubectl apply -k infra/k8s/overlays/local` deploys full stack on Kind
- Postman smoke suite passes through K8s Ingress endpoint
- CD pipeline deploys to free staging without manual intervention
- Linkerd mTLS verified across S2S calls
- Sprint 5 traceability and docs updated

## 7. Risks and Mitigations
- **Risk:** K8s resource constraints on dev machine  
  **Mitigation:** use Kind with minimal replicas; keep Docker Compose intact for non-K8s dev.

- **Risk:** Free cloud plan limits  
  **Mitigation:** keep staging lightweight; one replica per service.

- **Risk:** Linkerd injection breaking existing health probes  
  **Mitigation:** verify readiness/liveness probes before injection.

## 8. Deliverables
- `Dockerfile` per service
- `.github/workflows/ci.yml`, `cd.yml`
- `infra/k8s/` Kustomize tree
- `docs/USER-STORIES-S5.md`
- `docs/TEST-PLAN-S5.md`
- `SPRINT-5-TRACEABILITY.md`
- `SPRINT-5-TRACEABILITY-REVIEW.md`
- `SPRINT-5-IMPLEMENTATION-CHECKLIST.md`
- `US-21.md` to `US-25.md`

