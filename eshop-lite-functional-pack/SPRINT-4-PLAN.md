# Sprint 4 Plan - Hardening and Operability

**Project:** E-Shop Lite  
**Sprint:** Sprint 4  
**Date:** 2026-03-26

---

## 1. Sprint Goal
Harden the end-to-end checkout platform (Sprints 1-3 scope) for production-style reliability: stronger security controls, measurable resilience, better observability, reconciliation safeguards, and CI quality gates.

## 2. Scope
### In Scope
- Security hardening for gateway and service endpoints
- Resilience policy standardization across S2S calls and async flows
- Observability dashboards and alerting baseline
- Reconciliation tooling for order/payment/inventory consistency checks
- CI quality gates (tests, coverage, contract checks, smoke)

### Out of Scope
- New business domains (refunds, returns, subscriptions)
- Major architecture migration to Kubernetes/service mesh
- New external provider integrations beyond current mock/stub approach

## 3. Story Set and Sizing
- US-16 Security Hardening and Access Controls (8)
- US-17 Resilience Policy Unification (5)
- US-18 Observability Dashboards and Alerts (5)
- US-19 Reconciliation and Drift Detection (8)
- US-20 CI Quality Gates and Release Readiness (3)

**Total:** 29 story points

## 4. Architecture Deltas
- Tighten gateway security filters and route policies
- Standardize Resilience4j policy profiles across services
- Add structured operational metrics for retries, DLQ backlog, compensation lag
- Add reconciliation job (scheduled) and report endpoint/artifact
- Add CI stages for integration, async reliability, and smoke gates

## 5. Execution Plan (10 working days)
### Week 1
- **Day 1-2:** Security baseline and route policy hardening
- **Day 3-4:** Resilience policy unification and fallback validation
- **Day 5:** Observability dashboards + alert definitions

### Week 2
- **Day 6-7:** Reconciliation job and drift report
- **Day 8-9:** CI quality gates and release checks
- **Day 10:** Final hardening, traceability updates, demo prep

## 6. Definition of Done (Sprint 4)
- Security controls validated on all protected paths
- Resilience policies applied consistently and verified
- Dashboards/alerts available for critical reliability signals
- Reconciliation checks detect and report inconsistencies
- CI gates enforce quality before release candidates
- Sprint 4 smoke suite green

## 7. Risks and Mitigations
- **Risk:** Over-hardening causes false positives/blocking traffic  
  **Mitigation:** staged rollout of policies and explicit allowlists.

- **Risk:** Alert fatigue from noisy thresholds  
  **Mitigation:** tune thresholds with baseline data before strict paging.

- **Risk:** Reconciliation load impacts runtime performance  
  **Mitigation:** scheduled off-peak execution and bounded scans.

## 8. Deliverables
- `docs/USER-STORIES-S4.md`
- `docs/TEST-PLAN-S4.md`
- `SPRINT-4-TRACEABILITY.md`
- `SPRINT-4-TRACEABILITY-REVIEW.md`
- `SPRINT-4-IMPLEMENTATION-CHECKLIST.md`
- Optional detailed stories: `US-16.md` to `US-20.md`

