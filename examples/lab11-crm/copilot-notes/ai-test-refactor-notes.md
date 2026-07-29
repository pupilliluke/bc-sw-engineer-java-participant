# AI test/refactor notes — Lab 11

Assistant used for this lab: Claude Code (Opus 5) in the IntelliJ terminal, not
Copilot inline ghost text. Where an entry below shows a suggestion block, that
block is the assistant standing in for Copilot Chat on the prompt quoted above
it, in the shape Copilot returns for that prompt. Recorded here so the evidence
is not misattributed, same rule as lab10-001. The one exception is the
CustomerTest in lab11-001, which was already sitting in the tree when this
session started and is quoted verbatim.

`ai-review-notes.md` beside this file is the Lab 10 review log, carried across
unchanged per the GUIDE's project tree. Nothing in it is Lab 11 work; it is here
so the audit trail for `CustomerService` reads in one place.

Project base. The timed path copies `starter/` in, and the starter is a leaner
rewrite of the same domain rather than my Lab 10 project. It ships without
`controller/`, `repository/`, the seven `package-info.java` files,
`deleteCustomer` and the two DTO stubs, and it strips the javadoc off `Customer`
and `CustomerStatus` — including the "reject `@Entity`" note that was the whole
point of Lab 10. Merged `lab10-crm` back over it so this project is Lab 10 plus
the Lab 11 delta rather than a parallel branch. `deleteCustomer` came across
routed through `validateCustomerId` instead of keeping its own inline blank-id
check, so the lab11-002 extract covers three call sites, not two. Lab 9's
`PlaceholderTest` deliberately not brought across, per the GUIDE's Windows note
about it inflating the count.

Numbering note: the starter template labels 002 "CustomerServiceTest", 003
"CustomerNotifier extract + Mockito" and 004 "coverage gaps / acceptance
guidelines". The GUIDE Steps 4, 6, 7 and 8 label them "false confidence",
"code smell", "coverage gaps" and "acceptance guidelines". Followed the GUIDE,
since Checkpoint D and the rubric are written against it. Nothing is lost, the
CustomerServiceTest and Mockito content sits inside 002 and 003.

Interface naming conflict, decided once here and referenced below: the starter
ships `CustomerNotifier.notifyCreated(String customerId, String correlationId)`.
GUIDE Step 5 specifies `notifyStatusChange(String, CustomerStatus,
CustomerStatus)` and its "If it fails" line says to reject `notifyCreated` as an
invented API. The two ship in the same lab folder and contradict each other. The
GUIDE wins: Checkpoint C row 3 and the mock test are both written against
notifyStatusChange, so notifyCreated could not satisfy them. The module 11
pre-lab exercise 2 predicted "the lab's version wins when i get there" — the lab
turned out to have two versions, and the one the grader checks is the GUIDE's.


## lab11-001 — reject false-confidence assertions (GUIDE Step 4)

- Date: 2026-07-29
- Files: src/test/java/com/northstar/crm/entity/CustomerTest.java

Two rejections here, one found and one produced on purpose.

### Rejection 1 — the CustomerTest already in the tree

Found on opening the project, in two places at once,
`src/test/java/com/northstar/crm/CustomerTest.java` and
`src/test/java/com/northstar/crm/entity/CustomerTest.java`, byte-identical:

    @Test
    void equalsUsesCustomerIdOnly() {
        String id = "C123";
        Customer c1 = new Customer(id, "Alice", "alice@example.com", "111-1111",
                CustomerStatus.ACTIVE, LocalDateTime.now());
        Customer c2 = new Customer(id, "Bob", "bob@example.com", "222-2222",
                CustomerStatus.INACTIVE, LocalDateTime.now().minusDays(1));
        assertEquals(c1, c2);
    }

Three findings, in order of how loudly they fail:

1. `CustomerStatus.INACTIVE` does not exist. The enum is PROSPECT, ACTIVE,
   SUSPENDED, CLOSED. This is the invented-API tell from the module 10 pre-lab,
   arriving in a test instead of in production code. It does not compile, so it
   is the honest kind of wrong:

        [ERROR] .../entity/CustomerTest.java:[13,92] cannot find symbol
          symbol:   variable INACTIVE
          location: class com.northstar.crm.entity.CustomerStatus

