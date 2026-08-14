Northstar CRM React UI (Lab 34)

  cd crm-ui
  npm install
  npm run test -- --run
  # expect Test Files 1 passed, Tests 7 passed

  npm run dev
  # Vite on http://localhost:5173, tab title CRM (2), Amina and Ravi render

  npm run build
  # expect dist/ built, no type errors

Copied from the lab 34 starter. The scaffold, Vitest wiring, types file,
seed fixtures, form stub and validation stub came with it. The lab work is
the query state and controlled search, the derived visible list, the Edit
buttons that enter edit mode and seed the draft, handleFieldChange with
per-field error clearing, the validation rules, the create/edit/cancel
handlers, the document.title effect and the seven RTL flow tests.

App is the single source of truth, customers, query, mode, draft and errors.
mode is a discriminated union so create and edit cannot both be active.
Create appends immutably with crypto.randomUUID(), edit maps by customerId
and keeps the original id, cancel resets draft, errors and mode without
touching customers. State reasoning is in crm-ui/docs/state-notes.md.

The GUIDE hard gate names Node 22+. Built and tested here on Node 20.18.0,
everything runs, Vite 5 requires 18+.

TIMED-PATH PASS CRITERIA

| Criterion | Result |
| --------- | ------ |
| Create path adds a customer in UI state | Pass |
| Edit path updates existing by customerId | Pass |
| Invalid submit shows field errors | Pass |
| Cancel discards draft | Pass |

CLEANUP

  # stop Vite with Ctrl+C
  git status --short

node_modules/ and dist/ are ignored. Keep lab34-crm, lab 35 swaps the seed
fixtures for typed fetch against Spring.

NOTES

Evidence and the failure experiments are in
java-bootcamp/notes/screenshots/lab-34/. Checkpoints and reflection answers
are in notes/Week 4/Module 34/lab34-answers.md. Full GUIDE at
labs/Week 4 - Kafka, React, PostgreSQL and Resilience/module-34/lab34/.
