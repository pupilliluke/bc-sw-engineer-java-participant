# Lab 34 — Controlled Form Sketch

## Reference

| UI piece | State field |
| --- | --- |
| Name input | name |
| Status select | status |
| Error text | error |
| Submit disabled | isValid derived |

## Step 2 — Flow

1. render, this is the ui, the user sees the current state
2. onChange, calls setName() for every keystroke. No validation.
3. validate, this is the business logic, it checks the state every render, isValid is recomputed from current state
4. onSubmit, user-initiated event that re-checks and writes error if needed

## Step 3 — Fixture

Draft: name `Ravi Singh`, status `ACTIVE` before submit assigns `CUS-1002`
(server later). the draft has no customerId until the server issues one.

## Step 4 — Uncontrolled note

 uncontrolled refs are out of scope for this lab path.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab34-controlled-form.md`
- [ x ] Table present
- [ x ] Four flow steps
- [ x ] Fixture example included
