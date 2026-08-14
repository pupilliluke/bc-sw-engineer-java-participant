# Lab 34 — State notes

## Lifted state

App owns customers, query, mode, draft and errors. the list, the search box
and the form all read or change the same records, so the state has to sit in
their closest common parent, a card cannot answer whether some other card is
being edited. mode is a discriminated union, list or create or
edit-with-customerId, one value instead of an isCreating and isEditing pair,
so create and edit cannot both be active and the edit variant is the only one
that carries an id. presentation components stay props-driven, the form
receives draft, errors and callbacks and owns nothing.

## Derived state ban

visible is computed during render from customers and query, it is not state.
storing a filtered copy in useState and syncing it with an effect re-renders
on every pass because the filter allocates a new array each time, setState
schedules another render and the loop never settles. tried as failure
experiment 2, the test run hung until it was killed. if a value can be
computed from existing state, compute it in render.

## Validation

client validation is UX only. validateCustomerDraft runs on submit and
returns a field-to-message map, typing in a field clears only that field's
message. nothing here protects the data, the browser is the user's machine.
Lab 35 adds the API and server-side 400s on top, shown through the same
error state.
