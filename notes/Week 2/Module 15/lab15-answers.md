Lab 15 service layer design (reflection questions, checkpoints, manual
verification, failure experiments)

built under examples\lab15-crm, copied forward from lab14-crm. a repository
interface with an in-memory adapter, a CustomerService interface with
DefaultCustomerService behind it, CustomerValidator holding the transition
table, three new test classes, forty tests green.


REFLECTION QUESTIONS

1. Which design decision most affected correctness?

calling validateTransition before setStatus. one line of ordering, and it is the
difference between a rejected request and a corrupted customer, because nothing
in an in-memory store rolls back a mutated object. the second decision was
giving the validator the same repository instance the service uses, which fails
silently when it is wrong.

2. What evidence proves the implementation works?

forty green tests, and specifically
illegalTransitionLeavesStoredStatusUnchanged, which asserts the stored status
after the exception rather than only that an exception was thrown. the Main
transcript shows activated CUS-1002 status=ACTIVE, the ACTIVE to PROSPECT
rejection carrying lab-request-001, and CUS-1001 still ACTIVE. output is under
examples\lab15-crm\notes\screenshots\lab-15.

3. Which failure was hardest to diagnose?

experiment 4, the split repository instances. nothing threw, no test failed, and
the suite was still forty green. the only symptom was a line in the Main
transcript reading no failure where a duplicate email should have been
reported, with two customers stored against the same address.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab15-crm under examples/ | Pass |
| A2 | CustomerRepository plus private-Map in-memory impl | Pass, LinkedHashMap, no getter |
| A3 | no Map exposed to callers | Pass, findAll returns a copy |
| B1 | CustomerService interface plus DefaultCustomerService | Pass |
| B2 | CustomerValidator with ALLOWED transitions | Pass, static EnumMap of EnumSet |
| B3 | shared repository instance in wiring | Pass, one repo in Main, proved by experiment 4 |
| C1 | CUS-1002 activates PROSPECT to ACTIVE | Pass |
| C2 | CUS-1001 ACTIVE to PROSPECT rejected, status unchanged | Pass |
| C3 | correlation id present on failure | Pass, lab-request-001 in the message |
| D1 | CustomerValidatorTest green | Pass, 7 tests |
| D2 | README transition table and wiring | Pass, plus docs/service-layer-notes.md |
| D3 | failure experiments recorded, no secrets or target/ staged | Pass, five, all restored |


MANUAL VERIFICATION

| # | Check | Result |
| --- | --- | --- |
| 1 | Amina ACTIVE and Ravi PROSPECT created | Pass |
| 2 | activate Ravi succeeds | Pass, activated CUS-1002 status=ACTIVE |
| 3 | illegal ACTIVE to PROSPECT fails with the correlation id | Pass |
| 4 | Amina still ACTIVE after the failure | Pass, asserted in the store, not just printed |
| 5 | duplicate customerId and email fail clearly | Pass, CUSTOMER_CONFLICT at the facade |
| 6 | no HashMap or SQL imports in service | Pass, and none in api either |
| 7 | listAll unmodifiable from the caller | Pass, UnsupportedOperationException |
| 8 | constructor DI graph explicit in Main | Pass, four lines |
| 9 | validator tests pass | Pass |
| 10 | README table matches the code | Pass, both say CLOSED is terminal and same-status is a rejection |


FAILURE EXPERIMENTS

1. made the repository save throw for CUS-1003. the failure surfaced, Amina and
Ravi were untouched, and the store was consistent. the finding was in the
reporting, the facade returned a storage outage as CUSTOMER_CONFLICT, because
a rule violation and an infrastructure failure both arrive as
IllegalStateException and a catch block cannot tell them apart. lab 16's typed
exceptions are the fix. restored.

2. CLOSED to ACTIVE and ACTIVE to PROSPECT. both rejected by the validator, both
messages carrying the from, the to and lab-request-001. no code change needed,
the table already says CLOSED is terminal.

3. activate twice. the second call is rejected as ACTIVE to ACTIVE rather than
ignored. that is the documented decision, and the cost is that changeStatus is
not idempotent, so a client retrying after a timeout cannot tell its own retry
from a real conflict. same gap lab 14 left on create.

4. gave the validator its own InMemoryCustomerRepository instead of the shared
one. duplicate email accepted, CUS-1004 stored with Amina's address, and every
one of the forty tests still passed because each of them builds its own wiring
correctly. the validator was asking an empty second store whether the address
was taken. restored.

5. moved setStatus above validateTransition. the rejected ACTIVE to PROSPECT
left Amina as PROSPECT, and the CLOSED to ACTIVE demo did the same to CUS-1003.
DefaultCustomerServiceTest caught it,
illegalTransitionLeavesStoredStatusUnchanged failed on the stored value.
restored, forty green.
