Module 18: Lab 18 activate interaction sequence (exercise 4)


STEP 1, COPY SEQUENCE

    1) stub findById("CUS-1002") -> ravi PROSPECT
    2) call service.changeStatus(...)
    3) verify repo.save(customer)
    4) verify notifier.sendActivated(...)  // if present
    5) assert status ACTIVE
    6) ArgumentCaptor previews status field ACTIVE


STEP 2, FILL BLANKS

six of six replaced: CUS-1002, changeStatus, save, sendActivated, ACTIVE,
ACTIVE.

the deck's blank for step 2 is activate. my service has no activate method, the
activate path is changeStatus("CUS-1002", CustomerStatus.ACTIVE,
"lab-request-001"), one method covering every transition the table allows.

step 3 is save, not update. CustomerRepository has five methods, save,
findById, existsById, existsByEmail and findAll, and changeStatus writes
through save with the customer it just mutated.

step 4 is the if present branch. no notifier exists in my CRM through lab 17,
so nothing is verified there yet. the negative I do have is
verify(repository, never()).save(any(Customer.class)) for CUS-9999, where
findById is stubbed empty and the not-found exception is thrown before any
write.

step 1 also needs save stubbed to return its argument, otherwise the mock
returns null and step 5 asserts on a null customer.


STEP 3, CAPTOR PREVIEW

captors prove the saved Customer carried ACTIVE, not only that save was called,
which matters here because Customer.equals compares customerId alone and would
match the argument whatever status it held.


STEP 4, SELF-CHECK

step 1 id reads CUS-1002 and the status in steps 5 and 6 reads ACTIVE. Ravi
starts PROSPECT, PROSPECT to ACTIVE is in the allowed table, so the transition
is the legal one and not a conflict.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab18-activate-interaction-todos.md | Pass |
| 2 | All blanks replaced | Pass, six of six under STEP 1 |
| 3 | Captor benefit sentence present | Pass, under STEP 3 |
| 4 | Ravi path correct | Pass, CUS-1002 PROSPECT to ACTIVE under STEP 4 |
