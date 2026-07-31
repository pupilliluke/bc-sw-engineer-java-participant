AI review log (Lab 17)

lab17-001, 2026-07-31, manual.

No Copilot suggestion was accepted into this suite. The tests were written by
hand, and the guide's acceptance checklist was run over them anyway, because the
checklist is about the tests rather than about who typed them.

| # | Check | Result |
| --- | --- | --- |
| 1 | Can every assert fail if production regresses? | Pass after the isAfter change below, see the red-green run |
| 2 | Shared CRM fixture ids, no real PII | Pass, CUS-1001, CUS-1002, CUS-9999 and example.com addresses |
| 3 | No phantom Spring or JPA imports | Pass, JUnit and project classes only |
| 4 | Independent @BeforeEach | Pass, a new repository, validator and service per test |
| 5 | mvn -B test after edits | Pass, 83 tests, run twice and in reverse order |

REJECTED

assertNotNull on its own. Lab 16 left addCustomerStampsTimestampsAndIsFoundById
asserting assertNotNull(created.getCreatedAt()), which passes for any non-null
value, including a timestamp copied from the caller's payload. It is kept only
because the same test then reads the customer back by id and asserts the name;
the null check alone would not have failed on any bug this lab could introduce.

assertTrue(!updatedAt.isBefore(createdAt)) in
activationMovesUpdatedAtWithoutTouchingCreatedAt, rejected on check 1. The
service stamps createdAt and updatedAt from one LocalDateTime.now(), and two
now() calls in the same test land in the same clock tick on this machine, so a
changeStatus that never restamped updatedAt left the two equal and the assert
still passed. Deleting existing.setUpdatedAt(LocalDateTime.now()) from
changeStatus left all sixteen tests green. The test now arranges createdAt
five minutes back and asserts isAfter, which fails on that deletion.

Nothing else in the suite asserts non-nullness as its only claim. Where an
exception is expected, the test asserts the code, the status hint and the
correlation id rather than the type alone, because BusinessException is thrown
by every rule in the validator and the type does not say which one fired.

RED-GREEN, WHICH IS WHAT CHECK 1 MEANS

changeStatus was temporarily changed to set the status before validating the
transition. Four tests in CustomerServiceTests and two in CustomerApiFacadeTest
failed, and the messages named the defect:

  illegalTransitionLeavesStoredStatusUnchanged
  the stored status must be unchanged after a rejected transition
  ==> expected: <ACTIVE> but was: <PROSPECT>

  activateRaviMovesProspectToActive
  BusinessException: illegal status transition ACTIVE -> ACTIVE

Mutating first makes the validator read the new status as the current status,
so a legal transition is reported as an illegal same-status one. Restored.

IF COPILOT IS USED LATER

The prompt would name the fixtures, the exception type and the constraint of no
Spring, and the same five checks would run before anything is accepted. A
generated test that ends at assertNotNull passes the build and raises the
coverage number without proving a rule.
