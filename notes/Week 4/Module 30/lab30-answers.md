Lab 30 event-driven architecture and Kafka (reflection questions, checkpoints)

built under examples\lab30-crm, copied from the lab 30 starter. the starter
ships compose.yaml, three event JSON samples and CustomerEventProducer with the
acks, idempotence and send TODOs, so the work was the envelopes, the three
producer settings, the send block and the group and lag runs. one KRaft broker
on 9092, crm.customer-events.v1 at 3 partitions and the DLQ at 1. all five
experiments run.


REFLECTION QUESTIONS

1. Which design decision most affected correctness (keying by customerId)?

keying by customerId. order is only guaranteed inside a partition, and the key
is the only thing that decides which partition a record lands on. CUS-1001
CustomerCreated at partition 0 offset 0 and CustomerStatusChanged at offset 1
are ordered because they share a key, not because they were sent in that order.
experiment 2 dropped the key and the four records still landed together on
partition 0, which is the sticky partitioner batching them rather than any
guarantee. nothing in the null key case would have stopped the status change
being read before the create.

2. What evidence proves produce/consume works end-to-end?

three things that do not share a failure mode. the console consumer printing
Partition:0 Offset:0 CUS-1001 and Offset:1 for the same key, so the record is
on the log with the key the producer set. the java producer printing
topic=crm.customer-events.v1 partition=0 offset=3 from RecordMetadata, which
only returns after the in-sync replica acknowledged. and crm-audit reading all
12 records that crm-notifications had already consumed, which proves the log is
durable and read position is per group rather than a queue that drains.

3. Which failure was hardest to diagnose (lag, rebalance, advertised listeners)?

none of those three. the topic coming back with 1 partition after a delete and
recreate. topic deletion is asynchronous, the create raced it and failed with
TopicExistsException, and by the time the name was free something referenced it
and auto create made it with the broker default of one partition. nothing
errored. the only place it showed was PartitionCount in describe, and every
produce after it would have looked fine while every key shared one partition.
experiment 5 is the same failure reached by a typo instead of a race.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab30-crm under examples/ | Pass, copied from starter/ |
| A2 | KRaft Kafka Up on 9092 | Pass, apache/kafka 3.9.1, single node broker and controller |
| A3 | crm.customer-events.v1 (3p) and .dlq (1p) exist | Pass, confirmed by describe after the recreate |
| B1 | versioned JSON for Amina/Ravi with lab-request-001 | Pass, three files under events/, eventVersion 1 on each |
| B2 | CLI keyed produce and consume with key/partition/offset | Pass, parse.key=true and key.separator=: |
| B3 | same-key ordering visible for CUS-1001 | Pass, partition 0 offsets 0 and 1 |
| C1 | java producer acks=all and idempotence | Pass, both set explicitly, printed partition=0 offset=3 |
| C2 | competing crm-notifications vs independent crm-audit | Pass, 2 members split 2 and 1 partitions, crm-audit read all 12 |
| C3 | lag inspected, catch-up observed | Pass, LAG 1 on each of 3 partitions then 0 after restart |
| D1 | DLQ topic created for lab 31 | Pass, crm.customer-events.v1.dlq at 1 partition, nothing writes to it yet |
| D2 | local vs production notes | Pass, in docs/kafka-notes.md |
| D3 | no secrets, PII dumps or needless volumes committed | Pass, fictional fixtures only, target/ and .env ignored |

FULL PATH

| Item | Result |
| --- | --- |
| All five failure experiments | Pass |
| Runbook a peer can follow | Pass, docs/kafka-notes.md |
| Ordering and delivery semantics paragraph | Pass, docs/kafka-notes.md |
| Spring Kafka consumers | not added, lab 31 |

SECURITY AND PRODUCTION REVIEW

1. which event inputs are untrusted?

the whole record. the key is an arbitrary string the producer chose, the value
is bytes the broker never parses, and eventType, eventVersion and customerId
are claims the producer made about itself. the console producer proves the
point, it published a record whose key was CUS-1001 while data.customerId said
CUS-1002 and nothing rejected it. a consumer has to validate the envelope and
check the key against data.customerId before acting on it.

2. where are authn and authz enforced in production?

not here. this broker is PLAINTEXT with no authentication, so anything that can
reach 9092 can publish to any topic and read any topic. production terminates
TLS at the broker, authenticates clients with SASL or mTLS, and puts ACLs on
the topic so only customer-service can write crm.customer-events.v1 and only
the notification and audit groups can read it.

3. which values are sensitive?

the event is durable, replayable and readable by every consumer group, so
anything in data outlives the request that made it and cannot be edited or
deleted afterwards. names and email addresses in data are already more than an
audit consumer needs. the pattern for anything sensitive is to carry the
customerId and let the consumer look the rest up under its own authorisation,
so retention on this topic is not also retention on personal data. the fixtures
here are fictional.
