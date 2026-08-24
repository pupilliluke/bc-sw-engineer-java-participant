# Lab 42 — Rollout and Rollback Checklist

## Step 1 — Rollout watch

1. `kubectl rollout status deployment/crm-api --timeout=180s`
2. `kubectl get pods -l app=crm-api` — pod Ready 1/1
3. Host-header Ingress check on `:8088`, since Traefik routes on Host and the
   request goes to loopback:

```
curl -s -o /dev/null -w '%{http_code}\n' \
  -H "Host: crm-api.localhost" \
  http://127.0.0.1:8088/actuator/health/readiness
```

4. `GET /api/customers`. Every `/api/customers` route requires ROLE_AGENT or
   ROLE_ADMIN and basic auth is disabled, so the call is login first, then the
   GET with the returned token:

```
curl -s -X POST http://127.0.0.1:8088/api/auth/login \
  -H "Host: crm-api.localhost" \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: lab-request-001" \
  -d '{"username":"agent1","password":"agent1"}'

curl -s http://127.0.0.1:8088/api/customers \
  -H "Host: crm-api.localhost" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Correlation-Id: lab-request-001"
```

Expect `CUS-1001` ACTIVE and `CUS-1002` PROSPECT. Without the token the same
call is 401.

## Step 2 — Check the reference

Rollback rehearses a bad image tag then `rollout undo` to `crm-api:lab41`.

1. `crm-api:lab41` is revision 1.
2. `kubectl set image deployment/crm-api crm-api=crm-api:bad` creates revision 2.
   The tag is not in the cluster image store, so the pod does not become Ready
   and `rollout status` runs to its timeout.
3. `kubectl rollout history deployment/crm-api` — confirm two revisions.
4. `kubectl rollout undo deployment/crm-api` — back to `crm-api:lab41`.
5. Re-run the Step 1 checks after the undo.

A Deployment on revision 1 alone has nothing to undo to, so the bad revision has
to exist before the rehearsal.

## Step 3 — Evidence

Under `notes/screenshots/lab-42/`:

| Folder | Holds |
| --- | --- |
| `before-rollback/` | `rollout status` and pod Ready on revision 1, smoke 200 |
| `bad-revision/` | `rollout status` timing out, pod not Ready, `rollout history` |
| `after-rollback/` | `rollout undo` output, pod Ready again, smoke 200 |

## Step 4 — Correlation

Header `X-Correlation-Id: lab-request-001` goes on every smoke call in the
checklist, before and after the undo.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/lab42-rollout-rollback.md`
- [x] Rollout checks listed
- [x] Undo rehearsal included
- [x] Correlation header noted
