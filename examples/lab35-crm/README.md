Northstar CRM React + Spring integration (Lab 35)

Two projects. crm-api is the Spring backend (a copy of lab25-crm with the
lab 35 changes, the original lab25-crm is untouched). crm-ui is the React
SPA carried forward from lab34-crm.

START THE API FIRST

  cd crm-api
  mvn spring-boot:run
  # port 8080, in-memory store, seeds Amina CUS-1001 and Ravi CUS-1002
  curl -i http://localhost:8080/api/customers

THEN THE SPA

  cd crm-ui
  npm install
  cp .env.example .env
  npm run dev
  # Vite on http://localhost:5173, list loads from the API, title CRM (2)

  npm run test -- --run
  # expect Test Files 2 passed, Tests 16 passed, no API needed (fetch mocked)

  npm run build
  # expect dist/ built, no type errors

The lab work: ApiError (network | http | abort | parse), the request()
fetch boundary in src/api/http.ts with correlation header and 204/ok
guards, customersApi with id/name <-> customerId/fullName mapping,
LoadState (loading | data | error) with abortable load and Retry,
backend 400 fieldErrors mapped beside the form fields, saving flag
against duplicate POSTs, and on the API side CORS allowlist for
http://localhost:5173, a 404/409/400 exception handler, server-side
validation and PUT /api/customers/{id}. Details in
crm-ui/docs/api-integration-notes.md.

The store is in-memory, restarting crm-api resets it to the two seeds.

The GUIDE hard gate names Node 22+. Built and tested here on Node
20.18.0, everything runs, Vite 5 requires 18+.

TIMED-PATH PASS CRITERIA

| Criterion | Result |
| --------- | ------ |
| List loads from Spring through the typed helper | Pass |
| Loading, empty, data and error states distinct | Pass |
| Abortable load, no setState after unmount | Pass |
| 400 field errors land beside labels | Pass |
| Evil Origin rejected, Vite origin allowed | Pass |

CLEANUP

  # stop Vite and Spring with Ctrl+C
  git status --short

node_modules/, dist/, target/ and .env are ignored, .env.example is
committed. Keep lab35-crm, lab 36 adds tokens, route guards and the
security controls on top of this tree.

NOTES

Evidence is in java-bootcamp/notes/screenshots/lab-35/, the API contract
capture, both test runs, CORS transcripts and the failure experiments.
Checkpoints and reflection answers are in
notes/Week 4/Module 35/lab35-answers.md. Full GUIDE at
labs/Week 4 - Kafka, React, PostgreSQL and Resilience/module-35/lab35/.
