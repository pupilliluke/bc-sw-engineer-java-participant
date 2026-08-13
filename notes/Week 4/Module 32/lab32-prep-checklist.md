# Lab 32 prep checklist

## Earlier exercise files present?
| File | Present? (yes/no) |
| ---- | ----------------- |
| notes/lab32-resilience.md | yes |
| notes/lab32-circuit-states.md | yes |
| notes/lab32-fallback-contract.md | yes |
| notes/lab32-pattern-map.md | yes |
| notes/lab32-todos.md | yes |

## Fixtures (verify)
| ID | Name | Status |
| -- | ---- | ------ |
| CUS-1001 | Amina Khan | ACTIVE |
| CUS-1002 | Ravi Singh | PROSPECT |

Correlation lab-request-001. Resilience4j instance name accountProfile.

## Teach-back

An unprotected outbound call is a shared resource problem, not a slow page
problem. Account Profile hanging 30 seconds holds a CRM request thread for 30
seconds, and enough of those exhaust the pool so requests that never touch
Account Profile start failing too. The TimeLimiter bounds the wait, so the
failure is fast. Retry covers a transient blip, and only on reads, because a
retried POST that already succeeded creates a second customer. The CircuitBreaker
stops calling a dependency that is already failing, closed counts outcomes, open
answers from the fallback without a call leaving the CRM, half-open probes with a
few trial calls before letting the load back. The fallback decides what the
caller gets, and it may only return what the CRM already knew, customerId,
displayName and status UNKNOWN, never an invented balance. It answers 200 with
degraded=true because the CRM is up and only the enrichment is missing. None of
this fixes a wrong URL, it makes the failure fast and contained instead of slow
and spreading.

## Evidence preview

Expected under notes/screenshots/lab-32/:

| Evidence | Shows |
| --- | --- |
| Test run output | resilience tests green on two consecutive runs |
| Circuit state transitions | closed to open on repeated failure, open to half-open to closed on recovery |
| Fallback response | CUS-1001 body with degraded=true and no balance or tier |
| Timeout evidence | call bounded at the configured timeoutDuration, not the dependency's hang |
| Failure experiments | the GUIDE table, every temporary edit restored |

## Tooling

Temurin JDK 21.0.4 and Maven 3.9.9, confirmed. Lab 32 adds resilience4j-spring-boot3
and Spring Boot AOP. Lab 30 and 31 Kafka work is unaffected, this module is
synchronous outbound HTTP.

## Scope statement
Pre-lab only — prepare for lab; do not complete full Lab 32 now.

## Self mark
Overall prep: Pass
If Fail, revisit exercise(s): n/a — five deliverables written, fixtures and the
accountProfile instance name agreed across the pattern map and the TODOs.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab32-prep-checklist.md`
- [ x ] Teach-back written
- [ x ] Evidence preview listed
- [ x ] Pass/Fail marked
