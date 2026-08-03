Module 18: Lab 18 ArgumentCaptor preview (exercise 3)


STEP 1, DECLARE

on paper, no test run yet:

    ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);


STEP 2, VERIFY

    verify(repository).save(captor.capture());

capture() stands where the expected argument would go, so the same call both
verifies save happened once and hands the argument to the captor.


STEP 3, ASSERT

Ravi CUS-1002, after changeStatus to ACTIVE:

    assertEquals("CUS-1002", captor.getValue().getCustomerId());
    assertEquals(CustomerStatus.ACTIVE, captor.getValue().getStatus());

this is the case the deck means by the argument being an observable outcome.
verify(repository).save(ravi) cannot do it, Customer.equals is customerId only,
so argument equality is blind to the status field the whole test is about.

one thing to keep in mind when I write it: changeStatus mutates the customer
findById returned and passes that same instance to save, so the captured value
is a reference to the stub's object rather than a snapshot taken at call time.
here it reads ACTIVE either way, because the transition is applied before save
and nothing touches the customer afterwards.


STEP 4, PREP ONLY

prepare for lab 18, do not complete the full Mockito lab now.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab18-argumentcaptor-preview.md | Pass |
| 2 | Declare/verify/assert sketched | Pass, STEP 1 to STEP 3 |
| 3 | ACTIVE asserted | Pass, for CUS-1002 under STEP 3 |
| 4 | Pre-lab boundary present | Pass, under STEP 4 |
