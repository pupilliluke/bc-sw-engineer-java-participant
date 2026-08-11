Lab 30 — Kafka notes (timed path)

## Produce → consume

The console producer and the Java `CustomerEventProducer` both write a JSON
envelope to `crm.customer-events.v1` with `customerId` as the record key. The
broker appends it to the partition the key hashes to and returns the partition
and offset once the in-sync replica has written it. Consumers subscribe by
group and read forward from their own committed offset, so the same record is
delivered to every group but only to one member within a group.

## Keying

The key is what picks the partition, and Kafka only guarantees order inside a
partition. Keying by `customerId` puts every event for one customer on one
partition, so `CUS-1001` CustomerCreated at offset 0 is always read before
CustomerStatusChanged at offset 1. A null key would round-robin the two across
different partitions and the status change could be read first.

Same key means same partition, but different keys do not mean different
partitions. On this topic `CUS-1001` and `CUS-1002` both hash to partition 0.
`CUS-1003` lands on 1 and `CUS-1005` on 2, which is what the step 7 records
used to put data on all three.

## DLQ

`crm.customer-events.v1.dlq` exists but nothing writes to it in this lab. It is
created now so Lab 31 has the name frozen. Its purpose is to hold records the
consumer cannot process — malformed JSON, or an envelope missing `customerId` —
so the consumer can commit past them instead of retrying forever and blocking
every later record on that partition.

## Ordering and delivery semantics

Per-key ordering holds: the same `customerId` routes to the same partition and
that customer's events keep their relative order. There is no global order —
`CUS-1001` and `CUS-1002` events can interleave arbitrarily, and on a topic
where they hash to different partitions there is no way to say which happened
first from the log alone. Delivery is at-least-once, so a rebalance or a retry
can hand a consumer the same record twice, which is why Lab 31 has to
de-duplicate on `eventId` rather than assume one delivery. The DLQ is the
release valve for records that will never succeed, so one poison message does
not stop the group making progress.

## Frozen for Lab 31

| Item | Lab value |
| ---- | --------- |
| Bootstrap (host) | `localhost:9092` |
| Bootstrap (in Compose network) | `kafka:9092` |
| Primary topic | `crm.customer-events.v1` (3 partitions) |
| DLQ topic | `crm.customer-events.v1.dlq` (1 partition) |
| Record key | `customerId` (`CUS-1001`, `CUS-1002`) |
| Sample correlation | `lab-request-001` |
| Demo groups | `crm-notifications` (competing), `crm-audit` (independent) |
| Producer settings | `acks=all`, `enable.idempotence=true` |

## Runbook

Broker up, from `examples/lab30-crm`:

    docker compose up -d
    docker compose ps

Topics:

    docker exec crm-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic crm.customer-events.v1 --partitions 3 --replication-factor 1
    docker exec crm-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic crm.customer-events.v1.dlq --partitions 1 --replication-factor 1
    docker exec crm-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic crm.customer-events.v1

Produce, key before the first colon:

    docker exec -it crm-kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic crm.customer-events.v1 --property parse.key=true --property key.separator=:

Consume with metadata:

    docker exec crm-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic crm.customer-events.v1 --from-beginning --property print.key=true --property print.partition=true --property print.offset=true --property print.timestamp=true --max-messages 3

Java producer:

    mvn -B -q -DskipTests package
    mvn -B exec:java "-Dexec.mainClass=com.northstar.crm.event.CustomerEventProducer"

Lag:

    docker exec crm-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group crm-notifications

The `-D` argument needs the quotes on PowerShell. Without them PowerShell splits
at the dot and Maven reads `.mainClass=...` as a lifecycle phase.

## Lab-only

PLAINTEXT with no auth, one broker, and replication factor 1. A single disk loss
takes the partition with it and there is no follower to elect. Production is TLS
with authn and authz on publish and consume, RF=3 with `min.insync.replicas=2`
so `acks=all` has more than one replica to wait for, and auto topic creation
off so a typo cannot silently make a 1-partition topic.
