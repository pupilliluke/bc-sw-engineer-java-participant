Northstar CRM React UI (Lab 33)

  cd crm-ui
  npm install
  npm run test -- --run
  # expect Test Files 1 passed, Tests 4 passed

  npm run dev
  # Vite on http://localhost:5173, dashboard shows Amina and Ravi

  npm run build
  # expect dist/ built, no type errors

Copied from the lab 33 starter. The scaffold, Vitest config, types file and
component stubs came with it, the component bodies, seed fixtures and the
four RTL tests are the lab work.

AppLayout owns the single main landmark. The customer grid is
repeat(auto-fit, minmax(260px, 1fr)), two columns at desktop and one column
at 375px with no horizontal scroll.

To see the loading and error shells, import LoadingState or ErrorState in
App.tsx and render one in place of the list, screenshots in
java-bootcamp/notes/screenshots/lab-33/. The Retry stub logs lab-request-001.

The GUIDE hard gate names Node 22+. Built and tested here on Node 20.18.0,
everything runs, Vite 5 requires 18+.

TIMED-PATH PASS CRITERIA

| Criterion | Result |
| --------- | ------ |
| Dashboard shows Amina and Ravi | Pass |
| List keys use customerId (not index) | Pass |
| RTL test queries by role | Pass |
| build succeeds | Pass |

CLEANUP

  # stop Vite with Ctrl+C
  git status --short

node_modules/ and dist/ are ignored. Keep lab33-crm, lab 34 copies it and
lifts state into App.

NOTES

Keys, a11y and the lab 34 handoff are in crm-ui/docs/component-notes.md.
Evidence and the failure experiments are in
java-bootcamp/notes/screenshots/lab-33/. Checkpoints and reflection answers
are in notes/Week 4/Module 33/lab33-answers.md. Full GUIDE at
labs/Week 4 - Kafka, React, PostgreSQL and Resilience/module-33/lab33/.
