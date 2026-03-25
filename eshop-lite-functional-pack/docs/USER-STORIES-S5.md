# User Stories - Sprint 5 (Continuous Delivery and Kubernetes Lift)

**Project:** E-Shop Lite  
**Version:** 1.0.0  
**Date:** 2026-03-26

---

## Epic Alignment
- EP-13 Containerisation and Registry
- EP-14 Kubernetes and Service Discovery Migration
- EP-15 Continuous Delivery and Staging
- EP-16 Service Mesh and Traffic Policies

## Definitions
- **DoR:** Dockerfile drafted, K8s resource shapes agreed, secrets inventory complete, estimated.
- **DoD:** Images build and pass health smoke; K8s manifests apply cleanly; CD pipeline is idempotent; mTLS verified; docs and traceability updated.

---

## US-21 Multi-Stage Docker Images and GHCR Registry (3 pts)
**As a** DevOps engineer **I want** lean production images **so that** all services can be reliably containerised and versioned.

**Acceptance Criteria (Gherkin)**
```gherkin
Given a service source module
When the multi-stage Dockerfile is built
Then a runnable JRE-based image is produced
And the image passes a health-probe smoke check
And CI pushes a versioned image to GHCR on merge to main
```

**Tasks**
- Multi-stage Dockerfile (JDK builder + JRE runtime) per service
- GitHub Actions `ci.yml` build and push to GHCR
- Image tag uses `<version>-<git-sha>`
- Update docker-compose `build:` contexts

---

## US-22 Kubernetes Manifests and K8s DNS Migration (8 pts)
**As a** DevOps engineer **I want** Kubernetes manifests **so that** the full stack deploys on Kind with K8s DNS.

**Acceptance Criteria**
```gherkin
Given infra/k8s Kustomize tree
When kubectl apply -k infra/k8s/overlays/local
Then all Deployments reach Ready state
And Feign clients resolve via K8s DNS without Eureka
And ConfigMaps supply Spring profile per environment
```

**Tasks**
- Kustomize `base/` (Deployment + Service per module)
- `overlays/local/` for Kind (Eureka enabled optional)
- `overlays/staging/` for free cloud (Eureka disabled, K8s DNS URIs)
- ConfigMaps and Secrets references per overlay
- `spring.cloud.eureka.client.enabled=false` in K8s profile

---

## US-23 Kubernetes Ingress and API Gateway Routing (5 pts)
**As a** DevOps engineer **I want** a single Ingress entry point **so that** only the gateway is exposed and all routes remain functional.

**Acceptance Criteria**
```gherkin
Given the K8s Ingress manifest
When traffic arrives on the Ingress hostname
Then it routes to api-gateway only
And all Sprint 1 to 4 Postman smoke cases pass
And no service ports are directly exposed outside the cluster
```

**Tasks**
- `ingress.yaml` manifest (nginx-ingress or Traefik)
- Route annotations and TLS stub for staging
- Validate smoke suite through Ingress
- Sprint 4 auth/security cases still pass

---

## US-24 CD Pipeline to Free Cloud Host (8 pts)
**As a** DevOps engineer **I want** an automated CD pipeline **so that** staging is updated on every main merge without manual steps.

**Acceptance Criteria**
```gherkin
Given a merge to main
When the cd.yml workflow runs
Then images are built, tagged, and deployed to free staging
And environment secrets are sourced from GitHub Actions secrets
And the pipeline is idempotent on re-run
```

**Tasks**
- `.github/workflows/cd.yml` targeting Fly.io staging
- Secrets management (JWT_SECRET, DB URLs, GHCR token)
- Rollback/re-run validation
- Staging smoke gate in pipeline

---

## US-25 Service Mesh Baseline - Linkerd mTLS and Traffic Policies (5 pts)
**As a** DevOps engineer **I want** a Linkerd mesh baseline **so that** S2S calls are mTLS-protected and traffic policies are declarative.

**Acceptance Criteria**
```gherkin
Given Linkerd installed and namespace annotated
When all Deployments are injected
Then linkerd check passes
And linkerd viz stat deploy shows 100% mTLS
And ServiceProfile policies apply retry and timeout rules
```

**Tasks**
- Namespace and Deployment Linkerd annotations
- `ServiceProfile` CRDs per service pair
- Validate with `linkerd viz tap` and `linkerd check`
- Coexistence with Resilience4j (complementary, not duplicate)

---

## Sprint 5 Definition of Done (recap)
- All service images build and pass health smoke
- Full stack deploys on Kind via `kubectl apply -k`
- Postman smoke passes through Ingress
- CD pipeline deploys to staging on every main merge
- Linkerd mTLS verified and `linkerd check` clean
- Traceability and docs updated

