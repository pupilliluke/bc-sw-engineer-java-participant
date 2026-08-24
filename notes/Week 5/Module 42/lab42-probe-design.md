# Lab 42 — Design Three Probes

## Step 1 — Definitions

Startup: gives a slow boot time to finish before the other probes are allowed to
judge it, so a cold start is not mistaken for a wedged process.

Readiness: says whether this pod should receive traffic right now; failing it
removes the pod from the Service endpoints without killing it.

Liveness: says whether the process is still working at all; failing it restarts
the container.

## Step 2 — Check the reference

The three must not all point at the same shallow endpoint. Readiness is the one
that has to reflect the database, because a pod that cannot reach PostgreSQL can
still answer a liveness check.

Spring Boot's default readiness group contains `readinessState` only, so
`/actuator/health/readiness` reports UP even with the datasource down. Making
readiness reflect the dependency needs the group widened:

```
management:
  endpoint:
    health:
      group:
        readiness:
          include: readinessState,db
```

Lab 41 covers only the boot case: an unreachable database cancels the context
refresh at Flyway and the container exits 1, so no probe ever answers. Losing
the database after a successful start is the case this group setting changes.

## Step 3 — Paths

| Probe | Path | Settings |
| --- | --- | --- |
| startup | `/actuator/health/readiness` | `failureThreshold: 30`, `periodSeconds: 2` |
| readiness | `/actuator/health/readiness` | `periodSeconds: 10`, `failureThreshold: 3` |
| liveness | `/actuator/health/liveness` | `periodSeconds: 10`, `failureThreshold: 3` |

Startup is given 60 seconds. Lab 41 measured `Started CrmApplication` at 9.11
seconds and set the container HEALTHCHECK `start-period` to 40s.

## Step 4 — Failure story

Readiness failing while liveness stays up means the container is not restarted.
The pod is dropped from the Service endpoints, so no new request reaches it. At
`replicas: 1` the Service has no endpoints at all and Traefik returns 503 —
agents see the CRM as down but the pod stays running with its logs intact for
diagnosis. With more replicas the traffic simply moves to the ready pods and
agents see nothing.

Liveness failing instead restarts the container, which discards that state and
helps only if the process is genuinely wedged.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [x] File exists at `notes/lab42-probe-design.md`
- [x] Three probes defined
- [x] Paths proposed
- [x] Readiness failure impact stated
