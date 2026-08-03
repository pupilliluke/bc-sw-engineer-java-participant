Module 18: Lab 18 when to keep real validator (exercise 1)


DECISION TABLE

| Collaborator | Decision | Why |
| --- | --- | --- |
| CustomerRepository | mock | I/O boundary, the port the service writes through |
| CustomerValidator | real | holds the rule under test, deterministic on this path |
| notifier, not built yet | mock | network side effect, and the never() case has to be assertable |


STEP 1, MOCK REPO

CustomerRepository is the persistence port and the thing that gets replaced.
in my CRM the only implementation is InMemoryCustomerRepository, a HashMap, so
the argument is not speed, it is control and visibility. a mock lets findById
return an empty Optional for CUS-9999 without arranging anything, and it turns
save into something the test can verify and capture. lab 17 wired the real
in-memory repository and had to read the customer back to prove the write
happened, which asserts on storage rather than on the call the service made.


STEP 2, REAL VALIDATOR?

CustomerValidator stays real, with one honest qualification. it is not a pure
helper, its constructor takes CustomerRepository and validateNew calls
existsById and existsByEmail for the uniqueness rules. validateTransition is
pure, it reads the static ALLOWED EnumMap and never touches the repository.
activate goes through changeStatus, which calls validateTransition only, so on
that path the real validator is deterministic, fast and self-contained, which
is exactly the deck's condition for keeping a collaborator real.

it is still constructed on the mocked repository, the same wiring production
uses, so the addCustomer tests keep working, they just stub existsById and
existsByEmail on the mock to drive the duplicate rules.

mocking it would be the mistake. the transition table is the rule the activate
tests exist to check, and a stubbed validateTransition that does nothing would
let PROSPECT to ACTIVE pass for the wrong reason.


STEP 3, MOCK NOTIFIER

there is no notifier in the CRM through lab 17, nothing sends email. if one
arrives it goes in as an app-owned port, sendActivated(Customer) on an
interface I own, and the port is mocked rather than any vendor client. a real
send is network I/O, slow, non-deterministic and a side effect that outlives
the test. mocking it also makes the negative assertable, a rejected transition
has to leave verify(notifier, never()).sendActivated(any()) satisfied.


STEP 4, RULE

mock I/O and unstable dependencies, keep pure domain helpers real when they are
cheap. the sharper question is which collaborator holds the rule under test:
that one stays real, whatever it costs to arrange.


SCOPE

pre-lab only, do not finish the full graded lab in this exercise.


SELF-CHECK

fixtures confirmed, Amina CUS-1001 ACTIVE, Ravi CUS-1002 PROSPECT, correlation
lab-request-001.

| # | Confirm | Result |
| --- | --- | --- |
| 1 | File exists, lab18-keep-real-validator.md | Pass |
| 2 | Repo mock justified | Pass, under STEP 1 |
| 3 | Validator real justified | Pass, under STEP 2 |
| 4 | Notifier mock justified | Pass, under STEP 3 |
