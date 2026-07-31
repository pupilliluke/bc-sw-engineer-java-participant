Northstar CRM build (Lab 17)

  mvn -B clean test
  mvn -B clean verify   (adds the JaCoCo check that can fail the build)
  mvn -q compile exec:java "-Dexec.mainClass=com.northstar.crm.Main"

Same jar caveat as Labs 15 and 16, java -jar target\customer-service.jar still
fails with NoClassDefFoundError because the jar bundles no dependencies.
exec:java supplies the Maven runtime classpath.

Adds a coverage gate and a formal service test suite on top of Lab 16's error
model. No production class changed in this lab. Artifact is still
com.northstar:customer-service:0.1.0-SNAPSHOT, copied forward from lab16-crm.

THE GATE

JaCoCo 0.8.12, package rule on com.northstar.crm.service, LINE covered ratio at
least 0.80, bound to verify. The minimum is the pom property
service.line.minimum so it can be overridden on the command line.

  mvn -B clean verify   -> All coverage checks have been met, BUILD SUCCESS

The service package sits at 46 of 46 lines and 20 of 20 branches. Reasoning,
the per-package numbers and what the gate does not prove are in
docs/coverage-notes.md.

TESTS

  mvn -B test
  mvn -B test "-Dtest=CustomerServiceTests"
  mvn -B test "-Dtest=CustomerValidatorParameterizedTest"

Eighty-three tests, up from Lab 16's fifty-two.

| Class | Tests | What it covers |
| --- | --- | --- |
| CustomerServiceTests | 16 | create, find, list, activate, and every failure path |
| CustomerValidatorParameterizedTest | 23 | the transition table as CSV and enum rows |
| CustomerValidatorTest | 8 | validateNew, the rules that need the repository |
| CustomerApiFacadeTest | 10 | unchanged from Lab 16 |
| the other six classes | 26 | unchanged from Labs 14 to 16 |

CustomerServiceTests is Lab 16's DefaultCustomerServiceTest renamed to the name
the guide asks for, with seven tests added. Its four transition tests moved into
the parameterized class rather than being duplicated there.

THE TRANSITION TABLE AS A TEST

  @CsvSource({"PROSPECT, ACTIVE", "ACTIVE, SUSPENDED", ...})   6 legal rows
  @CsvSource({"ACTIVE, PROSPECT", "ACTIVE, ACTIVE", ...})      9 illegal rows
  @EnumSource(CustomerStatus.class)                            CLOSED is terminal
  @EnumSource(CustomerStatus.class)                            a null target is a 400

Jupiter converts the CSV strings to CustomerStatus by constant name, so a typo
in a row fails as an argument conversion error rather than passing quietly.
Every row matches an entry in CustomerValidator.ALLOWED; a row that stops
matching is a rule change, not a test to relax.

MEANINGFUL ASSERTS

Every expected failure asserts the code and the status hint, not just the type:

  BusinessException ex = assertThrows(BusinessException.class,
          () -> service.changeStatus("CUS-1001", CustomerStatus.PROSPECT, "lab-request-001"));
  assertEquals(409, ex.getStatusHint());
  assertEquals("BUSINESS_CONFLICT", ex.getCode());
  assertEquals("lab-request-001", ex.getCorrelationId());
  assertEquals(CustomerStatus.ACTIVE, repo.findById("CUS-1001").orElseThrow().getStatus());

BusinessException is thrown by every rule in the validator, so the type alone
does not say which rule fired. A rejected transition must leave the stored
status alone.

ISOLATION

Every test builds its own InMemoryCustomerRepository, CustomerValidator and
DefaultCustomerService in @BeforeEach. Nothing is static, nothing sleeps, and
the suite is green run twice, run in reverse class order and run one test at a
time. Transcripts in notes/screenshots/lab-17/04-determinism.txt.

Mockito is on the test classpath and is used by CustomerApiFacadeTest from an
earlier lab. The three service suites in this lab use the real in-memory
repository; substituting collaborators is Lab 18.

FIXTURES

| ID | Name | Status | Email |
| -- | ---- | ------ | ----- |
| CUS-1001 | Amina Khan | ACTIVE | amina.khan@example.com |
| CUS-1002 | Ravi Singh | PROSPECT | ravi.singh@example.com |
| CUS-9999 | not stored | | |

Correlation id lab-request-001 on every failure path that carries one.

FAILURE EXPERIMENTS

All five were run and restored.

| # | Experiment | Observed | Restored |
| --- | --- | --- | --- |
| 1 | Validator built on a second repository in @BeforeEach | duplicate id and duplicate email tests fail, nothing was thrown | one repository shared by both collaborators |
| 2 | ACTIVE -> PROSPECT moved into the legal CsvSource table | row [2] fails, unexpected BusinessException | row moved back |
| 3 | mvn -B test twice, then in reverse class order | identical counts, 83 every time | no change needed |
| 4 | Thread.sleep(2000) in an activation test | class time 0.28 s to 2.28 s, still green | sleep removed |
| 5 | Coverage minimum raised | 0.99 passes on the full suite; 0.90 with the service suites dropped fails at 0.84 | pom keeps 0.80 |

The guide's version, raise the minimum to 0.99 and watch the rule fail, does
not fail here because the package is at 1.00. Failing the gate needs the
coverage taken away, and dropping the three service suites only takes the
package to 0.84, still above the honest 0.80 gate, because the facade tests
exercise the service. A line gate is a floor against deletion, not proof that
the rules are asserted.

The proof that the rules are asserted is the red-green run in
copilot-notes/ai-junit-review.md. Reordering two lines in changeStatus so the
status is set before the transition is validated turns six tests red.

AI REVIEW

lab17-001 in copilot-notes/ai-junit-review.md, marked manual. No Copilot
suggestion was accepted; the acceptance checklist was applied to the hand
written tests anyway, and it names one weak assertion carried over from Lab 16.

SECURITY AND PRODUCTION REVIEW

1. Which inputs are untrusted?

The same ones as Lab 16, everything reaching the facade. Tests use fixtures, so
nothing in this lab widens the trust boundary.

2. Where is each check enforced?

Unchanged. Shape at the facade, meaning in CustomerValidator, exposure in
GlobalExceptionHandler. The suite asserts where each one fires rather than
moving any of them.

3. Which values are sensitive?

Email addresses. Test data uses example.com addresses only, and the duplicate
email test asserts that the address is absent from the client message.

CLEANUP

  mvn -B clean
  git status

target/ is ignored, which covers target/site/jacoco. Keep lab17-crm for Lab 18.

NOTES

The coverage gate and its limits are in docs/coverage-notes.md. Lab 16's error
model is still in docs/error-model-notes.md, Lab 15's layers in
docs/service-layer-notes.md and Lab 14's in docs/dto-boundary-notes.md.
Evidence transcripts are in notes/screenshots/lab-17. Pre-lab exercises and
reflection answers are in notes/Week 2/Module 17/. Full GUIDE at
labs/Week 2 - Backend, AI Tools and Testing/module-17/lab17/.
