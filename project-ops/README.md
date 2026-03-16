# GitHub Project Board & Issue Templates — Bootstrap Pack

**Generated:** 2026-03-10

This folder helps you quickly spin up a **GitHub classic Project board**, labels, milestones, and standardized **Issue/PR templates** for the E‑Shop Lite project.

## What’s included
- `.github/ISSUE_TEMPLATE/` → user story, task, bug, and config
- `.github/pull_request_template.md` → PR checklist
- `project-ops/labels.json` → label seeds
- `project-ops/milestones.json` → milestones for M0..M4
- `project-ops/setup_github_project.sh` → script to create a **classic** board and columns via GitHub CLI

> Note: **GitHub Projects (Beta/Next)** is configured via the web UI or GraphQL; this pack creates a **classic** board programmatically for speed.

## How to use
1. Ensure you have the GitHub CLI and jq installed:
   ```bash
   gh --version
   jq --version
   ```
2. Authenticate:
   ```bash
   gh auth login
   ```
3. From your repository root, run:
   ```bash
   bash project-ops/setup_github_project.sh "E‑Shop Lite Board"
   ```
4. Create issues using the templates (User Story / Task / Bug). They will appear in **Backlog**.

## Recommended Columns & Flow
```
Backlog → Ready for Development → In Progress → In Review → QA Ready → QA In Progress → Done
(Use Blocked as a parking lane when needed)
```

## Labels
- `story`, `task`, `bug`, `documentation`, `infra`, `platform`
- `service:*` per microservice
- `priority:*` for triage

## Milestones
- **M0 – Foundation** → Sprint 0
- **M1 – Identity & Catalog** → Sprint 1
- **M2 – Orders & Inventory** → Sprint 2
- **M3 – Payments & Notifications** → Sprint 3
- **M4 – Hardening & Observability** → Sprint 4
