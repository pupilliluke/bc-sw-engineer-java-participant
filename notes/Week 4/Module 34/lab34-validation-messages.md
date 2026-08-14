# Lab 34 — Validation Messages

## Step 1 — Rules

Name required; status required; name min length 2.

## Step 2 — Messages

| Rule | Message |
| --- | --- |
| name required | Name is required |
| status required | Choose a status |
| name min length 2 | Name must be at least 2 characters |

## Step 3 — Timing

On submit. The exercise 1 sketch already validates there, setError lives in
onSubmit and there is no blur handler, so it is one check and one error state
instead of an onBlur and a touched flag per field. The user is not told off
for fields they have not reached yet.

## Step 4 — Server later

Lab 35 adds API 400 errors on top of these, shown through the same error
state, the messages above are client-side only.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab34-validation-messages.md`
- [ x ] Three messages
- [ x ] Timing chosen
- [ x ] Lab 35 boundary noted