2. Invented fixtures. C123 / Alice / Bob instead of CUS-1001 Amina Khan and
   CUS-1002 Ravi Singh. It reads fine and it is untraceable — nothing ties the
   test to the business scenario every other lab artifact uses, and Checkpoint B
   row 2 asks for the shared ids by name.
3. Half a test. `equals` returning true for the same id is one direction of the
   rule. Nothing asserted that two *different* ids are not equal, so an `equals`
   that returned `true` unconditionally would have passed.

Decision: rejected and rewritten. Kept the entity/ copy per the GUIDE layout,
deleted the root-package duplicate. Fixtures now CUS-1001 / CUS-1002, and
`assertNotEquals` covers the second direction of the identity rule.

### Rejection 2 — "add one more test", unguided

Prompt, deliberately with no constraints, per Step 4:

    Add one more test to CustomerServiceTest.

Suggestion:

    @Test
    void serviceIsNotNull() {
        assertNotNull(service);
    }

Why it is false confidence: `@BeforeEach` assigns `service` before every test, so
the assertion is checking that an assignment two lines up happened. There is no
input that breaks it and no change to CustomerService that turns it red. It adds
one to the test count and nothing to the safety net.

Not just argued — demonstrated. Temporarily gutted `addCustomer` so it validates
and returns without ever calling `customers.add(customer)`, then ran the class
with the trivial test still in it:

    [ERROR] Tests run: 6, Failures: 3, Errors: 1, Skipped: 0
    [ERROR] CustomerServiceTest.addCustomerStoresNewCustomer:43 expected: <1> but was: <0>
    [ERROR] CustomerServiceTest.addCustomerRejectsDuplicateId:55 Expected
            java.lang.IllegalStateException to be thrown, but nothing was thrown.
    [ERROR] CustomerServiceTest.findByStatusReturnsOnlyMatchingCustomers:82 expected: <1> but was: <0>
    [ERROR] CustomerServiceTest.updateStatusChangesExistingCustomer:63
            IllegalArgument Customer does not exist: CUS-1002

Four real tests went red against a service that silently stores nothing.
`serviceIsNotNull` stayed green. That is the entire argument for the rule, in
one run.

Replacement, per the GUIDE's suggestion of a `findByStatus` filter:

    @Test
    void findByStatusReturnsOnlyMatchingCustomers() {
        service.addCustomer(amina());
        service.addCustomer(ravi());

        List<Customer> prospects = service.findByStatus(CustomerStatus.PROSPECT);

        assertEquals(1, prospects.size());
        assertEquals("CUS-1002", prospects.get(0).getCustomerId(),
                "Ravi is the only PROSPECT, Amina is ACTIVE and must be filtered out");
    }

This one can fail: drop the filter and it returns 2, invert it and it returns
Amina. It also asserts *which* customer came back, not just how many — a count
alone would pass if the filter returned the wrong single customer.

`addCustomer` restored, trivial test deleted, suite back to green.


## lab11-002 — code smell and the validateCustomerId extract (GUIDE Steps 5–6)

- Date: 2026-07-29
- Files: service/CustomerService.java, service/CustomerNotifier.java

Prompt:

    Review CustomerService for code smells: duplicated logic, long methods,
    unclear names. Suggest one specific refactor.

Suggestion, condensed to the three it named:

    1. addCustomer does four jobs: validation, duplicate detection, defaulting,
       and I/O. The println at the end makes the method impossible to unit test
       for its notification behaviour.
    2. The blank-customerId rule is written out inline. It belongs in one place.
    3. updateStatus announces nothing, so a status transition is invisible to
       any collaborator. Extract a CustomerNotifier interface and call it after
       the status changes.

Smell named: duplicated validation plus untestable I/O inside `addCustomer`.
Before the refactor the starter had the blank-id rule inline in `addCustomer`
and a `System.out.println("created " + id)` on the line before the return, and
`updateStatus` did no id validation at all — so the rule was simultaneously
duplicated *and* missing, depending on which method you entered through.

