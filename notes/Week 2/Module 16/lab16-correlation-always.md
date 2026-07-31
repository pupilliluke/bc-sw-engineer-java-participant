Module 16: Lab 16 correlation on every error (exercise 5)


STEP 1, SUCCESS PATH

activate Ravi CUS-1002 succeeds and the response still echoes correlationId
lab-request-001, the service logs it on the same request.


STEP 2, FAILURE PATH

not found CUS-9999 returns the same correlationId field, lab-request-001, same
field name and value as the success path.


STEP 3, MISSING HEADER

policy, generate a correlation id server side when the incoming header is
missing, and use it in both the log line and the response body so the client
can quote it back. note for later labs, not wired now.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab16-correlation-always.md | Pass |
| 2 | Success path noted | Pass, under STEP 1 |
| 3 | Failure path noted | Pass, under STEP 2 |
| 4 | Missing-header policy idea written | Pass, under STEP 3 |
