Lab 31 Kafka integration with Spring Boot (reflection questions, checkpoints)

built under examples\lab31-crm from the lab 31 starter, timed path. the starter
ships the Boot baseline, CustomerEvent, the publisher, the listener, the store,
KafkaErrorConfig and an EmbeddedKafka test with the bodies as TODOs, so the work
was the record guards, the send, the listener checks, markIfNew, the error
handler and the tests. 3 tests green on two consecutive runs, no Docker broker
needed for the suite. four of the five experiments run.


REFLECTION QUESTIONS

1. Which design decision most affected correctness (publish-after-success vs
outbox)?

not the publish timing, the idempotency key. publish-after-success is the only
option this lab implements and its risk is one-directional, the write commits
and the publish fails, so nobody downstream is told. choosing eventId as the
idempotency key is what makes the redelivery that follows any retry or rebalance
safe, and it is a decision that cannot be retrofitted, the field has to be in the
envelope before anything is published. lab 30 put eventId in the envelope and
nothing used it, this lab is where it earns its place.

2. What evidence proves once-only business side effects?

ignoresDuplicateEventId publishes the same eventId twice and asserts
listener.events() has one entry for it. the assertion is on the listener rather
than on ProcessedEventStore because the store returns false on a second call
whether or not the listener ran, so it cannot tell handled-once from
never-handled. the DLT test adds the negative case, a rejected record is absent
from events() entirely.

3. Which failure was hardest (deserialization, DLT wiring, flaky await)?

the trusted packages experiment, because it passed when it should have failed.
setting spring.json.value.default.type stops JsonDeserializer consulting the type
header, so the trusted package list is never checked and pointing it at
com.example.nowhere changed nothing. only removing the default type as well
reproduced the failure, and the symptom then was silence, an empty
listener.events() and an await timeout, not an error in the handler. second
hardest was the DLT exception header, the top level one names
ListenerExecutionFailedException and the assertion has to be on the cause header.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab31-crm under examples/ | Pass, copied from starter/ |
| A2 | spring-kafka and test jar present | Pass, 3.2.4 both, versions from the Boot 3.3.5 parent |
| A3 | bootstrap, group, trusted packages, topic externalized | Pass, all in application.yml, bootstrap has an env override |
| B1 | CustomerEvent v1 with null and version guards | Pass, compact constructor, requireNonNull on eventId and customerId, throws on version not 1 |
| B2 | KafkaTemplate publish keyed by CUS-1001 / CUS-1002 | Pass, key is event.customerId() |
| B3 | @KafkaListener validates key against customerId | Pass, throws InvalidCustomerEventException on null or mismatch |
| C1 | ProcessedEventStore ignores duplicate eventId | Pass, markIfNew is Set.add, asserted by ignoresDuplicateEventId |
| C2 | retry backoff and non-retryable exceptions | Pass, FixedBackOff(1000, 2) and three non-retryable types |
| C3 | dead-letter publication observed | Pass, keyMismatchGoesToDeadLetterTopic asserts the record and its headers |
| D1 | EmbeddedKafka flow test green twice | Pass, 3 tests, two consecutive runs |
| D2 | correlation ids in logs and events, no PII dumps | Pass, publisher and listener log correlationId, no payload dumps |
| D3 | runbook and DLT naming documented | Pass, docs/spring-kafka-notes.md |

FULL PATH

| Item | Result |
| --- | --- |
| Failure experiments 1, 2, 3, 5 | Pass |
| Failure experiment 4, retryable then succeed | not run, needs a test only fault injection point in the listener |
| Publisher wired into a service create method | not added, the starter has no web or service layer, that is the full path with lab29-crm copied |
| Transactional outbox | not added, documented as the production answer in docs/spring-kafka-notes.md |

SECURITY AND PRODUCTION REVIEW

1. which event and network inputs are untrusted?

the whole record. the key is a string the producer chose, the value is bytes the
broker never parsed, and eventType, eventVersion and customerId are claims the
producer made about itself. lab 30 proved this by publishing a record keyed
CUS-1001 whose payload said CUS-1002 with nothing rejecting it. the consumer
treats all of it as input, the record constructor rejects a missing eventId or a
version other than 1, and the listener rejects a key that does not match
data.customerId. the deserializer is also an input surface, which is why it is
wrapped in ErrorHandlingDeserializer rather than trusted to only ever see valid
JSON.

2. where are validation and authz enforced?

validation is at the consumer boundary here, in the record constructor and the
first two checks of the listener, the same shape as @Valid at the controller in
lab 29. authz is nowhere. the broker is PLAINTEXT with no authentication, so
anything that can reach 9092 can publish to crm.customer-events.v1 and read it.
HTTP authz from lab 28 protects the API, not the topic. production needs SASL or
mTLS and topic ACLs, write on the events topic for customer-service only and read
for the notification and audit groups.

3. which values are sensitive in payloads and logs?

the event is durable, replayable and readable by every consumer group, so
anything in data outlives the request and cannot be edited or deleted later.
fullName is already more than the notification path needs to decide what to
send. the logs deliberately carry eventId, correlationId, customerId, partition
and offset and no payload dump, so a support ticket can be traced without the
log becoming a second copy of the personal data. the fixtures here are
fictional.
