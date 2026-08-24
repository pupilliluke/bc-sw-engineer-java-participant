# Lab 42 — ConfigMap vs Secret Split

## Step 1 — Sort list

Sort: `CRM_DB_HOST`, `CRM_DB_NAME`, `CRM_DB_USER`, `CRM_DB_PASSWORD`,
`SPRING_PROFILES_ACTIVE`, log level.

## Step 2 — Check the reference

Secret data is created out-of-band; Git only gets `secret.example.yaml` without
values. Never `kubectl apply` the example file.

## Step 3 — CRM fixtures

`CUS-1001` (Amina Khan, ACTIVE) and `CUS-1002` (Ravi Singh, PROSPECT) are rows
seeded by `FixtureLoader` at startup, idempotent by `public_id`. They are
application data, not Kubernetes config keys, and don't belong in ConfigMap.

## Step 4 — Write table

| ConfigMap (non-secret) | Secret (sensitive) |
| --- | --- |
| `CRM_DB_HOST` | `CRM_DB_PASSWORD` |
| `CRM_DB_NAME` | |
| `CRM_DB_USER` | |
| `SPRING_PROFILES_ACTIVE` | |
| log level | |

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/lab42-config-vs-secret.md`
- [x] Every setting classified
- [x] secret.example pattern stated
- [x] Fixtures not in ConfigMap
