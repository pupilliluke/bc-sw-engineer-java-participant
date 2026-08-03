Northstar CRM build (Lab 18)

  mvn -B clean test
  mvn -B clean verify   (adds the JaCoCo check that can fail the build)
  mvn -q compile exec:java "-Dexec.mainClass=com.northstar.crm.Main"

Same jar caveat as Labs 15 to 17, java -jar target\customer-service.jar still
fails with NoClassDefFoundError because the jar bundles no dependencies.
exec:java supplies the Maven runtime classpath.

Adds Mockito unit isolation for the service on top of Lab 17's suite and
coverage gate. No production class changed in this lab. Artifact is still
com.northstar:customer-service:0.1.0-SNAPSHOT, copied forward from lab17-crm.

THE POM DID NOT CHANGE EITHER

Step 1 of the guide adds mockito-core and mockito-junit-jupiter. Both were
already in the pom at 5.14.2, test scope, and mock(CustomerService.class) has
been in CustomerApiFacadeTest since Lab 14.

  mvn -B dependency:tree -Dincludes=org.mockito

  +- org.mockito:mockito-core:jar:5.14.2:test
  \- org.mockito:mockito-junit-jupiter:jar:5.14.2:test

mockito-junit-jupiter is the one that supplies MockitoExtension. Without it
the @Mock fields stay null and every test fails on a NullPointerException.

TESTS

  mvn -B test
  mvn -B test "-Dtest=CustomerServiceMockitoTest"
  mvn -B test "-Dtest=CustomerServiceBddMockTest"

Ninety-eight tests, up from Lab 17's eighty-three.

| Class | Tests | What it covers |
| --- | --- | --- |
| CustomerServiceMockitoTest | 12 | new, stub, verify, never, inOrder and ArgumentCaptor on a mocked repository |
| CustomerServiceBddMockTest | 3 | new, the same paths in BDDMockito given/then/should |
| CustomerServiceTests | 16 | Lab 17, the same rules against the real in-memory repository |
| CustomerValidatorParameterizedTest | 23 | the transition table as CSV and enum rows |
| CustomerValidatorTest | 8 | validateNew, the rules that need the repository |
| CustomerApiFacadeTest | 10 | unchanged since Lab 16 |
| the other six classes | 26 | unchanged from Labs 14 to 16 |

WHAT THE MOCK PROVES

Lab 17 asserted on stored state, read the customer back and check the status.
That is a claim about the HashMap as much as about the service. The mocked
suite asserts on the calls instead:

  when(repository.findById("CUS-1002")).thenReturn(Optional.of(ravi));
  when(repository.save(any(Customer.class))).thenAnswer(call -> call.getArgument(0));

  Customer activated = service.changeStatus("CUS-1002", CustomerStatus.ACTIVE, "lab-request-001");

  verify(repository).findById("CUS-1002");
  verify(repository).save(argThat(c -> "CUS-1002".equals(c.getCustomerId())
          && c.getStatus() == CustomerStatus.ACTIVE));

save has to be stubbed with thenAnswer because changeStatus returns
repository.save(existing). An unstubbed mock returns null and the test then
fails on a NullPointerException rather than on the rule.

Four of the twelve prove a call that must not happen:

  verify(repository, never()).save(any(Customer.class));

for CUS-9999 not found, an illegal transition, a null target status and a
duplicate email. Lab 17 could only argue that from a count of stored
customers. One test goes further, a duplicate id is rejected before the email
is looked up at all, verify(repository, never()).existsByEmail(anyString()),
which no assertion on state can express.

CAPTORS

  ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
  verify(repository).save(captor.capture());
  assertEquals(CustomerStatus.ACTIVE, captor.getValue().getStatus());

Customer.equals compares customerId alone, so verify(repository).save(ravi)
passes whatever status the argument carried. The captor is what reads the
field the test is named after. It is also how addCustomer is checked on three
fields at once, id, name and status, plus the timestamps the service stamps.

BDD IS WORDING

given/willReturn is when/thenReturn and then(repository).should() is
verify(repository). Same engine, same strict stubbing, same failure messages.
CustomerServiceBddMockTest keeps to one style per class; mixing the two static
import sets gives a file where half the arrange reads backwards.

