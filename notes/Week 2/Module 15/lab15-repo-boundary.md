Module 15: Lab 15 repository boundary (exercise 2)


STEP 1, REPO OWNS

CRUD by id, existence checks, persistence mapping. email uniqueness as the
final integrity guarantee.


STEP 2, SERVICE OWNS

the transition matrix, notifier calls, domain exceptions.


STEP 3, ANTI-PATTERN

repo.activateCustomer(id), a repository method that hides a business rule. the
correct split is findById, the transition decision on the service, then save.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab15-repo-boundary.md | Pass |
| 2 | Repo responsibilities listed | Pass, under STEP 1 |
| 3 | Service responsibilities listed | Pass, under STEP 2 |
| 4 | Anti-pattern named | Pass, repo.activateCustomer under STEP 3 |
