# Lab 31 — Error and DLT Notes

## Step 1 — Retryable

Example: transient network blip calling email API — retry.

The record is fine, the thing the listener depends on is not. A timeout or a
503 from the email API, or the broker being unreachable mid-poll. The same
record on the same input will succeed once the other side recovers, so back off
and try again. Lab 30 experiment 1 is this shape, the producer blocked for
60 seconds against a stopped broker and succeeded on the next run with nothing
about the record changed.

## Step 2 — Non-retryable

Example: JSON missing `customerId` — DLT after limited attempts.

The record itself is wrong, so every attempt fails the same way. Missing
customerId, a value that will not deserialize, an eventType the listener has no
branch for, or the key not matching data.customerId. Lab 30 put a record on the
topic keyed CUS-1001 whose payload said CUS-1002 and nothing rejected it, so
that one is real rather than hypothetical. Retrying it forever blocks the
partition, every later record behind it waits on a record that will never
succeed, so it goes to the DLT after a small fixed number of attempts and the
offset commits past it.

## Step 3 — Ops note

Write: support replays DLT after fixing the consumer, using correlation `lab-request-001`.

Support replays from the DLT after the consumer is fixed, not before, because
the record was parked for a reason that a redelivery on its own will not
change. correlationId is how they find it, lab-request-001 ties the DLT record
back to the HTTP request that created Amina and to the log line the listener
wrote when it failed. The replay has to be idempotent for the same reason
every consume does, which is the idempotency plan exercise.

The DLT name needs deciding before lab 31. Spring's
DeadLetterPublishingRecoverer defaults to the source topic plus .DLT, so
crm.customer-events.v1.DLT, and lab 30 froze crm.customer-events.v1.dlq. Two
names for one thing means half the records land somewhere nobody is watching,
so the recoverer gets pointed at the lab 30 name.

## Step 4 — No runtime

Confirm you will not publish to DLT from CLI in this pre-lab.

Not publishing to the DLT from the CLI in this pre-lab. The topic exists from
lab 30 and stays empty until the listener routes to it.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab31-error-dlt-notes.md`
- [ x ] Retryable example
- [ x ] Non-retryable example
- [ x ] Replay/ops sentence
