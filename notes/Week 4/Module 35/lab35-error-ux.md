# Lab 35 — Error UX Copy

## Step 1 — 404

Message when `CUS-9999` not found.

"This customer could not be found. It may have been removed."

## Step 2 — Network

Message when API unreachable.

"Could not reach the server. Check your connection and try again."

no status code in the copy, an unreachable API is not a 500, no response
ever came back.

## Step 3 — 400

Message when name validation fails.

"Name is required." on the field, same client messages as lab 34. the
server's 400 detail maps into the same errors state, not a generic
try-again toast, a 400 is the user's input and retrying unchanged input
fails the same way.

## Step 4 — Logging

Dev console may show correlation id; users see plain language only.

console.error gets lab-request-001 and the status, the UI copy above never
shows codes, ids or stack traces. AbortError is not shown at all, a
cancelled request is not a failure.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab35-error-ux.md`
- [ x ] 404/network/400 messages
- [ x ] Correlation stays in logs note
- [ x ] No stack traces in UI copy
