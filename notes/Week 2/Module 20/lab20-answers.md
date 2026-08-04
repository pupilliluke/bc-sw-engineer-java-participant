Lab 20 structured logging and diagnostics (reflection questions, checkpoints)

built under examples\lab20-crm, copied from lab19-crm per step 1. the change is
logback-spring.xml with a corr/cust/op pattern, CorrelationFilter owning the
MDC lifecycle, SLF4J in the service and the controller, and CustomerLoggingIT
holding the no-PII rule. 8 tests green on three consecutive mvn -B clean
verify runs, Chrome 150 headless for the carried-over UI suite. app captured on
port 8081, a CrmApplication left running from the lab 19 session still held
8080.


REFLECTION QUESTIONS

1. Which design decision most affected correctness (filter-owned MDC vs
service-owned)?

filter-owned. the filter puts correlationId once and clears everything in
finally; the service only puts and removes cust and op. that split means a
service call cannot wipe the correlation id mid-request, and one clear covers
every path out of the request including the 500 in experiment 1. it also
removed the correlationId parameter that lab19-crm threaded through create.

2. What evidence proves support can search a request?

every line in 02-api-manual.txt carries the corr the caller sent, across six
requests and three different ids, and the ERROR in experiment 1 carries the
same corr, cust and op as the INFO before it. CustomerLoggingIT asserts corr,
cust and op are present and that the name and the address are not, so the claim
fails the build rather than drifting.

3. Which failure was hardest to diagnose?

the MDC leak in experiment 5, because the first attempt did not reproduce. with
the clear commented out nothing leaked: the filter defaults a correlation id on
every request, so the stale key is overwritten before anything logs. the leak
only appears once the put is conditional on the header, and then only when the
pool hands back the same thread, which took fourteen requests. reading the
thread name in the pattern is what made it visible at all.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab20-crm under examples/ | Pass, copied from lab19-crm, artifact renamed |
| A2 | SLF4J + Logback, single binding | Pass, logback-classic 1.5.11, slf4j-api 2.0.16, the log4j and jul entries are bridges |
| A3 | logback-spring.xml includes corr/cust/op | Pass, no competing logback.xml |
| B1 | CorrelationFilter sets MDC and clears in finally | Pass, defaults lab-request-001 and echoes the header |
| B2 | service create/get use SLF4J with ID/op MDC | Pass, cust and op removed in finally, filter owns the full clear |
| B3 | no PII in sampled INFO lines | Pass, cust is the id only, asserted by CustomerLoggingIT |
| C1 | controller WARN reason codes without payload dump | Pass, invalid_id, missing_full_name, missing_status |
| C2 | manual traces for CUS-1001 / CUS-1002 | Pass, 02-api-manual.txt, six requests |
| C3 | CustomerLoggingIT asserts ids present and Amina absent | Pass, 2 tests |
| D1 | docs/logging.md contract complete | Pass, matches the observed console |
| D2 | two green runs | Pass, three consecutive clean verifies at 8 tests |
| D3 | no secrets or raw PII dumps committed | Pass, example.com fixtures, transcripts carry ids only |

WINDOWS HOW-TO PASS CRITERIA

| # | Confirm | Result |
| - | --- | --- |
| 1 | workspace open in IntelliJ with SDK 21 | Pass, temurin-21, though examples/ was never imported as a Maven project so the IDE showed no errors |
| 2 | lab project under examples/lab20-crm | Pass |
| 3 | GUIDE deliverables and checkpoints complete | Pass |
| 4 | commands succeed | Pass, dependency:tree needs -B not -q to print |
| 5 | evidence under notes/screenshots/lab-20 | Pass, kept in the project as since lab 14 |
