Lab 21 API observability and monitoring (reflection questions, checkpoints)

built under examples\lab21-crm, copied from lab20-crm per step 1. the change is
spring-boot-starter-actuator with probes and a readiness group,
CrmReadinessIndicator, CustomerMetrics with create and get counters plus a get
timer, and ActuatorIT. 11 tests green on three consecutive mvn -B clean verify
runs, Chrome 150 headless for the carried-over UI suite. app captured on port
8080, after stopping a CrmApplication left running from the lab 20 session that
still held it.


REFLECTION QUESTIONS

1. Which design decision most affected correctness (readiness group vs single
health blob)?

the readiness group. a single blob gives one status for two questions, so
anything that fails takes the whole answer down and an orchestrator cannot tell
drain from restart. with the group, experiment 1 flips crmReadinessIndicator
and readiness returns OUT_OF_SERVICE and 503 while liveness stays UP and 200,
which is the split the load balancer and the restart policy read differently.
the group also validates its members at context refresh, so naming a bean that
does not exist fails startup instead of reporting UP forever.

2. What evidence proves create traffic is observable?

crm.customer.create is 404 before any traffic and COUNT 1 with result=success
after one POST of CUS-2101, in 02-api-and-actuator-manual.txt. the failure
paths move it too: blank fullName takes it to 2 and two duplicate CUS-1001
posts take result=failure to 3, in 03-failure-experiments.txt.
createMetricAppearsAfterTraffic in ActuatorIT holds the path, and the prometheus
scrape shows the same counts as crm_customer_create_total.

3. Which failure was hardest to diagnose?

the app not starting at all, before any of the lab work. the readiness group
named crmReadinessIndicator while the class was still a guide snippet with no
package line and no imports, so the bean did not exist and the context refresh
cancelled with NoSuchHealthContributorException. it read as an actuator problem
because /actuator/health was unreachable


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab21-crm under examples/ | Pass, copied from lab20-crm, artifact renamed |
| A2 | Actuator dependency present | Pass, spring-boot-actuator-autoconfigure 3.3.5, BOM-managed |
| A3 | Local exposure configured with production hardening notes | Pass, lab-only comments in application.yml, restrictions in docs/monitoring-report.md |
| B1 | Liveness and readiness curls documented | Pass, 02-api-and-actuator-manual.txt |
| B2 | CrmReadinessIndicator can fail readiness independently | Pass, experiment 1, readiness 503 with liveness 200 |
| B3 | Written distinction: LB drain vs process restart | Pass, docs/monitoring-report.md and experiment 1 |
| C1 | CustomerMetrics counters/timers wired | Pass, two counters tagged by result, one untagged timer |
| C2 | Before/after create/get evidence with CUS-1001 | Pass, 404 baseline then COUNT 1, experiment 3 for the repeats |
| C3 | ActuatorIT green (health + increment) | Pass, 3 tests |
| D1 | monitoring-report.md complete | Pass, probes, indicator, exposure, metrics, alert, experiments |
| D2 | No high-cardinality tags; no secrets in Actuator config | Pass, result only, experiment 5 counter removed |
| D3 | Lab-only readiness toggle marked non-production | Pass, marked in docs/monitoring-report.md and README, toggle endpoint deleted after experiment 1 |

SECURITY AND PRODUCTION REVIEW

1. which browser, network, or actuator inputs are untrusted?

all of them. the JSON body, the form fields, the X-Correlation-Id header which
is echoed and never treated as identity, and every caller of /actuator, which
is unauthenticated in this build.

2. where are authn/authz enforced for management endpoints in production?

not here. in production actuator binds to its own management.server.port that
the public load balancer does not route, health alone stays on the public path
with show-details: when-authorized, and the rest sits behind an authenticated
internal route. prometheus is scraped from inside the network.

3. which values are sensitive, never as metric tags or open actuator fields?

fullName, email, phone, address, tokens, and everything /actuator/env would
print. show-details: always already leaks the absolute project path and the
free bytes on the volume through diskSpace, which is the concrete reason it is
lab-only. customer ids are a separate case: allowed in logs, forbidden as tags,
because the problem there is cardinality rather than privacy.

WINDOWS HOW-TO PASS CRITERIA

| # | Confirm | Result |
| - | --- | --- |
| 1 | workspace open in IntelliJ with SDK 21 | Pass, temurin-21 |
| 2 | lab project under examples/lab21-crm | Pass |
| 3 | GUIDE deliverables and checkpoints complete | Pass |
| 4 | commands succeed | Pass, mvn -o fails until the prometheus jars are cached |
| 5 | evidence under notes/screenshots/lab-21 | Pass, kept in the project as since lab 14 |
