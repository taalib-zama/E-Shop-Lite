# Sprint 5 Implementation Checklist (Day-by-Day)

**Project:** E-Shop Lite  
**Sprint:** Sprint 5 (Continuous Delivery and Kubernetes Lift)  
**Duration:** 10 working days  
**Date:** 2026-03-26

---

## 1) Sprint Goal
Containerise all services, ship a GitHub Actions CD pipeline to Fly.io free staging, deploy on Kind with K8s DNS, and add Linkerd mTLS mesh baseline.

---

## 2) Pre-Sprint Prerequisites
- [ ] Kind installed locally (`kind create cluster`)
- [ ] `kubectl` and `kustomize` available
- [ ] Fly.io free account and `flyctl` CLI installed
- [ ] GitHub repository with Actions enabled
- [ ] GHCR personal access token available

---

## 3) Day-by-Day Execution Sheet

### Day 1 - Dockerfile Baseline (US-21)
- [ ] Write `Dockerfile` for `user-service` and `catalog-service` (validate pattern)
- [ ] Add `.dockerignore` files
- [ ] Local `docker build` and health-probe smoke

**Validation:** `TC-S5-IMG-01`, `TC-S5-IMG-02` partial

---

### Day 2 - Remaining Dockerfiles + GitHub Actions CI (US-21)
- [ ] Write `Dockerfile` for all remaining services and platform modules
- [ ] Add `.github/workflows/ci.yml` (build + push to GHCR)
- [ ] Validate image tag includes Git SHA
- [ ] Update `docker-compose.yml` build contexts

**Validation:** `TC-S5-IMG-01..03` full pass

---

### Day 3 - Kustomize Base Manifests (US-22)
- [ ] Create `infra/k8s/base/` with Deployments and Services per module
- [ ] Add `namespace.yaml` and `kustomization.yaml`
- [ ] Add `ConfigMap` for Spring profiles
- [ ] Add secrets template (reference only, no real values)

**Validation:** `kubectl kustomize infra/k8s/base` renders without error

---

### Day 4 - K8s DNS Migration + Overlays (US-22)
- [ ] Add `application-k8s.yml` per service (Eureka disabled)
- [ ] Create `overlays/local/` (Eureka optional) and `overlays/staging/` (K8s DNS URIs)
- [ ] Apply to Kind cluster: `kubectl apply -k infra/k8s/overlays/local`
- [ ] Validate all Deployments reach `Ready`

**Validation:** `TC-S5-K8S-01..03` pass

---

### Day 5 - Ingress Setup and Smoke (US-23)
- [ ] Install nginx-ingress on Kind via Helm
- [ ] Add `ingress.yaml` to Kustomize base
- [ ] Add `/etc/hosts` entry for `eshop-lite.local`
- [ ] Run full Sprint 1-4 Postman smoke via Ingress hostname

**Validation:** `TC-S5-ING-01..03` pass

---

### Day 6 - Fly.io Config and Staging Prep (US-24)
- [ ] Create `fly.toml` files under `infra/fly/`
- [ ] Configure Fly.io apps for api-gateway and required services
- [ ] Set up GitHub Actions encrypted secrets (`FLY_API_TOKEN`, `JWT_SECRET`, DB URLs)
- [ ] Add `cd.yml` workflow skeleton

**Validation:** `fly launch` dry-run succeeds

---

### Day 7 - CD Pipeline End-to-End (US-24)
- [ ] Complete `cd.yml` with build, push, deploy, and staging smoke gate
- [ ] Trigger on push to `main` and validate end-to-end
- [ ] Confirm secrets masked in pipeline logs
- [ ] Test idempotent re-run

**Validation:** `TC-S5-CD-01..03` pass

---

### Day 8 - Linkerd Installation and Injection (US-25)
- [ ] Install Linkerd CRDs and control plane on Kind
- [ ] Annotate `eshop-lite` namespace
- [ ] Restart Deployments to trigger injection
- [ ] Validate health probes still pass after injection

**Validation:** `linkerd check` clean; `TC-S5-MESH-01`

---

### Day 9 - ServiceProfiles and mTLS Verification (US-25)
- [ ] Add `ServiceProfile` CRDs for key service pairs
- [ ] Run `linkerd viz stat deploy` and confirm 100% mTLS
- [ ] Simulate latency and confirm retry policy fires
- [ ] Validate Resilience4j and Linkerd coexistence

**Validation:** `TC-S5-MESH-01..03` pass

---

### Day 10 - Docs, Final Smoke, and Demo Prep
- [ ] Final regression and smoke run (local + staging)
- [ ] Update all Sprint 5 traceability evidence
- [ ] Add pipeline status badge to `README.md`
- [ ] Prepare demo: Docker build, Kind deploy, Ingress smoke, CD run, Linkerd Viz

**Validation:** All `TC-S5-*` green; sprint readiness gate complete

---

## 4) Dependency and Risk Tracker

### Dependencies
- [ ] Sprint 4 baseline passes locally before K8s work starts
- [ ] Kind cluster ready and sufficient memory allocated (min 8GB)
- [ ] Fly.io account and flyctl configured before Day 6
- [ ] GitHub Actions quota sufficient for CI/CD runs

### Risks
- [ ] Linkerd proxy overhead on constrained dev machine
- [ ] Fly.io free tier limits for multi-service deployment
- [ ] Resilience4j + Linkerd retry amplification

---

## 5) End-of-Sprint Readiness Gate

### Functional
- [ ] US-21 complete (images + CI)
- [ ] US-22 complete (K8s + DNS)
- [ ] US-23 complete (Ingress + smoke)
- [ ] US-24 complete (CD pipeline)
- [ ] US-25 complete (Linkerd mTLS)

### Quality
- [ ] All `TC-S5-*` passed
- [ ] Sprint 1-4 Postman smoke green via Ingress

### Operational
- [ ] CD pipeline run evidence captured
- [ ] `linkerd check` output clean
- [ ] README pipeline badge live

### Release Readiness
- [ ] Demo scenario runs end-to-end
- [ ] Known issues documented with owners

