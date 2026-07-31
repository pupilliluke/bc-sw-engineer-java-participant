# Lab 16 — Fill Message Hygiene TODOs

## Step 1 — Copy TODOs

Safe not-found message:  Customer CUS-9999 not found
Unsafe message anti-pattern:  Customer CUS-9999 not found (with internal sql log details)
Correlation always field: correlationId
Log stack trace? yes (server logs yes/no)
Return stack trace to client? no
@ControllerAdvice live in this pre-lab? no

## Step 2 — Fill blanks

Fill safe message for unknown customer, unsafe SQL/PII example, `correlationId`, yes for server logs, no for client, no for live advice.

## Step 3 — Correlation always

Every error sketch includes lab-request-001, or the incoming request header value.

## Step 4 — Self-check

Confirm client stack-trace blank is no.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.