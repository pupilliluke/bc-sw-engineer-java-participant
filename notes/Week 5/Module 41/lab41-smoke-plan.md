# Lab 41 — Plan Container Smoke

## Step 1 — Steps

| # | Step | Pass condition |
| - | --- | --- |
| 1 | `docker compose up -d` for postgres, then run the image | container reaches running |
| 2 | poll `/actuator/health/readiness` | 200 with `"status":"UP"` |
| 3 | `POST /api/auth/login` as agent1 | 200 with a bearer token |
| 4 | `GET /api/customers/CUS-1001` with that token and `X-Correlation-Id: lab-request-001` | 200, Amina Khan, ACTIVE |
| 5 | `GET /api/customers/page?status=ACTIVE&size=20` | 200, a bounded page |
| 6 | `docker inspect` the running container | user is 10001, not root |
| 7 | `docker stop` | exits 0 within the grace period, no severed request |

readiness before anything else, and polled rather than slept on. a fixed
`sleep 30` is either too short on a slow machine or wasted time on a fast
one, and it turns a flake into a mystery.

step 3 exists because every customer endpoint requires a token —
`SecurityConfig` — so a smoke test that skips login gets 401 and
proves only that security is on. the token has to come from the running
container, not from a test fixture, or the smoke is not end to end.

step 4 is the one that proves the whole chain: the JAR in the image, the
JVM in the runtime stage, the datasource URL supplied at run time, the
network hop to the postgres container, Flyway's schema, the JPA mapping,
and the fixture row. one 200 covers all of it.

## Step 2 — Check the reference

evidence as plain text under `notes/screenshots/lab-41/`: the build
context size before and after `.dockerignore`, `docker images` for the
final size, the inspect output showing UID 10001, and the smoke
transcript.

no production dumps and no environment dumps. `docker inspect` on a
running container prints its environment, which holds exactly the values
lab 40 worked to keep out of the image — so the inspect evidence is
filtered to the user and the image fields rather than pasted whole.

the correlation id `lab-request-001` appears in the request headers and
should appear in the container logs beside it. that pairing is the thing
that makes the transcript reproducible by someone else.

## Step 3 — Failure case

wrong `SPRING_DATASOURCE_URL`, run deliberately.

expected: the container starts, liveness passes, **readiness fails**, and
`GET /api/customers/CUS-1001` never gets a chance to return a confusing
error, because the container is not ready to receive it.

that split is the point of having two probes. the process is healthy; it
simply cannot serve. restarting it would not help, and in lab 42 failing
readiness takes the pod out of the load balancer while failing liveness
would kill it pointlessly.

a second negative worth running while the first is set up: start with
`JWT_SECRET` unset. the app should fail startup outright with
`Could not resolve placeholder`, which is lab 40's fix behaving as
intended inside a container — loud misconfiguration rather than a silent
insecure default.

## Step 4 — Scope line

this is a plan. no image is built, no container is run, no evidence is
captured in the pre-lab. the build, the run and the transcript are lab
41's timed and full paths.

two things recorded in lab41-health-resources.md would have stopped step
2 of this plan passing at all, and both are now fixed in lab40-crm:
`probes.enabled` was unset, so `/actuator/health/readiness` returned 404,
and `SecurityConfig` permitted `/actuator/health` as an exact match, so
the readiness sub-path returned 401. `ProbeEndpointsTest` covers both. a
HEALTHCHECK failing for either reason looks identical to a broken
application, which is why they were worth finding before the lab rather
than during it.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab41-smoke-plan.md`
- [ x ] Happy path ordered
- [ x ] Negative readiness case listed
- [ x ] Pre-lab scope stated
