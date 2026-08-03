Module 18: Lab 18 stub vs verify (exercise 2)


STEP 1, STUB

Ravi CUS-1002, stored as PROSPECT, arranged on the mock instead of saved into a
repository:

    when(repository.findById("CUS-1002"))
            .thenReturn(Optional.of(ravi));

that is arrange. it decides what the service reads, nothing is asserted yet.

changeStatus needs a second stub that the deck's one-liner does not show,
because it returns repository.save(existing):

    when(repository.save(any(Customer.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

an unstubbed mock method returns a default, so save would hand back null and
changeStatus would return null. the test would then fail on a
NullPointerException rather than on the rule, or worse, pass an assertNull.
returning the argument keeps the mock behaving like the port's contract, save
gives back what it stored.


STEP 2, VERIFY

    verify(repository).save(ravi);

that is assert. it proves the service actually called the port once with that
customer, which no amount of reading state back can show.

one caveat that matters in my CRM: Customer.equals compares customerId alone,
so save(ravi) matches whatever status the argument carries. this verify proves
save happened for CUS-1002, not that the saved customer was ACTIVE. the status
belongs in an assert on the returned customer or in a captor, exercise 3.

the negative is the other half:

    verify(repository, never()).save(any(Customer.class));

for CUS-9999, where findById is stubbed to Optional.empty() and the not-found
exception is thrown before any write. lab 17 could only argue that from a
count of stored customers.


STEP 3, BOTH

stubs feed the inputs the unit reads, verifies prove the side-effect calls it
made, and the order is fixed, a when() written after the call under test never
applies to a call that already happened.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab18-stub-verify.md | Pass |
| 2 | Stub example written | Pass, CUS-1002 under STEP 1 |
| 3 | Verify example written | Pass, under STEP 2 |
| 4 | Contrast sentence present | Pass, under STEP 3 |
