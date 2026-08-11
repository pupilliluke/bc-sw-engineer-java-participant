# Lab 30 — Producer Checklist

## Step 1 — Settings list

Write a checklist: `acks=all`, idempotent producer, key = customerId, value = JSON envelope.

- acks=all
- enable.idempotence=true
- key = customerId (CUS-1001 / CUS-1002)
- value = JSON envelope from the envelope sketch
- bootstrap: localhost:9092 from the host, kafka:9092 inside the Compose network

## Step 2 — Why acks=all

One sentence: wait for ISR ack before considering the CRM event durable.

The send isn't treated as done until every in-sync replica has written it, so a broker dying straight after
the ack can't lose a CustomerCreated. acks=0 would return immediately and drop it on a broker restart.

## Step 3 — Idempotence

One sentence: broker dedupes producer retries so Amina is not double-created in the log.

The producer tags records with a producer id and sequence number, so a retry after a lost ack is recognised
by the broker and written once. CUS-1001 doesn't appear twice on the topic.

## Step 4 — Out of scope today

Mark: *Do not run `kafka-console-producer` in this pre-lab.*

Do not run `kafka-console-producer` in this pre-lab.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab30-producer-checklist.md`
- [ x ] acks + idempotence + key listed
- [ x ] Two why-sentences written
- [ x ] Explicit no-run note present
