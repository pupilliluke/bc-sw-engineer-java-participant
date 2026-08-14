# Lab 33 — Component notes

## List keys

`CustomerList` keys every card with `customer.customerId`. React matches
children across renders by key, and the key has to be the identity of the data,
not the position in the array. With `key={index}` a sort or filter changes
which customer sits at which index, React reuses the wrong component instances,
and anything the card holds outside props, focus, an uncontrolled input, stays
at the old position while the data moves under it. `customerId` comes from the
server and never changes, so identity follows the data.

## A11y

Status is text first. `StatusBadge` renders the label from a
`Record<CustomerStatus, string>`, so every status has visible text and the
color class is decoration on top, the badge still works in grayscale. The
record is also the exhaustiveness check, a new status without a label is a
compile error.

Every form input has a `<label htmlFor>` paired with the input `id`, which is
the same association a screen reader announces and the reason
`getByLabelText("Full name")` works in the tests.
Experiment 3 in notes/screenshots/lab-33/02-failure-experiments.txt shows the
test failing when the pairing breaks.

`AppLayout` owns the one `main` and the `h1`, no other component renders a
`main`. The empty list branch returns `EmptyState` with
visible text instead of an empty grid, so there is no section and heading
wrapping nothing in the accessibility tree.

The loading and error shells differ by role, `LoadingState` is `role="status"`
and `ErrorState` is `role="alert"` with an optional Retry button, so the two
are distinguishable to assistive tech, not just by copy.

## Lab 34 handoff

Everything here is presentational, state arrives in Lab 34 without markup
changes:

- `CustomerForm` is controlled. It renders `draft` and reports every edit
  through `onChange(next)`, it holds no state of its own. Lab 34 replaces
  App's `emptyDraft` constant and no-op handlers with `useState`.
- `CustomerList` and `CustomerCard` take data and callbacks only. `onEdit`
  currently dead-ends in a console.log with lab-request-001, Lab 34 points it
  at real state.
- `LoadingState` / `ErrorState` render one request state each. Lab 35 swaps
  the seed fixtures for fetches and chooses which shell to show, the toggle
  in the README shows both today.
- `CustomerDraft` omits `customerId` on purpose, drafts do not have a server
  id until a save round-trips.
