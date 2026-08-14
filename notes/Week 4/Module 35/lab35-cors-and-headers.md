# Lab 35 — CORS and Headers

## Step 1 — Origins

UI `http://localhost:5173` (Vite), API `http://localhost:8080` (Spring).
different ports are different origins.

## Step 2 — CORS

the browser blocks cross-origin XHR unless Spring's CORS config allows the
UI origin, http://localhost:5173 exactly, port included. a * allowlist in
production means any site open in a user's browser can call the API with
that user's session. curl working while the browser fails means the API is
fine and the CORS config is what to look at, CORS is browser enforcement.

## Step 3 — Correlation

send `X-Correlation-Id: lab-request-001` on fetches, the same id the CRM
logs have carried since the Spring labs, so a UI action can be matched to
its server log line.

## Step 4 — Secrets

frontend env holds only the public API base URL (VITE_CRM_API_URL). no DB
passwords, no signing secrets, anything in Vite env ships to every
browser. auth headers wait for lab 36, keep the fetch helper injectable.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab35-cors-and-headers.md`
- [ x ] Origins stated
- [ x ] Correlation header planned
- [ x ] No-secrets rule written
