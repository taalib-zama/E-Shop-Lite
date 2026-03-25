# Test Plan and Test Cases - Sprint 4

**Project:** E-Shop Lite  
**Version:** 1.0.0  
**Date:** 2026-03-26

---

## 1. Test Strategy
- **Security Tests:** authentication/authorization, route policy checks, error consistency.
- **Resilience Tests:** timeout/retry/circuit-breaker/fallback profile verification.
- **Observability Tests:** metrics, logs, traces, and alert rule validation.
- **Reconciliation Tests:** mismatch detection, report generation, idempotent reruns.
- **CI Gate Tests:** quality threshold checks and smoke gating.

## 2. Environments and Data
- Local/dev stack with Postgres, RabbitMQ, Prometheus, Grafana, Loki, Jaeger.
- Seed data including known-consistent and intentionally inconsistent records for reconciliation.

## 3. Entry and Exit Criteria
- **Entry:** Sprint 3 baseline flows available and stable.
- **Exit:** critical `TC-S4-*` tests green, dashboards/alerts validated, CI gates enforced.

## 4. Test Cases

### 4.1 Security Hardening
- **TC-S4-SEC-01** Missing token on protected route -> `401`.
- **TC-S4-SEC-02** Invalid token -> `401`.
- **TC-S4-SEC-03** Insufficient role -> `403`.
- **TC-S4-SEC-04** Error payload consistency on auth failures.

### 4.2 Resilience Profile
- **TC-S4-REL-01** Timeout triggers configured retries.
- **TC-S4-REL-02** Circuit breaker opens after threshold breaches.
- **TC-S4-REL-03** Fallback preserves recoverable workflow state.

### 4.3 Observability and Alerts
- **TC-S4-OBS-01** Metrics exposed for retries/breakers/DLQ/reconciliation.
- **TC-S4-OBS-02** Logs include correlation fields for incident flows.
- **TC-S4-OBS-03** Traces show cross-service path with fallback spans.
- **TC-S4-OBS-04** Alert rules trigger on simulated threshold breach.

### 4.4 Reconciliation
- **TC-S4-REC-01** Reconciliation detects known mismatch.
- **TC-S4-REC-02** Reconciliation report includes actionable details.
- **TC-S4-REC-03** Reconciliation rerun is idempotent.

### 4.5 CI Quality Gates
- **TC-S4-CI-01** Gate blocks on failing integration tests.
- **TC-S4-CI-02** Gate blocks on failing smoke tests.
- **TC-S4-CI-03** Gate summary artifact published.

## 5. Reporting
- CI publishes S4 test summaries by category (`SEC`, `REL`, `OBS`, `REC`, `CI`).
- Sprint demo includes dashboard snapshots and reconciliation report sample.

