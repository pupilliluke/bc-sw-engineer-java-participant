Lab 32 resilience and fault tolerance (reflection questions, checkpoints)

built under examples\lab32-crm from the lab 32 starter. the starter ships the
Boot baseline, AccountSummary, AccountClient, AccountProfileService,
TemporaryAccountException, the resilience4j config and three empty tests, so the
work was the RestClient call and its 5xx mapping, the three annotations, the
fallback body and the WireMock tests. 5 tests green on two consecutive runs, no
Docker and no Account Profile service needed. correlationId is threaded through
find as an argument and sent to Account Profile as X-Correlation-Id. all five experiments addressed,
four run and one deliberately left as a thought experiment.

one starter defect. application.yml shipped timeoutDuration: 1.5s, which Spring
cannot bind to a Duration, and the context failed to start with '1.5s' is not a
valid duration. changed to 1500ms, which is also the value the GUIDE checkpoint
names.


REFLECTION QUESTIONS

1. Which design decision most affected correctness (fallback honesty vs
fail-hard)?

fallback honesty, specifically the available flag. the fallback returns 200 with
a body that parses exactly like a healthy one, so the flag is the only thing
separating a real answer from a fabricated one. experiment 5 changed
AccountSummary.unavailable to return available=true and nothing else about the
response changed, same status, same shape, and two tests failed on that one
field. fail-hard was the wrong choice here because the CRM is up and only the
enrichment is missing, a 503 would take out a whole customer page for a
dependency that supplies part of it.

2. What evidence proves OPEN fail-fast without calling WireMock?

the WireMock journal, not the clock. openCircuit_failsFastWithoutHittingStub
counts serve events before and after the call and asserts the count is unchanged,
so the assertion is that no request left the CRM rather than that the answer came
back quickly. the elapsed check under 500ms is a second, weaker signal alongside
it. timing alone would not distinguish fail-fast from a dependency that happened
to answer quickly.

3. Which failure was hardest (aspect order, TimeLimiter + Future, CB thresholds)?

the breaker not opening, which turned out to be two separate problems with the
same symptom. the metrics
said buffered=3 failed=3 rate=-1.0, and the -1 is the breaker saying it has fewer
than minimumNumberOfCalls and is not computing a rate at all, so three failures
out of three left it CLOSED. the same output also showed 3 HTTP calls for 3
find() calls, which means @Retry never fired. fallbackMethod is on
@CircuitBreaker, so the failure becomes a successful return and @Retry as the
outer aspect sees success and has nothing to retry. neither would have been
visible from the test assertion, both came from printing the breaker metrics
and counting the stub calls.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab32-crm under examples/ | Pass, copied from starter/ |
| A2 | Resilience4j Boot3, AOP and Actuator resolve | Pass, all three in the starter pom, versions from the Boot 3.3.5 parent |
| A3 | WireMock on the test classpath | Pass, wiremock-standalone, dynamic port |
| B1 | Retry instance accountProfile with bounded backoff | Pass, maxAttempts 3, waitDuration 200ms with exponential backoff x2, retryExceptions limited to IOException and TemporaryAccountException. inert on find(), see reflection 3, exercised directly in retryRecovery_secondAttemptSucceeds |
| B2 | CircuitBreaker count window and OPEN fail-fast proof | Pass, window 6, threshold 50, journal asserts no call while OPEN |
| B3 | TimeLimiter 1500ms with async CompletableFuture | Pass, after fixing the starter's 1.5s |
| C1 | AccountSummary.unavailable for degraded reads | Pass, customerId only, available=false |
| C2 | no false-success write fallback | Pass, read policy only, documented in docs/resilience-notes.md |
| C3 | correlation and CUS-1001 visible in logs | Pass, account_profile_degraded logs customerId, correlationId and the cause type, and the outbound call carries X-Correlation-Id, asserted in healthyCall_returnsAvailable |
| D1 | actuator health, events and metrics consulted | Pass, output in notes/screenshots/lab-32/02-actuator.txt, including CLOSED_TO_OPEN, OPEN_TO_HALF_OPEN and HALF_OPEN_TO_OPEN in circuitbreakerevents and the breaker state in health |
| D2 | AccountProfileResilienceTest green twice | Pass, 5 tests, two consecutive runs |
| D3 | production threshold caution documented | Pass, docs/resilience-notes.md |

FULL PATH

| Item | Result |
| --- | --- |
| Failure experiments 1, 2, 3, 5 | Pass |
| Failure experiment 4, retry a non-idempotent write | thought experiment only, deliberately not demonstrated |
| Correlation id threaded from the web layer | Pass by argument rather than by header capture, find takes correlationId and passes it to the client as X-Correlation-Id. There is no controller in this starter to read the inbound header |
| React banner wiring | not added, no frontend in this starter |

SECURITY AND PRODUCTION REVIEW

1. which inputs are untrusted?

the Account Profile response. it is a remote body parsed straight into
AccountSummary, so its size, its shape and its latency are all outside the CRM's
control. the latency is the one this lab is about, an unbounded read is a way for
a third party to hold CRM threads. the base URL is configuration rather than
input and it is externalised to ACCOUNT_API_BASE_URL, not hard coded.

2. where are validation and authz enforced?

not on this call. the CRM is the client here, so authz would be an outbound
credential the Account Profile service checks, and there is none in this lab, the
stub accepts anything. inbound authz is the lab 28 filter chain on the CRM's own
API, which this starter does not include. production would send a service
credential on the outbound call and would not treat a 401 from Account Profile as
a transient failure worth retrying.

3. which values are sensitive?

whatever a real Account Profile returns, balance and tier being the obvious ones.
they are deliberately absent from the fallback rather than defaulted, so a
degraded response cannot leak a stale or invented figure. the logs carry
customerId and the exception type and no response body, so a degraded read does
not put account data in the log. the fixtures here are fictional.
