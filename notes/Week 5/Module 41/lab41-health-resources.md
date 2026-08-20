# Lab 41 — Health and Resource Checklist

## Step 1 — Health

readiness at `/actuator/health/readiness`, liveness at
`/actuator/health/liveness`.

ready means an agent's next request will succeed: the context has
started, and the datasource can reach `crm41`. live means the process is
running and does not need restarting. the distinction matters to the
orchestrator in lab 42, where failing readiness removes the pod from the
load balancer while failing liveness kills it.

**two things in this project stopped those paths working, both of them
the exercise's Predict prompt, and both now fixed in lab40-crm so lab 41
inherits a working probe.**

`application.yml` exposed `health,info` only. the readiness and liveness
sub-paths are not exposed by default outside Kubernetes, so they need

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: true
```

without it `/actuator/health/readiness` returned 404.

`SecurityConfig` permitted `"/actuator/health"` and that string is an
exact match, not a prefix. `/actuator/health/readiness` is a different
path, so it fell through to `.anyRequest().authenticated()` and returned
**401** — exactly the "health 401" symptom the prompt asks about. a
Docker HEALTHCHECK carries no bearer token, so it would have seen 401
forever and marked the container unhealthy while the app was perfectly
fine.

the fix was one of two, and they are not equivalent:

| Option | Effect |
| --- | --- |
| permit `/actuator/health/**` | probes reachable; also exposes component detail if `show-details` is ever widened |
| keep the exact matcher, add `/actuator/health/readiness` and `/liveness` explicitly | narrower, and states which probes are public |

the second was taken: it is the smaller grant and the one consistent
with lab 40's finding that a permissive default is worse than an explicit
one. `ProbeEndpointsTest` now asserts both probes answer anonymously and
that `/actuator/health` exposes no `components` detail, so a regression
here fails the build rather than surfacing as an unhealthy container.

## Step 2 — Check the reference

readiness fails closed when the database is unreachable.

Spring's readiness group includes the datasource health indicator, so a
dead `crm41` makes readiness fail while liveness still passes. that is
the correct pairing: the process is fine, it just cannot serve, so it
should be taken out of rotation rather than restarted. restarting it
would not reach the database either.

the failure this prevents is a half-ready CRM: an agent opens `CUS-1001`,
the API is up, the query cannot run, and the agent sees an error that
looks like the customer is missing. better that the container is never
sent the request.

`show-details` stays off or `when-authorized`. an anonymous probe should
learn ready or not ready, not the database URL and the connection pool
state.

## Step 3 — Resources

placeholders for `docker run`, to be measured rather than trusted:

| Setting | Value | Reason |
| --- | --- | --- |
| `--memory` | 512m | Boot with JPA, Flyway and Tomcat starts comfortably under this |
| `--cpus` | 1.0 | single instance locally |
| `JAVA_OPTS` | `-XX:MaxRAMPercentage=75` | the important one |

the JVM flag is what stops the OOMKill the Debug prompt asks about.
without it a modern JVM reads the container limit and takes a default
share, but a fixed `-Xmx` set for a laptop ignores the cgroup entirely:
the JVM believes it may grow to a heap the container is not allowed to
have, the kernel kills the process at the limit, and the exit is 137 with
no Java stack trace to explain it. expressing the heap as a percentage of
whatever the container was given keeps the two numbers tied together.

so the tuning answer for an OOMKill is not always "raise the limit". it
is first "does the JVM know what the limit is".

## Step 4 — Graceful stop

`server.shutdown: graceful` plus
`spring.lifecycle.timeout-per-shutdown-phase: 20s`, neither of which is
set in this project today, so the current behaviour is immediate.

on `docker stop` the container receives SIGTERM and has a grace period
before SIGKILL. graceful shutdown means Tomcat stops accepting new
connections and lets in-flight requests finish, so a `PATCH
/api/customers/CUS-1002/status` carrying `lab-request-001` completes and
commits rather than being severed mid-transaction. the correlation id is
the thing that makes the difference visible afterwards: a request that
finished has a complete log line, one that was killed does not.

the docker grace period must exceed the Spring timeout, or SIGKILL
arrives first and the setting achieves nothing.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab41-health-resources.md`
- [ x ] Readiness path named
- [ x ] DB-down behavior stated
- [ x ] Graceful stop noted
