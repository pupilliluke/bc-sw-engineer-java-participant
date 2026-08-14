# Lab 34 — Event Handler Map

## Step 1 — Table

| Event | Handler | State updated |
| --- | --- | --- |
| name onChange | setName | name |
| status onChange | setStatus | status |
| form onSubmit | handleSubmit | error |
| row onClick, select Amina | selectCustomer | selectedCustomerId |

## Step 2 — Rows

handleSubmit updates error, not isValid, the failure path calls setError and
the success path has nothing to write until lab 35's POST. the row click
handler is selectCustomer(customerId) rather than a per-fixture function, and
it stores selectedCustomerId, the id points into the customers list so there
is only one copy of Amina to keep in sync.

## Step 3 — Derived

isValid is derived from name and status at render, not stored, so it can
never disagree with them. storing it would mean updating it in every handler
that touches name, and the first forgotten update makes the form lie.


| Event                      | Handler | State updated|
|----------------------------| --- | ---|
| `name` onChange            | `setName` | `name`|
|  `status` onChange         | `setStatus` | `status`|
| form onSubmit              | `handleSubmit` | `error`|
| row onClick → select Amina | `selectCustomer` | `selectedCustomerId`|

isValid is derived from name and status at render, not stored, so it can never disagree with them.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab34-event-handler-map.md`
- [ x ] ≥4 event rows
- [ x ] Select-Amina row included
- [ x ] Derived state note

