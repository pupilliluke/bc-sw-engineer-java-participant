# CRM Monitoring Report (Lab 21)

What this service reports to operators, and what production must tighten before
any of it is exposed. Evidence is in `notes/screenshots/lab-21/`.

## Probes

| Path | Serves |
| --- | --- |
| `/actuator/health` | overall status with every component |
| `/actuator/health/liveness` | `livenessState` |
| `/actuator/health/readiness` | `readinessState` and `crmReadinessIndicator` |

Liveness answers whether the process is alive; readiness answers whether it can
take CRM traffic. An instance replaying a schema migration is live and not
ready, so the load balancer stops routing to it while the orchestrator leaves
it running to finish. A dead process fails liveness and is restarted, which is
the wrong action for the migrating instance because it would discard the work
and re-enter the same wait.

## Readiness indicator

`CrmReadinessIndicator` reports `UP` with `crm=ready`, or `OUT_OF_SERVICE` with
`crm=not-ready` and `reason=dependency-unavailable`.

`setReady(boolean)` is a lab-only toggle. It flips a flag with no dependency
behind it and exists so readiness can be failed on demand for evidence. Not
production code: a production indicator checks the dependency itself and no
caller gets to set the answer.

`application.yml` names the bean in the readiness group. Group membership is
validated during context refresh, so a missing or renamed bean fails startup
rather than leaving readiness silently UP.

## Exposure

Lab-only, both lines: `include: health,metrics,info,prometheus` and
`show-details: always`.

Production restricts:

- `show-details: when-authorized`. Under `always` the `diskSpace` component
  returns the absolute project path and the free bytes on the volume to any
  unauthenticated caller.
- Actuator on its own `management.server.port`, not routed from the public load
  balancer. `health` alone stays on the public path.
- Prometheus scraped from inside the network.
- `/actuator/env` stays closed. It prints configuration, secrets included.

## Metrics

`CustomerMetrics` registers two counters, tagged by `result` only.

| Meter | Values | Recorded by |
| --- | --- | --- |
| `crm.customer.create` | `success`, `failure` | `CustomerService.create`, and `CustomerController.badRequest` for edge rejections |
| `crm.customer.get` | `success`, `not_found` | `CustomerService.findById` |
| `crm.customer.get.latency` | timer, untagged | `CustomerService.findById` |

Each outcome increments once. Validation rejections never reach the service,
so the controller's `IllegalArgumentException` handler owns them; every other
outcome belongs to the service. Recording in both places double-counts, which
reads as plausible traffic rather than as a bug.

`management.metrics.tags` adds `application=northstar-crm` to every meter. It
is one fixed value, so it costs one series per counter.

Customer ids and correlation ids are log fields (Lab 20), never metric tags.
They are unbounded: one tag value per customer multiplies the stored series
until the backend fails.

Prometheus renames on scrape, dots to underscores plus a `_total` suffix:

```
crm_customer_create_total{application="northstar-crm",result="failure"} 2.0
crm_customer_create_total{application="northstar-crm",result="success"} 1.0
crm_customer_get_total{application="northstar-crm",result="not_found"} 1.0
crm_customer_get_total{application="northstar-crm",result="success"} 1.0
```

The timer reports `COUNT`, `TOTAL_TIME` and `MAX` in seconds. `TOTAL_TIME` over
`COUNT` is the mean; `MAX` is what catches a single slow call that a mean
buries. Experiment 4 moved it from 0.00056 to 0.16 seconds.

## Example traffic

`POST CUS-2101`, `GET CUS-1001`, correlation `lab-request-001`. Full session in
`notes/screenshots/lab-21/02-api-and-actuator-manual.txt`.

## Alert

```text
Alert: CRMCreateFailureRatioHigh
Expr:  rate(crm_customer_create_total{result="failure"}[5m])
       / rate(crm_customer_create_total[5m]) > 0.05
For:   5m
Action: page on-call; search logs for op=customer.create level=ERROR|WARN
```

The ratio is the alertable shape, not the raw count: a fixed threshold on
failures fires on any traffic spike, and a service with no traffic at all
cannot breach a ratio. `For: 5m` keeps a single bad minute from paging.

One caveat this build carries. `result=failure` covers three different things —
duplicate ids, edge validation rejections, and unexpected exceptions — because
the starter's vocabulary is `success` and `failure`. The first two are client
errors, so a caller retrying a duplicate can push the ratio past 5 percent with
the service healthy, which experiment 3 shows. Splitting `duplicate` and
`invalid` into their own tag values would let the alert sum only genuine
faults, and stays low-cardinality at four fixed values.

The metric says how many; the log says which. On a page, the alert names the
operation and the logs carry `corr`, `cust` and `op` for the individual
requests behind it (Lab 20, `docs/logging.md`).

## Failure experiments

Five, recorded with before and after payloads in
`notes/screenshots/lab-21/03-failure-experiments.txt`. Every temporary edit was
restored and the suite is green at 11 tests afterwards.

| # | Experiment | Observed |
| - | --- | --- |
| 1 | Flip readiness off | Readiness `OUT_OF_SERVICE` 503, liveness `UP` 200, API still serving |
| 2 | Invalid create, blank name | `crm.customer.create` 1 to 2, `failure` value appears |
| 3 | Repeat create/get `CUS-1001` | Two 409s, `failure` 1 to 3, record unchanged |
| 4 | Induce latency | Timer `MAX` 0.00056 to 0.16 seconds, counter shape unchanged |
| 5 | Temporary `customerId` tag | Same 8 requests, 2 series to 8 series |
