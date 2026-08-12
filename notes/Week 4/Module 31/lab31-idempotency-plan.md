# Lab 31 — Idempotency Plan

## Step 1 — Why duplicates

List two causes: producer retry, consumer rebalance/reprocess.

1. Producer retry. A send whose ack is lost gets retried, and without
   enable.idempotence the broker has no way to tell the retry from a new record
   and appends it twice.
2. Consumer rebalance or reprocess. Offsets commit after the listener returns,
   so a consumer that does its work and then dies, or gets kicked out of the
   group before the commit, leaves the offset where it was and the next member
   reads the same records again.

Replay is the third one and it is deliberate. Lab 30 experiment 3 pointed a new
group id at the topic with --from-beginning and it read all 27 records, every
one of which had already been delivered to crm-notifications and crm-audit.

## Step 2 — Business key

Propose an idempotency key, e.g. `eventId` or `customerId+eventType+occurredAt` for `CUS-1001`.

eventId. It is already in the envelope from lab 30, the producer sets it once,
and it survives replay and rebalance because it is a property of the event
rather than of the delivery. customerId+eventType+occurredAt is the fallback if
an envelope arrives without one, but it collides when a customer has two events
of the same type in the same second, and lab 30 wrote CustomerCreated and
CustomerStatusChanged for CUS-1001 with occurredAt one at 06:00:00Z and one at
06:05:00Z only because they were set by hand.

Not the offset. Offsets are per partition and per group, they repeat on replay,
and the same event read by crm-notifications and crm-audit has the same offset
in both, so an offset does not identify an event.

## Step 3 — Store idea

One sentence: check a processed-events table/set before side effects (email).

Check eventId against a processed-events store before the side effect, skip the
record if it is already there, and write it after the side effect succeeds.

Marking before the side effect loses events, a crash between the mark and the
send means the notification never goes out and the retry sees the event as
already handled. Marking after duplicates them, a crash between the send and
the mark means the next delivery sends a second email. Neither is exactly once,
so the choice is which failure is cheaper, and for a notification a rare
duplicate beats a silent miss.

## Step 4 — Out of scope

Do not implement the table yet — paper design only.

Paper design only, no table in this pre-lab. A Set in memory is enough for lab
31 on one instance, but it empties on restart and is not shared, so two
instances would each notify once for the same event. Production needs a durable
store with a unique constraint on eventId and a retention window.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab31-idempotency-plan.md`
- [ x ] Two duplicate causes
- [ x ] Concrete key proposal
- [ x ] Processed-store idea stated
