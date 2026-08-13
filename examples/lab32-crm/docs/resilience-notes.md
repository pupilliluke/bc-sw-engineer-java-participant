Lab 32 — Resilience notes

## What is protected

One outbound call, `AccountClient.fetch`, a synchronous GET to
`/accounts/{customerId}/summary` on the Account Profile service. This is not the
Kafka path from labs 30 and 31. There the broker holds the record and the
consumer reads at its own pace, so nothing waits. Here the CRM has to wait for an
answer it cannot make asynchronous, which is why the patterns are needed at all.

The instance name is `accountProfile` on all three annotations and it matches the
key under `resilience4j.*.instances` in `application.yml`. A name matching nothing
silently gets defaults instead, and the usual symptom is a breaker that never
opens.

## Configuration and why these numbers

| Setting | Value | Reason |
| ------- | ----- | ------ |
| `timelimiter.timeoutDuration` | 1500ms | Bounds the wait. This is the setting that stops a 30 second dependency hang from holding a CRM request thread for 30 seconds |
| `retry.maxAttempts` | 3 | The first call plus two retries, for a transient 503 |
| `retry.waitDuration` | 200ms | Bounded backoff, so a retry storm cannot become the outage |
| `retry.enableExponentialBackoff` | true, multiplier 2 | 200ms then 400ms, each retry waits longer than the last |
| `retry.retryExceptions` | IOException, TemporaryAccountException | Only transient infrastructure failures retry. A validation error would fail the same way on every attempt |
| `circuitbreaker.slidingWindowSize` | 6 | Count based, the last 6 calls |
| `circuitbreaker.minimumNumberOfCalls` | 4 | Below this the breaker reports a failure rate of -1 and cannot open, whatever the outcomes were |
| `circuitbreaker.failureRateThreshold` | 50 | Half the window failing is an outage, one bad call is not |
| `circuitbreaker.waitDurationInOpenState` | 2s | Then half-open probes. Lab value, production would be longer |
| `circuitbreaker.permittedNumberOfCallsInHalfOpenState` | 2 | Two probes decide recovery, the GUIDE value |
| `circuitbreaker.registerHealthIndicator` | true | Breaker state appears in /actuator/health, with management.health.circuitbreakers.enabled |

The starter shipped `timeoutDuration: 1.5s`, which Spring cannot bind to a
Duration and which fails context startup with `'1.5s' is not a valid duration`.
It is `1500ms` here, which is also the value the GUIDE checkpoint names.

## Annotation composition

    @CircuitBreaker(name = "accountProfile", fallbackMethod = "fallback")
    @Retry(name = "accountProfile")
    @TimeLimiter(name = "accountProfile")
    public CompletableFuture<AccountSummary> find(String customerId, String correlationId)

Two things about this that are not obvious and were measured rather than assumed.

The return type has to be a `CompletableFuture` and the work has to be supplied
asynchronously. A TimeLimiter cannot interrupt a synchronous method, so with a
plain return type it silently does nothing.

The `fallbackMethod` on `@CircuitBreaker` neutralises `@Retry`. The fallback
turns a failure into a successful return, and Retry is the outer aspect, so it
sees success and never retries. Measured against a permanent 503: five calls to
`find` produced five HTTP requests and five breaker records, not fifteen. The
retry configuration is present and correct, but on this method nothing exercises
it. Moving `fallbackMethod` to `@Retry` would restore retries before the
fallback, at the cost of the breaker no longer seeing per-attempt outcomes. The
annotation placement here follows the GUIDE.
`retryRecovery_secondAttemptSucceeds` exercises the same retry instance
directly, decorating the client call without the breaker in front, 503 then
200 on a scenario stub with two requests in the journal.

The fallback signature must be the same arguments plus a `Throwable` and the same
return type, or it is not found at runtime. Adding `correlationId` to `find` means
adding it to `fallback` too.

## Correlation

`correlationId` is an argument to `find`, not something read from the request
inside it. The TimeLimiter runs the supplier on another thread, so anything held
in a ThreadLocal on the calling thread is not visible either to the client call or
to the fallback. Passing it explicitly is what survives the thread hop.

It goes out to Account Profile as the `X-Correlation-Id` header, and into the
`account_profile_degraded` log line alongside `customerId` and the exception type.
One id therefore spans the CRM log line and the dependency's own logs, which is
what a support ticket quoting `lab-request-001` needs. The header is asserted in
`healthyCall_returnsAvailable` against the WireMock journal rather than assumed.

## Fallback contract

    AccountSummary.unavailable(customerId)
    -> {"customerId":"CUS-1001","available":false,"note":"account-profile-unavailable"}

The fallback returns only what the CRM already knew. `customerId` came in on the
request, it did not come from Account Profile. Nothing else is invented. There is
no zero balance and no default tier, because a defaulted value could be read as
a real one and acted on.

`available=false` is the whole contract. A fallback returning a success-shaped
body with empty fields is a lie the caller cannot detect, and the UI would render
it as a customer with no accounts rather than as a customer whose accounts could
not be read.

This is a read policy. Write endpoints do not get this fallback. A degraded read
is honest, a write that returns success-shaped output when it did not happen is
not, and the caller will not retry something it was told succeeded.

UI string: Account information is temporarily unavailable.

## Runbook

    mvn -B test
    # expect Tests run: 5, Failures: 0, Errors: 0

The suite runs WireMock on a dynamic port, so no Account Profile service and no
Docker are needed.

    mvn -B spring-boot:run "-Dspring-boot.run.arguments=--server.port=8092"

    curl -s http://localhost:8092/actuator/health
    curl -s http://localhost:8092/actuator/circuitbreakers
    curl -s http://localhost:8092/actuator/circuitbreakerevents

`circuitbreakers` reports the live state and the thresholds actually bound, which
is the fastest way to catch an instance name that matches nothing.
`circuitbreakerevents` is the transition history, empty until calls have been
made.

There is no controller in this starter, so with the plain run nothing calls the
service and the events list stays empty. Adding `--lab.demo=true` to the run
arguments enables AccountDemoRunner, which drives 7 calls at a dead base-url so
the endpoints above show the transitions, captured in
notes/screenshots/lab-32/02-actuator.txt.

The `-D` argument needs the quotes on PowerShell, otherwise PowerShell splits at
the dot.

## Production caution

These thresholds are lab values chosen so a test can drive them in seconds. A
2 second wait in open state means a dependency that is restarting gets probed
almost immediately. A 6 call window means six requests decide the state of the
breaker, which on real traffic is a sample far too small to distinguish an outage
from noise.

Production sizes the window against real request rates, uses a wait duration
matched to how long the dependency actually takes to recover, and alerts on the
breaker opening rather than discovering it in a log. Retry only ever goes on
idempotent operations, a retried write that already succeeded creates a duplicate
and the caller cannot tell.
