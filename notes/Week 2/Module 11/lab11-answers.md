Lab 11 GitHub Copilot for testing and refactoring (concepts to discuss,
reflection questions, manual verification)


CONCEPTS TO DISCUSS

1. Exploratory Copilot-generated test vs a deliberately designed suite

Exploratory asks what this code appears to do and writes assertions around it.
A designed suite starts from the rules the business needs held and picks cases
that break them. The first describes the code as written, so a bug already in
the code gets enshrined as expected behaviour.

2. What makes an assertion false confidence

It cannot fail. assertNotNull(service) checks that @BeforeEach ran, not that
the service works. The test is proof only when I can name an input that turns
it red. Gutting addCustomer to store nothing left serviceIsNotNull green while
four real tests failed.

3. Why extract CustomerNotifier before mocking rather than mocking CustomerService

Mocking the class under test means asserting against a fake version of the
thing I'm supposed to be proving. The extract gives the side effect a named
boundary, so the real service runs and only the collaborator is faked. That
boundary is the design improvement; the mock is a consequence.

4. What a code smell is, and the clearest candidate from Lab 10

A smell is code that works and resists change. The candidate was addCustomer
doing validation, duplicate detection, defaulting and a println in one method.
The println was untestable where it sat, and the blank-id rule was inline in
one method and missing entirely from updateStatus.

5. Why high coverage % is not meaningful coverage

Coverage counts lines executed, not rules checked. A test that calls every
method and asserts nothing scores the same as one that asserts everything.
Percentage answers did this run, meaningful coverage answers would this fail if
the rule broke, and only the second is worth anything.

6. Regression risk when refactoring without a full suite, and how today's tests help

Without tests a refactor is a claim, not a verified change. Today's eight run
before and after, so the extract is evidence-backed. They are honest about
their limits though, the createdAt and status defaulting has no test, so that
particular regression would still get through unseen.

7. When to trust a Copilot extract-method vs verify manually

Trust it when the extracted body is moved verbatim and the suite is green
either side, which is a mechanical change the compiler mostly polices. Verify
by hand whenever the diff deletes anything. The reference refactor in this lab
dropped three guard clauses, and deletions hide far better than additions.

8. Acceptance criteria before merging an AI-generated test or refactor

Every assertion can fail, suite green before and after, no new dependency, no
behaviour removed without saying so, shared fixtures not invented ones, gaps
written down, and I can explain it with the assistant closed. Full list is
lab11-004 in ai-test-refactor-notes.md.

9. Why keep JUnit and Mockito at test scope

They exist to check the code, not to ship with it. Test scope keeps them off
the runtime classpath and out of the jar, so nothing in production can import
a mock by accident. It also keeps the deployed artifact smaller and its attack
surface narrower.

10. How this preview sets up Labs 17-18 without replacing them

It teaches the judgement, an assertion that can fail, a collaborator worth
mocking, gaps named honestly. It skips the machinery, parameterized tests and
JaCoCo in Lab 17, stub-vs-verify and ArgumentCaptor in Lab 18. One mock and
one verify here is a worked example, not a method.


REFLECTION QUESTIONS

1. What made a test meaningful vs false confidence here

Whether breaking the code turned it red. I broke addCustomer on purpose and
watched which tests noticed, four did and serviceIsNotNull did not. Meaningful
assertions also named domain values, CUS-1002 and PROSPECT, rather than counts
and null checks that any object satisfies.

2. How extracting CustomerNotifier changed testability

Before, the notification was a println, and asserting on it meant capturing
stdout. After, it is a method call on an interface, so a mock records it and
verify asserts the arguments. The side effect went from something you observe
to something you assert.

3. What to tell a teammate who accepts every Copilot test unread

Show them experiment 1 rather than argue. An unread suite raises the test count
and the confidence while protecting nothing, and it is harder to fix later
because everyone now believes the code is covered. The unread test is a
liability that looks like an asset.

4. Which refactor suggestion I rejected, and why

The reference CustomerService in Step 5. It extracted validation correctly and
quietly dropped the null-customer guard and the createdAt and status defaults.
addCustomer(null) would have gone from IllegalArgumentException to
NullPointerException. Kept all three, extracted only the validation.

5. How this preview connects to Labs 17-18

The guard clauses I left untested are exactly the parameterized batch Lab 17
teaches, and the one verify here is the shallow end of Lab 18's stub-vs-verify
and ArgumentCaptor. The correlation id lab-request-001 stays unassertable until
something can capture it, which is Lab 18.

6. Which coverage gap is acceptable now, and what would change that

The guard clauses are acceptable, they throw immediately and read correctly.
The defaulting gap is not, because a refactor already tried to delete that
behaviour and the suite would not have caught it. Any change touching
addCustomer's tail makes it urgent.

