Module 15: Lab 15 transition matrix (exercise 3)


REFERENCE MATRIX

| From | To | Allowed? |
| --- | --- | --- |
| PROSPECT | ACTIVE | yes, Ravi activate |
| ACTIVE | ACTIVE | reject |
| ACTIVE | PROSPECT | no |

SUSPENDED and CLOSED exist in CustomerStatus but are unruled in this lab.


STEP 2, AMINA

CUS-1001 is already ACTIVE, so activate on her is rejected.


STEP 3, ILLEGAL LIST

1. ACTIVE to PROSPECT.
2. ACTIVE to ACTIVE.

both throw a domain exception carrying a stable error code.


STEP 4, BOUNDARY

HTTP status-code mapping for these exceptions waits for lab 16.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab15-transition-matrix.md | Pass |
| 2 | Matrix filled | Pass, three rows, ACTIVE to ACTIVE is reject |
| 3 | Amina case noted | Pass, under STEP 2 |
| 4 | Lab 16 mapping deferred | Pass, under STEP 4 |
