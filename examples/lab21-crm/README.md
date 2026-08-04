Northstar CRM build (Lab 21)

  mvn -B -Dtest=ActuatorIT test
  mvn -B clean verify
  mvn -q spring-boot:run   (serves /customers.html and /api/customers on 8080)

  curl -s http://localhost:8080/actuator/health
  curl -s http://localhost:8080/actuator/health/liveness
  curl -s http://localhost:8080/actuator/health/readiness
  curl -s http://localhost:8080/actuator/metrics/crm.customer.create

Copied from lab20-crm per step 1 of the guide, artifact renamed to
com.northstar:lab21-crm:0.0.1-SNAPSHOT. Actuator and Micrometer are the whole
of the change: spring-boot-starter-actuator with health probes and a readiness
group, CrmReadinessIndicator so readiness can fail while liveness holds,
CustomerMetrics with create and get counters and a get timer, and ActuatorIT to
hold all three. The lab 19 and 20 suites are untouched and still green, 11
tests in total.

micrometer-registry-prometheus is the step 1 bonus. It is BOM-managed by the
Boot parent, so the dependency carries no version, and it needs a network fetch
the first time: mvn -o fails on it until the jars are cached.

ACTUATOR EXPOSURE

exposure.include is health,metrics,info,prometheus and show-details is always.
Both are lab-only. Production keeps health on the public path, moves the rest
behind management.server.port, sets show-details to when-authorized and leaves
/actuator/env closed. The probe contract, the alert and the full restriction
list are in docs/monitoring-report.md.

CHANGES BEYOND THE ACTUATOR WORK

findById captures the Optional before returning it, so the get counter can tag
success against not_found and the timer can measure the call. create is
unchanged apart from the three counter lines beside the log lines it already
had.

Counting happens once per outcome. The service owns success, duplicate and
unexpected failure; the controller's IllegalArgumentException handler owns edge
rejections, which never reach the service because validation sits at the edge
where lab 20 put it. Counting in the validation branches as well doubled every
rejection, which reads as plausible traffic rather than as a bug.

TIMED-PATH PASS CRITERIA

| Criterion | Result |
| --------- | ----------- |
| Actuator health, liveness and readiness reachable | Pass |
| CrmReadinessIndicator fails readiness with liveness UP | Pass |
| crm.customer.create and crm.customer.get move with traffic | Pass |
| ActuatorIT green | Pass, 3 tests |
| Production exposure restrictions documented | Pass, docs/monitoring-report.md |

SECURITY NOTES

untrusted: everything over HTTP, the JSON body, the form fields, the
X-Correlation-Id header, and any caller of /actuator. Actuator is unauthenticated
in this build, which is why the exposure set is marked lab-only in
application.yml and in the report.

authn/authz: none here. In production management endpoints sit behind their own
port and an authenticated route, health alone stays public and returns no
details.

sensitive: fullName, email, phone, address, tokens, and everything /actuator/env
would print. Forbidden in log lines, in MDC values and in metric tags. Customer
ids are allowed in logs and forbidden as tags, which is a cardinality rule
rather than a privacy one; experiment 5 has the numbers.

CLEANUP

  mvn -B clean
  git status

Stop spring-boot:run if it is still serving. The lab-only readiness toggle
endpoint used for experiment 1 was deleted after the capture; setReady is
reachable only from ActuatorIT. target/ is ignored. Keep lab21-crm, Lab 22
replaces the remaining new wiring with Spring IoC.

NOTES

The monitoring contract is docs/monitoring-report.md and the logging contract
from lab 20 is docs/logging.md. Evidence transcripts and the five failure
experiments are in notes/screenshots/lab-21/. Reflection answers and checkpoints
are in notes/Week 2/Module 21/lab21-answers.md. Full GUIDE at
labs/Week 2 - Backend, AI Tools and Testing/module-21/lab21/.
