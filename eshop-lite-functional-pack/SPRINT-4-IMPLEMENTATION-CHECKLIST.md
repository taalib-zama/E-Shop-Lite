# Sprint 4 Implementation Checklist (Day-by-Day)

**Project:** E-Shop Lite  
**Sprint:** Sprint 4 (Hardening and Operability)  
**Duration:** 10 working days  
**Date:** 2026-03-26

---

## 1) Sprint Goal
Deliver security, resilience, observability, reconciliation, and CI hardening for the end-to-end checkout platform.

---

## 2) Day-by-Day Execution Sheet

### Day 1 - Security Baseline Review
- [ ] Audit protected routes and policy drift between gateway/services
- [ ] Freeze baseline security matrix (public vs protected paths)
- [ ] Define negative-path test cases
**Validation:** `TC-S4-SEC-01` dry run

### Day 2 - Security Hardening Implementation (US-16)
- [ ] Apply route/policy fixes
- [ ] Standardize 401/403 payload behavior
- [ ] Add regression tests
**Validation:** `TC-S4-SEC-01..04`

### Day 3 - Resilience Profile Inventory
- [ ] Review current timeout/retry/circuit settings
- [ ] Define shared profile targets
- [ ] Align fallback behavior contracts
**Validation:** controlled failure simulation baseline

### Day 4 - Resilience Unification (US-17)
- [ ] Apply shared Resilience4j configs
- [ ] Validate fallback and breaker transitions
- [ ] Capture reliability metrics
**Validation:** `TC-S4-REL-01..03`

### Day 5 - Dashboards and Alert Rules (US-18)
- [ ] Build/update Grafana dashboards for critical reliability signals
- [ ] Add alert definitions for breaker-open, retry spikes, DLQ backlog, error rates
- [ ] Validate alert trigger path
**Validation:** `TC-S4-OBS-01`, `TC-S4-OBS-04`

### Day 6 - Reconciliation Job Core (US-19)
- [ ] Implement reconciliation scanner across order/payment/inventory states
- [ ] Generate mismatch report format
- [ ] Add audit logging and run metadata
**Validation:** `TC-S4-REC-01`

### Day 7 - Reconciliation Hardening (US-19)
- [ ] Implement idempotent rerun behavior
- [ ] Add mismatch categorization and action hints
- [ ] Add integration tests for drift scenarios
**Validation:** `TC-S4-REC-02`, `TC-S4-REC-03`

### Day 8 - CI Quality Gates (US-20)
- [ ] Add/align CI stages for integration + smoke + reliability checks
- [ ] Set gate thresholds and failure criteria
- [ ] Add gate summary artifact output
**Validation:** `TC-S4-CI-01..03`

### Day 9 - Observability and Traceability Closure
- [ ] Verify logs/traces across incident scenarios
- [ ] Ensure alert links and dashboards are documented
- [ ] Update Sprint 4 traceability evidence
**Validation:** `TC-S4-OBS-02`, `TC-S4-OBS-03`

### Day 10 - Hardening Freeze and Demo Readiness
- [ ] Run full Sprint 4 regression and smoke
- [ ] Finalize docs and evidence
- [ ] Prepare demo walk-through and known-issues register
**Validation:** all critical `TC-S4-*` green

---

## 3) Dependency and Risk Tracker

### Dependencies
- [ ] Sprint 3 baseline services and flows stable
- [ ] Observability stack healthy (Prometheus/Grafana/Loki/Jaeger)
- [ ] CI environment supports full gate stages

### Risks
- [ ] Alert noise from initial thresholds
- [ ] Reconciliation runtime overhead
- [ ] Policy mismatch between environments

---

## 4) End-of-Sprint Readiness Gate

### Functional
- [ ] US-16 complete
- [ ] US-17 complete
- [ ] US-18 complete
- [ ] US-19 complete
- [ ] US-20 complete

### Quality
- [ ] Critical `TC-S4-*` passed
- [ ] No unresolved blocker defects for hardening scope

### Operational
- [ ] Dashboard and alert baseline approved
- [ ] Reconciliation report reviewed
- [ ] CI gates enforced for release candidates

