# Lab 31 prep checklist

## Earlier exercise files present?
| File | Present? (yes/no) |
| ---- | ----------------- |
| notes/lab31-spring-kafka.md | yes |
| notes/lab31-listener-sketch.md | yes |
| notes/lab31-todos.md | yes |
| notes/lab31-error-dlt-notes.md | yes |
| notes/lab31-idempotency-plan.md | yes |

## Fixtures (verify)
| ID | Name | Status |
| -- | ---- | ------ |
| CUS-1001 | Amina Khan | ACTIVE |
| CUS-1002 | Ravi Singh | PROSPECT |

Correlation lab-request-001.

## Topic dependency

Lab 31 publishes and consumes on the lab 30 topics, it does not create them.
crm.customer-events.v1 at 3 partitions and crm.customer-events.v1.dlq at 1,
on the KRaft broker in examples/lab30-crm/compose.yaml, localhost:9092 from the
host and kafka:9092 inside the Compose network. Both were created and confirmed
by describe during lab 30 and the container was not brought down with -v, so
they survive. Docker Desktop is not running at the time of writing this
checklist, so the first step of lab 31 is docker compose up -d and a describe to
confirm PartitionCount 3 before any Spring code runs. An empty consume or a
producer blocking for 60 seconds means the broker is down, not the code.

## Tooling

Temurin JDK 21.0.4 and Maven 3.9.9, same as lab 30, both confirmed on the
machine. Lab 31 adds spring-kafka and the test starter for EmbeddedKafka. No
Resilience4j, circuit breakers are lab 32 and stay parked.

## Scope statement
Pre-lab only — prepare for lab; do not complete full Lab 31 now.

## Self mark
Overall prep: Pass
If Fail, revisit exercise(s): n/a — five deliverables written, fixtures and
topic names match lab 30, tooling confirmed. The one thing not verified today is
the broker running, because Docker Desktop is stopped.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab31-prep-checklist.md`
- [ x ] Topic dependency stated
- [ x ] JDK 21/Maven note
- [ x ] Pass/Fail self-mark
