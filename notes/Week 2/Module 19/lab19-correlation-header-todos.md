Module 19: Lab 19 fill correlation header TODOs (exercise 6)


Header name: X-Correlation-Id
Header value for lab: lab-request-001
IT call must attach header? yes
UI journey logs correlation? optional
Flake mitigation idea: explicit waits and stable testids
Actuator in this pre-lab? no, Lab 21

the IT blank is yes because the facade has required a non-blank correlation id
since lab 15 and every ErrorResponse echoes it back. the UI journey asserts
the status text the page shows, the id belongs to the API calls and the
server log.


CI NOTE

CI agents need browser driver management, expect flake without waits.


SCOPE

pre-lab only.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab19-correlation-header-todos.md | Pass |
| 2 | All blanks replaced | Pass, six of six |
| 3 | X-Correlation-Id named | Pass, first blank |
| 4 | Actuator = no | Pass, last blank |
