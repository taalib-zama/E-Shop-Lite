# Test Plan and Test Cases - Sprint 5

**Project:** E-Shop Lite  
**Version:** 1.0.0  
**Date:** 2026-03-26

---

## 1. Test Strategy
- **Image Smoke Tests:** container health probe after `docker run`.
- **K8s Deployment Tests:** rollout status, DNS resolution, service connectivity.
- **Ingress Smoke Tests:** full Postman suite via Ingress host.
- **CD Pipeline Tests:** pipeline idempotency and secrets masking verification.
- **Mesh Tests:** `linkerd check`, mTLS stat, ServiceProfile retry/timeout.

## 2. Test Environments and Data
- **Local:** Kind cluster with Kustomize local overlay.
- **Staging:** Free cloud host (Fly.io) via CD pipeline.
- **Seed:** Sprint 1-4 Postman collection reused for smoke.

## 3. Entry and Exit Criteria
- **Entry:** Sprint 4 baseline passes locally; Kind installed; GHCR access configured.
- **Exit:** All `TC-S5-*` green; Ingress smoke green; Linkerd check clean; CD deploys without manual steps.

## 4. Test Cases

### 4.1 Container Images
- **TC-S5-IMG-01** Image builds without error.
- **TC-S5-IMG-02** `docker run` health probe returns `200`.
- **TC-S5-IMG-03** Image tag includes Git SHA.

### 4.2 Kubernetes Deployment
- **TC-S5-K8S-01** `kubectl rollout status` passes for all Deployments.
- **TC-S5-K8S-02** K8s DNS resolution verified (service-to-service call succeeds without Eureka).
- **TC-S5-K8S-03** ConfigMap values applied correctly per overlay.

### 4.3 Ingress and Routing
- **TC-S5-ING-01** Full Sprint 1-4 Postman smoke suite passes via Ingress hostname.
- **TC-S5-ING-02** Sprint 4 `401/403` security cases remain intact through Ingress.
- **TC-S5-ING-03** No service is directly reachable outside cluster.

### 4.4 CD Pipeline
- **TC-S5-CD-01** Pipeline completes without manual intervention on clean main push.
- **TC-S5-CD-02** Secrets are masked in pipeline logs.
- **TC-S5-CD-03** Pipeline re-run is idempotent.

### 4.5 Service Mesh
- **TC-S5-MESH-01** `linkerd check` passes with no errors.
- **TC-S5-MESH-02** `linkerd viz stat deploy` shows 100% mTLS.
- **TC-S5-MESH-03** `ServiceProfile` retry policy fires under injected latency.

## 5. Reporting
- CI publishes image build and push summary.
- CD publishes deploy outcome and staging smoke result.
- `linkerd check --output short` captured in sprint evidence.

