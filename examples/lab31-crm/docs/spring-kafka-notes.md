# Lab 31 — Spring Kafka notes

## Publish path

`CustomerEventPublisher.publish` sends the event to the topic from
`crm.kafka.customer-events-topic` with key `event.customerId()`, so CUS-1001 and
CUS-1002 keep the partition affinity lab 30 froze. `KafkaTemplate.send` returns a
future and does not block, unlike the `send().get()` the lab 30 raw producer used.
The `whenComplete` callback logs `customer_event_published` with the eventId,
correlationId, partition and offset on success, and
`customer_event_publish_failed` with the same ids on failure. Both carry the
correlationId, so a publish that never happened is still traceable.

Producer settings live in `application.yml`, not in code: `acks: all` and
`enable.idempotence: true`, the same two the lab 30 producer set through
`Properties`.

### Publish timing, DB vs Kafka

Publish-after-success is what this lab does. The event is published once the
write it describes has succeeded, so no consumer is told about a customer that
does not exist. The risk it leaves open is the other direction, the write commits
and the publish fails, and nothing downstream is ever told. The publish is
asynchronous, so a failure cannot fail the caller's request either, it only
appears in the `customer_event_publish_failed` log line.

A transactional outbox is the production answer. The event row is written in the
same transaction as the customer, and a relay reads that table and publishes.
One atomic write, publishing after commit, at-least-once end to end.

Lab 31 on its own does not give dual-write atomicity and this file does not claim
it does. No outbox and no transactions were implemented here.

## Idempotency

`ProcessedEventStore.markIfNew` is `Set.add`, which is the check and the mark in
one atomic operation, so two threads on the same eventId cannot both see it as
new. The listener calls it before the side effect. A second delivery of the same
eventId logs `duplicate_event_ignored` and returns without handling.

eventId is the key rather than the offset. Offsets are per partition and per
group, they repeat on replay, and the same event read by crm-notifications and
crm-audit has the same offset in both, so an offset does not identify an event.

Marking before the side effect is a deliberate trade. A crash between the mark
and the side effect loses the notification, a crash between the side effect and
the mark duplicates it. Neither is exactly once and the lab takes the first.

Lab only: the store is an in-memory Set. It empties on restart and is not shared,
so two instances would each handle the same event once. Production needs a
durable store with a unique constraint on eventId and a retention window.

## DLT

Dead letters go to `crm.customer-events.v1.dlq`, the name lab 30 froze, not
Spring's default. `DeadLetterPublishingRecoverer` with no destination resolver
would write to the source topic plus `.DLT`, so `crm.customer-events.v1.DLT`,
and a lab 30 consumer watching `.dlq` would see an empty topic while records
piled up under the other name.

The resolver in `KafkaErrorConfig` returns partition `-1` rather than the source
partition. The default is to publish to the same partition number as the record
came from, and the source topic has 3 partitions while the DLQ has 1, so any
record failing on partition 1 or 2 would fail to publish at all.

`DeadLetterPublishingRecoverer` adds headers to the dead-lettered record naming
the original topic, partition, offset, timestamp, exception class and exception
message, so the record can be traced back without the original log.

### Retryable vs non-retryable

`DefaultErrorHandler` with `FixedBackOff(1000, 2)` gives one second between
attempts and two retries after the first try, then the recoverer runs and the
offset commits past the record. Anything not listed below is treated as
retryable, which is the right default for a timeout or an unreachable
dependency.

Non-retryable, registered with `addNotRetryableExceptions`:

| Exception | Cause |
| --- | --- |
| `InvalidCustomerEventException` | record key does not match `data.customerId`, or key is null |
| `UnsupportedEventVersionException` | `eventVersion` is not 1 |
| `DeserializationException` | the value is not a `CustomerEvent` |

All three are properties of the record, so every attempt fails identically and
retrying only delays the partition.

`UnsupportedEventVersionException` and a malformed value are thrown inside the
deserializer, before the listener method runs, so `DefaultErrorHandler` would
never see them and the container would fail instead. `ErrorHandlingDeserializer`
in `application.yml` wraps `JsonDeserializer` and turns those into a
`DeserializationException` the handler can route to the DLQ.

## Runbook

Broker, from `examples/lab31-crm`. The lab 30 compose file is the broker for this
lab, it is not duplicated here:

    docker compose -f ../lab30-crm/compose.yaml up -d
    docker compose -f ../lab30-crm/compose.yaml ps

Topics are the lab 30 topics and this lab does not create them:

    docker exec crm-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic crm.customer-events.v1
    docker exec crm-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic crm.customer-events.v1.dlq

Tests, which need no broker because they run on EmbeddedKafka:

    mvn -B test

Application against the lab 30 broker:

    mvn -B spring-boot:run

Watch for `customer_event_published`, the listener's correlation line, and
`duplicate_event_ignored` on a replay.

Read the DLQ:

    docker exec crm-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic crm.customer-events.v1.dlq --from-beginning --property print.headers=true

Consumer lag for the notification group:

    docker exec crm-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group crm-notifications

## Frozen values

| Item | Value |
| ---- | ----- |
| Bootstrap (host) | `localhost:9092`, override `SPRING_KAFKA_BOOTSTRAP_SERVERS` |
| Bootstrap (Compose network) | `kafka:9092` |
| Primary topic | `crm.customer-events.v1` (3 partitions) |
| Dead letter topic | `crm.customer-events.v1.dlq` (1 partition) |
| Record key | `customerId` |
| Consumer group | `crm-notifications` |
| Sample correlation | `lab-request-001` |
| Event version | 1, rejected otherwise |

## Lab-only

PLAINTEXT with no auth, one broker, replication factor 1, and an in-memory
processed-event store. Production needs TLS with authn and authz on the topic,
RF=3 with `min.insync.replicas=2` so `acks=all` waits on more than one replica,
a durable idempotency store shared across instances, and an alert on DLQ depth.
A dead letter topic nobody watches is the same as dropping the record.
