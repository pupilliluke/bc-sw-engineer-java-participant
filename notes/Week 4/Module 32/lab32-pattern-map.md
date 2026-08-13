# Lab 32 — Pattern Map

## Reference

| Pattern | CRM use |
| --- | --- |
| Retry | Transient 503 from Account Profile |
| TimeLimiter | Fail fast if call exceeds N ms |
| CircuitBreaker | Stop calling when failure rate high |
| Fallback | Return cached/minimal profile for Amina |

## Step 2 — Add Ravi row

Add one example sentence for `CUS-1002` Ravi when circuit is open.

| Fallback, circuit open | Ravi's detail page returns CUS-1002, Ravi Singh and status UNKNOWN with degraded=true, no balance or tier, answered from the CRM's own store without a call leaving the service |

## Step 3 — Order idea

Propose decorator order in one line (e.g. TimeLimiter → CircuitBreaker → Retry → call).

Retry → CircuitBreaker → TimeLimiter → call, so each attempt is separately time
limited and the breaker counts each bounded attempt.

The deck's example is TimeLimiter → CircuitBreaker → Retry → call, which bounds
the whole sequence including the retries instead. Both are defensible. The order
above is Resilience4j's own Spring Boot aspect order with Retry outermost, and it
is the one to expect if the annotations are stacked without setting aspect order
explicitly. What matters is knowing which it is, because a 2 second TimeLimiter
with 3 retries is a 2 second budget under one order and a 6 second budget under
the other.

## Step 4 — Boundary

Mark: do not apply circuit breaker to local in-memory map lookups.

Do not apply a circuit breaker to a local in-memory map lookup, only to real
outbound calls. The CustomerService store from lab 29 is a ConcurrentHashMap in
the same JVM, it cannot be slow, cannot be down and has no failure rate to
measure, so wrapping it adds state and latency to protect against nothing.

Kafka is not this either. Lab 30 and 31 are asynchronous publish and consume,
where the broker holds the record and the consumer reads at its own pace. Lab 32
is a synchronous outbound HTTP call the CRM has to wait for and cannot make
asynchronous.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab32-pattern-map.md`
- [ x ] Table present
- [ x ] Ravi example
- [ x ] Decorator order proposed
