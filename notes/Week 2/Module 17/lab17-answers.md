Lab 17 JUnit testing with AI assistance (reflection questions, checkpoints,
manual verification, failure experiments)

built under examples\lab17-crm, copied forward from lab16-crm. no production
class changed. JaCoCo 0.8.12 gating com.northstar.crm.service at LINE 0.80,
CustomerServiceTests 16 and CustomerValidatorParameterizedTest 23 added,
eighty-three tests green, service package at 46 of 46 lines and 20 of 20
branches.


REFLECTION QUESTIONS

1. Which design decision most affected correctness?

putting the transition table in a CsvSource and an EnumSource rather than in
copied test methods. fifteen rows plus eight enum cases cover every pair the
validator can be asked about, and a missing row is visible next to the others.

2. What evidence proves the implementation works?

not the coverage number. dropping the three service suites still leaves the
package at 0.84, because the facade tests reach the service. the red-green run
proves it, setting the status before validating the transition turns six tests
red, illegalTransitionLeavesStoredStatusUnchanged with expected ACTIVE but was
PROSPECT, transcripts under examples\lab17-crm\notes\screenshots\lab-17.

3. Which failure was hardest to diagnose?

the deliberate gate failure, because the guide's version does not reproduce.
raising the minimum to 0.99 passed, since the package is at 1.00 rather than
the guide's 0.97, so the recorded failure drops the three service suites and
asks for 0.90, which lands at 0.84.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab17-crm under examples/ | Pass |
| A2 | Surefire 3.x and JaCoCo with the service 0.80 rule | Pass, Surefire 3.5.2, JaCoCo 0.8.12 |
| A3 | JUnit 5 on the test classpath | Pass, junit-jupiter 5.11.4, test scope |
| B1 | happy path, add and find Amina, activate Ravi | Pass, in CustomerServiceTests |
| B2 | negatives, duplicate, illegal transition, not found | Pass, plus null target and duplicate email |
| B3 | parameterized legal and illegal transitions | Pass, 23 invocations from four tables |
| C1 | mvn clean verify passes the 80 percent service gate | Pass, at 1.00 |
| C2 | deliberate gate failure recorded then restored | Pass, 0.84 against 0.90, pom keeps 0.80 |
| C3 | Copilot review log or manual equivalent | Pass, lab17-001 marked manual |
| D1 | two consecutive mvn test runs identical | Pass, plus reverse order and one test alone |
| D2 | README runbook complete | Pass, README plus docs/coverage-notes.md |
| D3 | no secrets, no committed jacoco site, no target/ | Pass, target/ ignored since lab 8 |


MANUAL VERIFICATION

| # | Check | Result |
| --- | --- | --- |
| 1 | CustomerServiceTests covers add, find, activate, duplicate, illegal, not found | Pass, 16 tests |
| 2 | parameterized legal and illegal transitions run | Pass, 6 legal rows, 9 illegal, 8 enum cases |
| 3 | JaCoCo service package at or above 80 percent and the check passes | Pass, 1.00 |
| 4 | Copilot review exists if Copilot was used | Pass, manual, no suggestion accepted |
| 5 | two consecutive mvn test runs match | Pass, 83 both times |
| 6 | correlation asserted where exceptions carry it | Pass, in the 404, the 409 and every illegal row |
| 7 | no sensitive values in tests or git | Pass, example.com addresses only |
| 8 | deliberate gate fail evidence then restore | Pass, 03-gate-fail-and-restore.txt |
| 9 | README documents the verify command | Pass, first block |
| 10 | can point in the report to a branch that was red | Pass, CustomerValidator line 56 |


FAILURE EXPERIMENTS

1. built the validator on a second InMemoryCustomerRepository in @BeforeEach, so
the service wrote to one store and the rules read another. duplicateIdIsA409 and
duplicateEmailIsRejectedAcrossTheSharedRepository failed with "Expected
BusinessException to be thrown, but nothing was thrown", and the other fourteen
tests stayed green because they never ask the validator about stored data. this
is the wiring bug the guide asks for and it fails as a missing rejection rather
than as an error. restored.

2. moved ACTIVE -> PROSPECT out of the illegal CsvSource table into the legal
one. invocation [2] of legalTransitionIsAccepted failed with "Unexpected
exception thrown: BusinessException: illegal status transition ACTIVE ->
PROSPECT". the display name carries the row, so the report names the pair
without opening the file. restored.

3. ran mvn -B test twice, then once with -Dsurefire.runOrder=reversealphabetical,
then one test on its own. same counts every time, 83, and the reverse run is
green in the opposite class order. nothing is static and every fixture is built
in @BeforeEach, so there is no state for the order to expose. no change needed.

4. put Thread.sleep(2000) in activationMovesUpdatedAtWithoutTouchingCreatedAt.
the class went from 0.28 s to 2.28 s and stayed green, which is the problem. a
sleep buys nothing here, the assertion is on LocalDateTime values that are
already stamped, and eighty tests each paying that cost is a suite people stop
running. removed.

5. the coverage gate. the guide says raise the minimum to 0.99 and watch the
rule fail; it passed, because the service package is at 1.00 and not the guide's
0.97. so the failure was produced by removing the coverage instead, running
without the three service suites and asking for 0.90: "Rule violated for package
com.northstar.crm.service: lines covered ratio is 0.84, but expected minimum is
0.90", BUILD FAILURE. the minimum is a pom property, so both runs override it on
the command line and the pom keeps 0.80.

6, not in the guide's table, the red-green check. reordered changeStatus to set
the status before validating the transition. six tests failed, four in
CustomerServiceTests and two in CustomerApiFacadeTest. two of them are errors
rather than failures, because mutating first makes the validator compare the new
status against itself and a legal PROSPECT to ACTIVE comes back as "illegal
status transition ACTIVE -> ACTIVE". restored, and this is the run that answers
question 2.


WHAT THE COVERAGE NUMBER IS WORTH

the branch that closed last was CustomerValidator line 56, the fullName guard,
where the blank case had a test and the null case did not. the HTML report read
1 of 4 branches missed on a line that the LINE counter already called covered,
so the gate was green while the branch was red. a line gate cannot see a
half-covered condition; the report can.
