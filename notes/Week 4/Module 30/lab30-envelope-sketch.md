# Lab 30 — Event Envelope Sketch

## Step 1 — Headers

List envelope fields you will use: `eventType`, `eventVersion`, `occurredAt`, `correlationId`, `customerId`, `payload`.

eventType, eventVersion, occurredAt, correlationId, customerId, payload. Same six on every event, only
payload changes per event type. customerId sits in the envelope and is also the record key.

## Step 2 — Amina sample

in this notes file., sketch `CustomerCreated` for `CUS-1001` Amina Khan with `correlationId=lab-request-001`.

```json
{
  "eventType": "CustomerCreated",
  "eventVersion": 1,
  "occurredAt": "2026-08-11T09:15:00Z",
  "correlationId": "lab-request-001",
  "customerId": "CUS-1001",
  "payload": {
    "name": "Amina Khan",
    "status": "ACTIVE"
  }
}
```

key = CUS-1001, matches data.customerId.

## Step 3 — Ravi sample

Sketch `CustomerStatusChanged` for `CUS-1002` Ravi Singh (`ACTIVE` → `SUSPENDED` or similar).

```json
{
  "eventType": "CustomerStatusChanged",
  "eventVersion": 1,
  "occurredAt": "2026-08-11T09:20:00Z",
  "correlationId": "lab-request-001",
  "customerId": "CUS-1002",
  "payload": {
    "oldStatus": "PROSPECT",
    "newStatus": "ACTIVE"
  }
}
```

key = CUS-1002.

## Step 4 — Compatibility note

Write one rule: consumers must ignore unknown payload fields (forward compatible).

Consumers ignore payload fields they don't recognise. A producer can add a field without breaking anyone,
so it stays v1. Only removing or renaming a field forces eventVersion 2 / a .v2 topic.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab30-envelope-sketch.md`
- [ x ] Both event types sketched
- [ x ] customerId and correlationId present
- [ x ] Forward-compat rule written
