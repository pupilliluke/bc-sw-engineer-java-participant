# Lab 42 — Outline Deployment Runbook

## Step 1 — Headings

`docs/deployment-runbook.md` carries, in order:

| Heading | Holds |
| --- | --- |
| Prereqs | k3d cluster `lab42`, kubeconfig location, `crm-api:lab41` present in the cluster image store |
| Apply order | the five objects in the order below |
| Verify probes | `rollout status`, pod Ready, readiness and liveness returning UP |
| Smoke CRM | login as `agent1`, then `GET /api/customers` for `CUS-1001` and `CUS-1002` |
| Rollback | `rollout history`, `rollout undo`, re-verify |
| Contacts | who to tell before and after a destructive step |

## Step 2 — Apply order

ConfigMap → Secret (out-of-band, never apply `secret.example.yaml`) → Deployment
→ Service → Ingress.

The Deployment reads both the ConfigMap and the Secret, so neither can be
created after it. Service before Ingress because the Ingress routes to a Service
that has to exist.

## Step 3 — Safety

Stop before destructive actions; instructor approval first. Destructive here
means `kubectl delete`, applying over another namespace, or anything touching a
cluster that is not the local k3d `lab42`. Kubeconfig and Secret values never go
into Git or a screenshot.

## Step 4 — Scope

This is an outline only. The full apply, smoke and rollback are Lab 42 work.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/lab42-runbook-outline.md`
- [x] Headings complete
- [x] Apply order stated
- [x] Pre-lab scope marked
