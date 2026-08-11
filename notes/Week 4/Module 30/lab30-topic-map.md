# Lab 30 — Topic and Key Map

## Reference

| Concept | Northstar choice |
| --- | --- |
| Main topic | crm.customer-events.v1 |
| DLQ topic | crm.customer-events.v1.dlq |
| Partitions (lab) | 3 |
| Record key | customerId (e.g. CUS-1001) |
| Future Account* topic | crm.account-events.v1 |

## Step 2 — Keying reason

Write why keying by `CUS-1001` / `CUS-1002` keeps a customer's events ordered within a partition.

The producer picks the partition from the key, so every event with key CUS-1001 goes to the same partition.
Kafka only guarantees order inside a partition, and one consumer in a group reads that partition, so Amina's
create and status change come out in offset order. CUS-1002 can land on a different partition, that's fine,
Ravi's events only need to be ordered against each other.

## Step 3 — Versioning

Explain what the `.v1` suffix buys the team when the payload schema changes later.

A breaking payload change goes to a new topic crm.customer-events.v2 instead of changing v1 under existing
consumers. Old consumers keep reading v1 until they migrate, producers can write both for a while.

## Step 4 — DLQ trigger

List two failure cases that should land a record in the DLQ (conceptual only).

1. The value won't deserialize - malformed JSON or missing envelope fields, retrying will never fix it.
2. Consumer keeps failing on the record after retries, e.g. unknown eventType. Park it on the DLQ so it
   doesn't block the rest of the partition.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab30-topic-map.md`
- [ x ] Main + DLQ topic names match the reference
- [ x ] Key = customerId justified
- [ x ] Two DLQ cases listed