ISOLATION

CustomerRepository is mocked. CustomerValidator stays real and is built on the
same mock instance the service holds, because it owns the transition table
these tests exist to check. Which suite uses what, and why the Lab 17 suite
stays, is in docs/isolation-policy.md.

Nothing is static, nothing sleeps, and MockitoExtension builds fresh mocks per
test. The suite is green run twice, run in reverse class order and run one
test at a time. Transcripts in notes/screenshots/lab-18/04-determinism.txt.

The service package is still at 46 of 46 lines and 20 of 20 branches, so
mvn -B clean verify passes the 0.80 gate unchanged. The mocked tests walk
paths the older suites already reached; isolation is not extra coverage.

FIXTURES

| ID | Name | Status | Email |
| -- | ---- | ------ | ----- |
| CUS-1001 | Amina Khan | ACTIVE | amina.khan@example.com |
| CUS-1002 | Ravi Singh | PROSPECT | ravi.singh@example.com |
| CUS-9999 | never stubbed into findById | | |

Correlation id lab-request-001 on every failure path that carries one.

FAILURE EXPERIMENTS

Eight, the guide's five and three more on wiring and rule order. All restored.

| # | Experiment | Observed | Restored |
| --- | --- | --- | --- |
| 1 | findById stubbed to throw | error not failure, the RuntimeException leaves changeStatus untouched | Optional stub back |
| 2 | Amina ACTIVE asked for PROSPECT | 409 and never().save | kept as a permanent test |
| 3 | addCustomer called twice against one verify | TooManyActualInvocations, both call sites printed | second call removed |
| 4 | an existsById stub on the not-found path | UnnecessaryStubbingException from afterEach | stub removed |
| 5 | Thread.sleep(2000) | class time 1.56 s to 3.46 s, still green | sleep removed |
| 6 | CustomerValidator mocked | four rejections disappear, ACTIVE to PROSPECT written through | real validator back |
| 7 | validator built on a second mock | only the two uniqueness tests fail, and the dead stubs name the cause | one mock shared |
| 8 | validateNew reordered, email looked up before id | all eighty-three Lab 17 tests green, two new ones red with NeverWantedButInvoked | order restored |

Experiment 6 is the one that answers why the validator is not mocked. The four
happy-path tests stayed green with the rule switched off, so a suite without
the negatives would have passed.

Experiment 8 is the one that answers what the interaction tests add. A rule
that runs in the wrong order throws the same exception, stores the same
nothing and covers the same lines, so neither a state assertion nor the
coverage gate can see it.

AI REVIEW

lab18-001 in copilot-notes/ai-mockito-review.md, marked manual, with the
guide's five acceptance checks and the risk they exist to catch: mocking a
collaborator that holds the rule under test.

SECURITY AND PRODUCTION REVIEW

1. Which inputs are untrusted?

The same ones as Lab 16, everything reaching the facade. Mocks narrow what a
test touches, they do not move the trust boundary.

2. Where is each check enforced?

Unchanged. Shape at the facade, meaning in CustomerValidator, exposure in
GlobalExceptionHandler. Mocking the repository does not mock the rules, which
is the reason the validator is real.

3. Which values are sensitive?

Email addresses. Test data uses example.com addresses only, the duplicate
email test asserts that the address is absent from the client message, and
experiment 8 shows the ordering that would have sent it to the store on an
already invalid request.

CLEANUP

  mvn -B clean
  git status

target/ is ignored, which covers target/site/jacoco. Keep lab18-crm for Lab 19.

NOTES

The isolation policy is in docs/isolation-policy.md. Lab 17's coverage gate is
still in docs/coverage-notes.md, Lab 16's error model in
docs/error-model-notes.md, Lab 15's layers in docs/service-layer-notes.md and
Lab 14's in docs/dto-boundary-notes.md. Evidence transcripts are in
notes/screenshots/lab-18. Pre-lab exercises and reflection answers are in
notes/Week 2/Module 18/. Full GUIDE at
labs/Week 2 - Backend, AI Tools and Testing/module-18/lab18/.
