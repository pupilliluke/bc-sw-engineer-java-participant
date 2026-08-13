# Lab 32 — Fill Resilience TODOs

## Step 1 — Paste

Create `notes/lab32-todos.md`:

```java
@CircuitBreaker(name = "accountProfile", fallbackMethod = "profileFallback")
@Retry(name = "accountProfile")
@TimeLimiter(name = "accountProfile")
public CompletableFuture<AccountProfile> getProfile(String customerId) {
  return accountClient.fetch(customerId); // remote client
}

private CompletableFuture<AccountProfile> profileFallback(String customerId, Throwable t) {
  // return only what the CRM already knows, never an invented balance or tier
  // TODO: log correlationId lab-request-001 and t.getClass() when the fallback fires
  return CompletableFuture.completedFuture(AccountProfile.minimal(customerId));
}
```

## Step 2 — Fill

Suggested fills: `accountProfile`, `profileFallback`, `accountClient`, `AccountProfile.minimal(customerId)` (or similar).

The instance name is `accountProfile` on all three annotations and it has to
match the key under `resilience4j.circuitbreaker.instances` in the YAML. A name
that matches nothing gets defaults instead of the config below, and the breaker
looks like it never opens.

The fallback signature has to match the method plus a Throwable, and it has to
return the same type. A fallback returning AccountProfile instead of
CompletableFuture<AccountProfile> is not found at runtime.

## Step 3 — Config blanks

Add YAML TODOs: `failureRateThreshold: _____`, `waitDurationInOpenState: _____`, `maxAttempts: _____` with example numbers you choose.

```yaml
resilience4j:
  circuitbreaker:
    instances:
      accountProfile:
        failureRateThreshold: 50          # percent, open once half the window fails
        slidingWindowSize: 10             # last 10 calls
        waitDurationInOpenState: 10s      # then probe with half-open
        permittedNumberOfCallsInHalfOpenState: 3
  retry:
    instances:
      accountProfile:
        maxAttempts: 3                    # the first call plus 2 retries
        waitDuration: 500ms
  timelimiter:
    instances:
      accountProfile:
        timeoutDuration: 2s
```

2s is the number that stops the 30 second hang from exercise 1. 50 percent over
10 calls opens the breaker on a real outage without tripping on one bad call.
10s in open is long enough that a restarting dependency is not probed
immediately. maxAttempts 3 is the first call plus two retries, and it is only
safe here because getProfile is a read.

## Step 4 — Correlation

TODO comment: log `lab-request-001` when fallback fires.

The TODO is in the fallback body above. Log correlationId and the throwable type
when the fallback fires, because a degraded response is otherwise indistinguishable
from a healthy one in the logs, and the throwable is what says whether this was a
timeout, a 503 or CallNotPermittedException with the circuit already open.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab32-todos.md`
- [ x ] Annotation blanks filled
- [ x ] Three config numbers chosen
- [ x ] Correlation log TODO present
