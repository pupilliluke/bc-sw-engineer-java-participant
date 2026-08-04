Module 19: Lab 19 data-testid locators (exercise 2)


| Element | data-testid |
| --- | --- |
| Status | customer-status |
| Activate/submit | activate-customer / submit-customer |
| Customer id/name | customer-id / customer-name |


BRITTLE ALTERNATIVE

div.col-md-3 > span:nth-child(2), it encodes layout position, any markup or
grid change breaks it while the element it meant is still on the page.


CONTRACT NOTE

UI and tests share testids as a contract, a markup refactor that keeps the
testid breaks nothing.


SCOPE

pre-lab only.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab19-locators.md | Pass |
| 2 | Three testids | Pass, three rows in the table |
| 3 | Brittle example | Pass, under BRITTLE ALTERNATIVE |
| 4 | Contract note | Pass, under CONTRACT NOTE |
