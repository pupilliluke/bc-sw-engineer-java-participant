# Lab 30 — Why Async for CRM

## Step 1 — List sync pain

Customer service creates `CUS-1001` Amina Khan over HTTP with correlation `lab-request-001`. List **three** problems if it also calls 

Time to respond, operations out of order, 1 operation fails and whole request fails.


## Step 2 — Event idea

In one sentence, describe publishing a `CustomerCreated` event so other teams consume independently.

CustomerCreated will publish to kafka on async createCustomer completes through a broker. 3rd party services will consume from Kafka.

## Step 3 — Coupling check

Mark true/false: *The Customer JVM must be up for the Audit consumer to process an already-published event.*

No coupling between Kafka and Customer service. Not necessary, False


A short note contrasts sync fan-out with Kafka publish-and-forget for CRM in `notes/lab30-prelab-eda.md`.

Kafka operates through a broker, allowing decoupled services to consume events independently, while synchronous fan-out 
requires all services to be available and can lead to cascading failures.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab30-prelab-eda.md`
- [ x ] Three sync pain points named
- [ x ] One clear event-driven sentence
- [ x ] True/false answered with a one-line reason

