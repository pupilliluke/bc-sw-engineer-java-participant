# Lab 35 — Fetch Flow

## Step 1 — States

`idle | loading | success | error` for the list view.

| State | List view shows |
| --- | --- |
| idle | nothing requested yet, first render before the effect runs |
| loading | loading indicator, no rows |
| success | Amina and Ravi from the API, or the empty state when [] |
| error | plain-language message, no rows |

one state value, same shape as the UiMode union in lab 34, the view cannot
be loading and error at the same time.

## Step 2 — Sequence

Mount → set loading → fetch → set data (Amina/Ravi) or error message.

the mount effect sets loading, calls GET /api/customers and lands in
exactly one of success with data or error with a message. the seeds stop
living in seedCustomers.ts, the same two fixtures now arrive over the
network.

## Step 3 — Abort

Note AbortController on unmount to avoid setState after navigate away.

the effect creates an AbortController, hands its signal to fetch, and the
cleanup calls abort(). cleanup runs on unmount and before every re-run, so
it also cancels a stale in-flight request when a new one starts. without it
fast typing in search lets an old slow response land last and overwrite
newer results, and an unmounted component gets setState called on it.

## Step 4 — Empty

Draft empty-state copy when API returns [].

"No customers yet. Create your first customer to get started."

[] is a success response, the request worked and zero rows exist, so this
renders in the success state, not the error state. different from lab 34's
"No customers found", which is a search miss over rows that do exist.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab35-fetch-flow.md`
- [ x ] Four states named
- [ x ] Abort noted
- [ x ] Empty copy drafted
