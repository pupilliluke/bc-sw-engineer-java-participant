Module 15: Lab 15 activate Ravi TODOs (exercise 5)


STEP 1 AND 2, BLANKS FILLED

    customer = repo.findById("CUS-1002")
    if customer is null       -> throw CustomerNotFoundException("CUS-1002")
    if status is not PROSPECT -> throw a domain transition exception
    set status to ACTIVE
    repo.save(customer)
    log correlation lab-request-001

seven blanks, none left.


STEP 3, REPO BOUNDARY NOTE

the repository saves state, it does not decide PROSPECT to ACTIVE.


STEP 4, SELF-CHECK

Ravi starts PROSPECT and ends ACTIVE.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001. no blanks left.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab15-activate-ravi-todos.md | Pass |
| 2 | All blanks replaced | Pass, seven under STEP 1 AND 2 |
| 3 | PROSPECT to ACTIVE correct | Pass, Ravi is the demo path |
| 4 | Repo boundary sentence present | Pass, under STEP 3 |
