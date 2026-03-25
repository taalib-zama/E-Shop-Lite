# Sprint 4 Traceability Matrix

**Project:** E-Shop Lite  
**Sprint:** Sprint 4 (Hardening and Operability)  
**Version:** 1.0.0  
**Date:** 2026-03-26

---

## 1. Purpose and Scope
This document maps Sprint 4 requirements to stories, APIs/flows, and test coverage.

Source references:
- `eshop-lite-functional-pack/SPRINT-4-PLAN.md`
- `eshop-lite-functional-pack/docs/USER-STORIES-S4.md`
- `eshop-lite-functional-pack/docs/TEST-PLAN-S4.md`

---

## 2. Requirement to User Story Mapping

| FR ID | Requirement Summary | Story | API/Flow |
|---|---|---|---|
| FR-S4-01 | Harden authentication and authorization controls | US-16 | Gateway + service protected routes |
| FR-S4-02 | Standardize resilience profile across dependencies | US-17 | Feign/resilience configs + fallback paths |
| FR-S4-03 | Operational dashboards and alerting baseline | US-18 | Prometheus/Grafana/Loki/Jaeger observability paths |
| FR-S4-04 | Detect and report cross-service state drift | US-19 | Reconciliation scheduled job and report output |
| FR-S4-05 | Enforce CI quality gates before release | US-20 | Pipeline gate stages and smoke checks |

---

## 3. Story Traceability Matrix

| US ID | Outcome / Acceptance Intent | FR Link(s) | Validation IDs |
|---|---|---|---|
| US-16 | Consistent 401/403 enforcement and security policy behavior | FR-S4-01 | `TC-S4-SEC-01..04` |
| US-17 | Predictable retries/breakers/fallback with shared profiles | FR-S4-02 | `TC-S4-REL-01..03` |
| US-18 | Actionable dashboards and alert signals | FR-S4-03 | `TC-S4-OBS-01..04` |
| US-19 | Reconciliation detects mismatches and supports reruns | FR-S4-04 | `TC-S4-REC-01..03` |
| US-20 | CI blocks failing builds and reports outcomes clearly | FR-S4-05 | `TC-S4-CI-01..03` |

---

## 4. Cross-Cutting Quality Evidence

| Area | Expected Evidence |
|---|---|
| Security | Authn/authz policy outcomes and error payload consistency |
| Resilience | Retry/circuit metrics and fallback behavior in traces |
| Observability | Metrics/logs/traces + alert triggers |
| Data Integrity | Reconciliation mismatch report and rerun logs |
| Delivery Quality | CI gate pass/fail evidence and smoke results |

---

## 5. DoD Evidence Map

| DoD Item | Evidence Source | Status |
|---|---|---|
| Security hardening verified | `TC-S4-SEC-*` outputs | Planned/In Progress |
| Resilience profile verified | `TC-S4-REL-*` outputs | Planned/In Progress |
| Dashboards/alerts verified | `TC-S4-OBS-*` outputs | Planned/In Progress |
| Reconciliation checks verified | `TC-S4-REC-*` outputs | Planned/In Progress |
| CI gates enforced | `TC-S4-CI-*` outputs | Planned/In Progress |

---

## 6. Sign-Off Checklist
- [ ] All critical `TC-S4-*` cases pass
- [ ] Dashboards and alerts validated with sample incidents
- [ ] Reconciliation report generated and reviewed
- [ ] CI gates and smoke checks green
- [ ] Traceability artifacts updated