7. How this connects to the wider Northstar CRM across Weeks 2-6

The notifier is the seam that later becomes a real integration, email, events,
or a queue, without the service changing. The tests become the regression net
that DTO and API labs land on top of. Shared fixtures keep failures traceable
across every lab that follows.

8. Cost of skipping before/after test runs on a refactor in a shared codebase

The change ships as an assumption and the breakage surfaces in someone else's
work, days later, where the cause is no longer obvious. Before and after runs
cost thirty seconds and are the only evidence that a behaviour-preserving
change actually preserved behaviour.

9. Forward look, what stays valuable about the notifier mock pattern under Spring

All of it. Spring changes who constructs the notifier, from a constructor call
to injection, not what the test does. The service still depends on an
interface, the test still supplies a fake and verifies the interaction. The
annotations change, the design lesson does not.


CHECKPOINT A - project + test deps

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | lab11-crm copied from Lab 10 under examples/ | Pass. the timed path copies the starter in, which is not my Lab 10 project, so lab10-crm was merged back over it, controller, repository, the seven package-info files, deleteCustomer, the DTO stubs and the javadoc. the tree now matches lab10-crm plus CustomerNotifier |
| 2 | JUnit 5 + Mockito on test scope, Surefire present | Pass, junit-jupiter 5.11.4 and mockito-core / mockito-junit-jupiter 5.14.2, all three test scope, surefire 3.5.2 |
| 3 | Copilot still Ready | Pass, Ready in IntelliJ. the suggestions logged in this lab came from Claude Code, not ghost text, stated at the top of ai-test-refactor-notes.md |

CHECKPOINT B - core tests green

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | CustomerTest (2) and CustomerServiceTest (4) pass | Pass, 2 and 5. the fifth is findByStatusReturnsOnlyMatchingCustomers, the replacement for the rejected serviceIsNotNull |
| 2 | Sample IDs CUS-1001 / CUS-1002 used in tests | Pass, both ids in all three test classes, no invented fixtures left after lab11-001 |
| 3 | No JUnit 4 imports | Pass, grep over src/test for org.junit.Test, @RunWith and org.junit.Assert returned nothing |

CHECKPOINT C - refactor + mock

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | CustomerNotifier extracted and called from updateStatus | Pass, one method, called after the status changes so the old value is still readable |
| 2 | No-arg CustomerService() still works (no-op notifier) | Pass, delegates to a no-op lambda, Main and CustomerServiceTest both go through it |
| 3 | CustomerNotifierMockTest verifies PROSPECT -> ACTIVE | Pass, plus verifyNoMoreInteractions, so addCustomer is proven not to notify |
| 4 | validateCustomerId is the single blank-ID check | Pass, grep isBlank over src/main returns one hit, inside the helper |

CHECKPOINT D - notes + guidelines + experiments

| # | Confirm | Notes |
| --- | --- | --- |
| 1 | Entries lab11-001 to lab11-004 complete | Pass |
| 2 | False-confidence rejection documented | Pass, two. the CustomerTest already in the tree, and serviceIsNotNull proven green against a gutted addCustomer |
| 3 | Coverage gaps documented, acceptance checklist present | Pass, lab11-003 method matrix with four gap decisions, lab11-004 the guide's five points plus two of mine |
| 4 | Failure experiments recorded, mvn clean test green | Pass, all four experiments, suite restored to 8 tests |


MANUAL VERIFICATION

| # | Check | Result |
| --- | --- | --- |
| 1 | mvn -q clean test passes, exact count documented | Pass, 8 tests |
| 2 | CustomerTest proves equals/toString with real assertions | Pass, both directions of equals |
| 3 | CustomerServiceTest covers add / duplicate / update / unknown-id | Pass, plus findByStatus |
| 4 | Mock test verifies notifier args for CUS-1002 | Pass, PROSPECT to ACTIVE |
| 5 | CustomerNotifier is a real extraction, not a Spring/JPA paste | Pass, one method, no annotations |
| 6 | Notes lab11-001 to lab11-004 present | Pass |
| 7 | At least one false-confidence assertion rejected | Pass, two |
| 8 | No secrets or real PII in tests or prompts | Pass, fixtures only |
| 9 | git status clean of target/ junk | Pass, target/ is gitignored |
| 10 | Can explain every accepted test and refactor | Pass |

count for row 1 is 8 not the guide's about 7, CustomerTest 2 plus
CustomerServiceTest 5 plus the mock test 1. the fifth service test is the real
replacement for the rejected serviceIsNotNull, which is the count the guide's
verified windows line predicts.