Refactor applied:

| # | Change | Effect |
| --- | --- | --- |
| 1 | `validateCustomerId(String)` private helper | the blank-id rule exists once; addCustomer, updateStatus and deleteCustomer all call it |
| 2 | `CustomerNotifier` interface, one method | the transition announcement has somewhere to go that is not stdout |
| 3 | `CustomerService(CustomerNotifier)` constructor | a test can inject a mock |
| 4 | no-arg `CustomerService()` delegating to a no-op lambda | Main and every Lab 10 caller still compile and behave the same |
| 5 | `findByStatus` / `listAll` added back from the Lab 10 service | the GUIDE's Step 3 tests call them |
| 6 | println deleted from addCustomer | the untestable side effect is gone |

Confirmed single check, per Step 6: `grep isBlank` over src/main returns exactly
one hit, inside `validateCustomerId`.

Part of the suggested refactor rejected. The GUIDE's own reference
`CustomerService` (Step 5, lines 527–534) drops three behaviours the starter
had: the null-customer guard, the `createdAt` default, and the `status`
default. Applying it as printed would mean `addCustomer(null)` throws
NullPointerException instead of IllegalArgumentException, and a customer
constructed with a null status is stored with a null status instead of
PROSPECT. That is a refactor quietly changing behaviour, which is the exact
thing this lab is about, so all three were kept and only the validation was
extracted. Message strings were also kept aligned with the Lab 10 service
("Duplicate customerId: X", "customerId must not be blank", "Customer does not
exist: X") rather than switched to the GUIDE's wording, so the two labs fail the
same way.

One behaviour change accepted and recorded: the `created CUS-1001` line
`addCustomer` used to print is gone. Nothing asserted on it and stdout is not an
interface, which is why it was a smell. `Main` is Lab 10's harness, which never
printed those lines itself, so its output is unchanged by the deletion —
verified below.

Proving tests, run before and after the extract: `CustomerServiceTest` (5) and
`CustomerNotifierMockTest` (1).

    mvn -q test -Dtest=CustomerServiceTest,CustomerNotifierMockTest
    Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

Mockito proof that the notifier is actually called, not merely declared:

    verify(notifier).notifyStatusChange("CUS-1002", CustomerStatus.PROSPECT, CustomerStatus.ACTIVE);
    verifyNoMoreInteractions(notifier);

`verifyNoMoreInteractions` is the second half. Without it the test proves the
notifier was called at least once with those arguments; with it, the test also
proves `addCustomer` does not notify. That boundary is a design decision, so it
gets an assertion.


## lab11-003 — coverage gaps (GUIDE Step 7)

- Date: 2026-07-29

Prompt:

    What CustomerService behavior is not covered by CustomerServiceTest and
    CustomerNotifierMockTest?

Suggestion: named `findByStatus`, `listAll` and "the validation paths" as gaps,
and claimed the "core CRUD paths are well covered". The first half is right and
already out of date — `findByStatus` gained a direct test in lab11-001. The
second half is the part to distrust: "well covered" is not a finding, and it
skipped every guard clause. Method matrix below is mine, written by listing the
public methods from the source rather than from the answer, and re-listed after
the Lab 10 merge at the top of this file put `deleteCustomer` and the two DTO
stubs back on the service.

### Customer

| Method | Covered | By |
| --- | --- | --- |
| all-args constructor | yes | every test builds fixtures with it |
| no-arg constructor | no | nothing constructs an empty Customer |
| equals | yes, both directions | CustomerTest.equalsIsBasedOnCustomerIdOnly |
| hashCode | **no** | see gap 1 |
| toString | yes | CustomerTest.toStringIncludesCustomerId, id only |
| getCustomerId / getStatus | yes, indirectly | asserted on in service tests |
| getFullName / getEmail / getPhone / getCreatedAt | no | never read in an assert |
| setters | partly | setStatus via updateStatus; setCreatedAt via the default path, unasserted |

### CustomerService

| Method | Covered | By |
| --- | --- | --- |
| CustomerService() | yes | @BeforeEach, though the no-op notifier is never asserted |
| CustomerService(CustomerNotifier) | yes | CustomerNotifierMockTest |
| addCustomer — happy path | yes | addCustomerStoresNewCustomer |
| addCustomer — duplicate id | yes | addCustomerRejectsDuplicateId |
| addCustomer — null customer | no | gap 2 |
| addCustomer — blank/null id | no | gap 2 |
| addCustomer — createdAt / status defaulting | no | gap 3 |
| findByCustomerId — found | yes, indirectly | asserted through in three tests |
| findByCustomerId — null argument | no | gap 2 |
| findByStatus — match | yes | findByStatusReturnsOnlyMatchingCustomers |
| findByStatus — null argument, unmodifiability | no | gap 2, gap 4 |
| updateStatus — happy path | yes | updateStatusChangesExistingCustomer |
| updateStatus — unknown id | yes | updateStatusThrowsForUnknownCustomer |
| updateStatus — blank id, null newStatus | no | gap 2 |
| updateStatus — notifier called with old and new | yes | CustomerNotifierMockTest |
| deleteCustomer — happy path | **no** | gap 5 |
| deleteCustomer — blank id, unknown id | no | gap 5 |
| listAll | yes, indirectly | size and get(0); unmodifiability untested, gap 4 |
| create(CustomerRequest) | no | gap 6 |
| getById(String) | no | gap 6 |
| validateCustomerId | indirectly | private; reached through all three callers |

### Gap decisions

| # | Gap | Accept now? | Reasoning |
| --- | --- | --- | --- |
| 1 | hashCode untested while equals is tested | **no, but deferred to Lab 17** | this is the one that worries me. equals and hashCode have a contract, and only half of it is under test. Two CUS-1001 objects are equal but could land in different HashMap buckets and nothing here would notice. Deferred only because a Set-based test is one line and belongs with the parameterized work in Lab 17, not because it is safe |
| 2 | guard clauses (null customer, blank id, null status, null lookups) | yes, for now | every one throws immediately and is visibly correct on reading. They are also the natural @ParameterizedTest / @CsvSource batch, which is Lab 17's material — writing six near-identical assertThrows tests by hand now is the thing Lab 17 teaches you not to do |
| 3 | createdAt / status defaulting in addCustomer | **no** | this is the weakest spot in the suite. The defaulting is real behaviour that the GUIDE's own reference refactor silently deletes (see lab11-002), and nothing in the suite would have caught that. Flagged as the first test to add if this lab is extended |
| 4 | unmodifiability of findByStatus / listAll return values | yes | `List.copyOf` and `.toList()` are JDK guarantees, not project logic. Lab 10's harness already proved both throw UnsupportedOperationException on mutation |
| 5 | deleteCustomer has no test at all | **no**, deferred with gap 3 | it is the same shape as updateStatus, validate then look up then act, and updateStatus is tested twice. one test would close it. deferred only because the suite count is pinned at 8 by captured evidence, including step8-clean-test-green.png, and adding a ninth test mid-submission would make every screenshot disagree with the build. first test to add after gap 3, and the assertion has to be that the customer is gone from listAll, not just that the call returned |
| 6 | create(CustomerRequest) / getById(String) untested | yes | both are Lab 8 stubs whose entire body is `throw new UnsupportedOperationException`. a test asserting a stub throws pins the stub in place rather than proving behaviour, and would have to be deleted the moment Lab 14 gives them bodies. they are listed here so the matrix is honest about what is on the class, not because they are a real gap |

Honest summary: 8 tests cover the happy paths, the two documented exception
paths, entity identity, and one collaborator interaction. They do not cover the
hashCode half of the identity contract, the defaulting behaviour, or
deleteCustomer, and all three of those are real. Coverage percentage is not quoted anywhere because the pom has
no JaCoCo plugin — measuring it is Lab 17, and a number invented here would be
exactly the false confidence lab11-001 rejects.


## lab11-004 — acceptance guidelines (GUIDE Step 8)

- Date: 2026-07-29

Acceptance guidelines for AI-generated tests and refactors:

1. Every assertion must be able to fail — if I can't describe an input that
   breaks it, it isn't a real test.
2. Every refactor must be backed by a passing test suite run before and after.
3. No accepted suggestion may introduce a dependency not already in pom.xml.
4. I can explain, without re-reading Copilot's explanation, why the code
   is correct.
5. Coverage gaps are documented, not silently ignored.

Two clauses added from what this lab actually cost me, kept separate from the
GUIDE's five so the source of each is clear:

6. A suggestion that removes behaviour must say so out loud. The Step 5
   reference refactor dropped three guards without mentioning it (lab11-002);
   deletions hide in a diff far better than additions do.
7. Test data is the shared fixtures or it is rejected. CUS-1001 / CUS-1002, not
   C123 / Alice / Bob. Invented data compiles, passes, and quietly disconnects
   the test from the business rule it is supposed to protect.

Full suite for the record:

    mvn -q clean test
    Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
    BUILD SUCCESS

Count is 8, not the GUIDE's "about 7": CustomerTest 2 + CustomerServiceTest 5 +
CustomerNotifierMockTest 1. The fifth service test is the real replacement for
the rejected `serviceIsNotNull`, which is the count the GUIDE's Verified
(Windows) line predicts.


## lab11 failure experiments (GUIDE Step 9)

### Experiment 1 — ask for "one more test" with no constraints

Covered in full in lab11-001, rejection 2. Observed: a trivial assertion that
cannot fail, proven green against a service gutted to store nothing while four
real tests went red. Restored: `addCustomer` put back, trivial test deleted,
replaced with `findByStatusReturnsOnlyMatchingCustomers`.

### Experiment 2 — refactor updateStatus to skip the notifier

Deleted the `notifier.notifyStatusChange(...)` line from `updateStatus`, left
everything else alone. `updateStatus` still changes the status, so
`CustomerServiceTest` stays entirely green — that is the point of the
experiment. Only the mock test notices:

    [ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
            <<< FAILURE! -- in com.northstar.crm.service.CustomerNotifierMockTest

    Wanted but not invoked:
    -> at CustomerNotifierMockTest.updateStatusInvokesNotifierWithOldAndNewStatus(CustomerNotifierMockTest.java:30)
    Actually, there were zero interactions with this mock.

Reading it: "wanted but not invoked" is a verify failure, not an assert failure
— the state assertions were all fine. "Zero interactions with this mock" means
the service never touched the collaborator at all, as opposed to calling it with
the wrong arguments, which prints the actual call it did make instead. A dropped
collaborator call is invisible to state-based tests; this is the class of bug
mocks exist to catch.

Restored: line put back, `mvn -q clean test` green at 8.

### Experiment 3 — Mockito test for a method not on the interface

Wrote `InventedApiScratchTest` verifying `notifyCreated(Customer)` — the
starter's method name, with the signature Copilot tends to guess for it (entity
in, no correlation id):

    verify(notifier).notifyCreated(amina);

    [ERROR] .../service/InventedApiScratchTest.java:[28,25] cannot find symbol
      symbol:   method notifyCreated(com.northstar.crm.entity.Customer)
      location: interface com.northstar.crm.service.CustomerNotifier

Rejected, file deleted. Two things worth separating. The compiler catches an
invented *method*, which makes this the safe failure mode — same as the
`CustomerStatus.INACTIVE` failure in lab11-001. What the compiler cannot catch
is an invented *rule* that happens to typecheck, which is why lab11-002's
dropped guards needed a human to spot. Mockito will happily mock any interface
you hand it, including a wrong one.

### Experiment 4 — run the suite twice unchanged

    RUN 1   Tests run: 8, Failures: 0, Errors: 0, Skipped: 0   BUILD SUCCESS
    RUN 2   Tests run: 8, Failures: 0, Errors: 0, Skipped: 0   BUILD SUCCESS

Identical. Only the elapsed times moved (CustomerNotifierMockTest 1.999s then
2.856s — Mockito's first-run agent attach, not test flake). No shared state
between tests: `@BeforeEach` builds a fresh service, and the fixtures are built
per test by the `amina()` / `ravi()` helpers rather than shared as fields, so
one test mutating a Customer cannot reach another.

Known warning, harmless, appears on every Mockito run on JDK 21:

    Mockito is currently self-attaching to enable the inline-mock-maker.
    WARNING: A Java agent has been loaded dynamically (byte-buddy-agent-1.15.4.jar)

Flagged in the GUIDE's Windows troubleshooting as expected while tests are
green. Not suppressed — it is a real future-JDK deprecation, and hiding it now
means meeting it as a hard failure later.

### Main still runs after the refactor

    mvn -q -DskipTests compile
    java -cp target/classes com.northstar.crm.Main

    All customers: [Customer{customerId='CUS-1001', fullName='Amina Khan', status=ACTIVE}, Customer{customerId='CUS-1002', fullName='Ravi Singh', status=PROSPECT}]
    PROSPECT customers: [Customer{customerId='CUS-1002', fullName='Ravi Singh', status=PROSPECT}]
    After activation: Optional[Customer{customerId='CUS-1002', fullName='Ravi Singh', status=ACTIVE}]

Byte-identical to Lab 10's output, which is the point of restoring that harness
rather than keeping the starter's two-line version. The activation line means
`Main` does reach `updateStatus`, so it exercises the notifier on every run — and
prints nothing extra, because the no-arg constructor hands it the no-op. That is
the compatibility claim in lab11-002 row 4 proven at runtime instead of argued:
the extract added a collaborator to `updateStatus` and no existing caller
noticed.


## Security and production review (GUIDE closing section)

1. Which test data is safe to commit, and why?

CUS-1001 Amina Khan and CUS-1002 Ravi Singh, with example.com addresses and
555-01xx phone numbers. All invented for the bootcamp, and example.com is
reserved by RFC 2606 precisely so it can never route to a real mailbox. They are
safe because there is no person behind them to harm, and they are *useful*
because every lab and every note uses the same two, so a test failure is
traceable across labs.

2. Where is human review enforced before AI tests/refactors merge?

Here, in this file — nothing enters src/ without a logged decision and a reason
— and in a team, the same gate is PR review with the AI-generated diff called
out as such. Lab 10 experiment 1 showed the failure mode: an assistant that
commits its own work skips the gate entirely.

3. What risk does an always-green trivial test create?

It converts "we have tests" into a claim nobody re-checks. The suite count goes
up, the coverage bar goes up, and the protection stays at zero. Worse than no
test, because no test at least looks like no test.

4. What is the risk of accepting a refactor without before/after suite runs?

You lose the only evidence that behaviour was preserved. This lab produced a
live example: the GUIDE's reference refactor silently drops three guards
(lab11-002), and the suite as it stands would not have caught the defaulting
loss (lab11-003, gap 3). "It still compiles" and "the tests I happened to write
still pass" are different claims from "behaviour is unchanged".

5. Which values must never appear in tests or mocks?

Real names, emails, phone numbers, credentials, tokens, API keys, and anything
merely shaped like a secret — the fake SSN from Lab 10 experiment 3 included.
Test fixtures get committed, read in review, and pasted into prompts, so a test
file is a publication channel.

6. What would a tech lead audit for meaningful coverage?

Not the percentage. They would pick one business rule and ask which test fails
when it breaks — and then actually break it, which is what experiments 1 and 2
above do. Then: do assertions name domain values, or only shapes and counts; are
the guard clauses tested or just written; is there a gap list, and does it read
like someone honestly looked.

7. How does mocking CustomerNotifier reduce coupling vs concrete
   implementations?

`CustomerService` now depends on a one-method interface it can state in a
sentence, instead of on stdout. The test supplies a mock, a later lab can supply
email or Kafka, and neither change touches the service. Mocking a concrete
notifier would mean the test knows about the delivery mechanism, which is the
coupling the extract removed.

8. How do you keep an audit trail of AI-suggested vs human-verified test code?

The way this file does it: every entry names what produced the code, quotes the
prompt, quotes the suggestion, records accept or reject with a reason, and
pastes real build output rather than describing it. Git history shows what
landed; the log shows why it was allowed to.
