Lab 18 Mockito and mocking with AI assistance (reflection questions,
checkpoints, manual verification, failure experiments)

built under examples\lab18-crm, copied forward from lab17-crm. no production
class changed and the pom needed no edit, mockito 5.14.2 core and
junit-jupiter were already test scoped. CustomerServiceMockitoTest 12 and
CustomerServiceBddMockTest 3 added, ninety-eight tests green, service package
still 46 of 46 lines and 20 of 20 branches so the 0.80 gate passes unchanged.


REFLECTION QUESTIONS

1. Which design decision most affected correctness?

sharing one mock repository between the service and a real CustomerValidator,
built by hand in @BeforeEach rather than by @InjectMocks. the validator takes
the repository through its own constructor, so a second mock there is
invisible until a uniqueness rule stops firing, which is experiment 7.

2. What evidence proves the implementation works?

the never() verifies and the captor, not the count. four tests prove save was
not called, one proves existsByEmail was not called after a duplicate id, and
the captor reads ACTIVE off the customer that crossed the port where
save(ravi) would have passed regardless. experiment 8 is the red-green run,
reordering validateNew turns two of the new tests red and leaves all
eighty-three lab 17 tests green.

3. Which failure was hardest to diagnose?

UnnecessaryStubbingException, because it fires from afterEach and names a
stub, not the call that never happened. in experiment 7 it was the real
signal, the two exists stubs on the first mock went dead because the validator
was reading a different one.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab18-crm under examples/ | Pass |
| A2 | mockito core and junit-jupiter test scoped, tree confirms both | Pass, 5.14.2, already present |
| A3 | @ExtendWith(MockitoExtension.class) on the mock suites | Pass, both classes |
| A4 | lab 17 JaCoCo and Surefire still present | Pass, gate still passes at 1.00 |
| B1 | shared mock repo wires validator and service | Pass, one @Mock, both built in @BeforeEach |
| B2 | activate Ravi, stub find and save, verify | Pass, plus inOrder and verifyNoMoreInteractions |
| B3 | CUS-9999 not found, never().save | Pass, in both styles |
| B4 | ArgumentCaptor on add Amina | Pass, id, name, status and createdAt |
| C1 | CustomerServiceBddMockTest green with given/then/should | Pass, 3 tests |
| C2 | Copilot review log or manual equivalent | Pass, lab18-001 marked manual |
| C3 | no mocking of the class under test | Pass, only CustomerRepository is mocked |
| D1 | two consecutive mvn test runs identical | Pass, plus reverse order and one test alone |
| D2 | isolation policy documented | Pass, docs/isolation-policy.md and the README |
| D3 | no secrets, no debug mockingDetails, no target/ | Pass, target/ ignored since lab 8 |


MANUAL VERIFICATION

| # | Check | Result |
| --- | --- | --- |
| 1 | the mockito suite isolates the service from the real map | Pass, no repository implementation is imported |
| 2 | activate Ravi stubs find and save and asserts ACTIVE with lab-request-001 | Pass |
| 3 | unknown id verifies never().save | Pass, 404 and CUSTOMER_NOT_FOUND asserted too |
| 4 | captor asserts Amina's id, name and status on save | Pass |
| 5 | BDDMockito test shows the same semantics | Pass, 3 tests |
| 6 | lab 17 tests still pass | Pass, all eighty-three, none edited |
| 7 | illegal transition never saves | Pass, permanent test, the guide's experiment 2 |
| 8 | no sensitive values in tests or git | Pass, example.com addresses only |
| 9 | two consecutive mvn test runs match | Pass, 98 both times |
| 10 | README says which suites are mocked and which are real | Pass, README and docs/isolation-policy.md |


FAILURE EXPERIMENTS

1. stubbed findById to throw RuntimeException("connection to crm-db refused").
the test errored rather than failed and the frame named
DefaultCustomerService.changeStatus line 57. the service does not translate an
infrastructure fault, it lets it through, and the generic 500 is the facade's
job, which CustomerApiFacadeTest has asserted since lab 16. restored.

2. the guide's second experiment, stub Amina ACTIVE and ask for PROSPECT, is a
permanent test here rather than an experiment,
illegalTransitionIsA409AndNeverSaves. 409, BUSINESS_CONFLICT, the message
carries ACTIVE -> PROSPECT and save is verified never. nothing to restore.

3. called addCustomer twice against the implicit times(1) on the verify.
TooManyActualInvocations, "Wanted 1 time ... But was 2 times", with both call
sites printed. the mock is rebuilt per test by the extension, so this was two
calls inside one test and not leakage from another. restored.

4. added an existsById stub to the not-found test, where the path stops at
findById. UnnecessaryStubbingException thrown from
MockitoExtension.afterEach, so it is strictness and not an assertion. this is
why the never-saves tests stub findById only and leave save unstubbed.
restored.

5. Thread.sleep(2000) in an activation test. the class went from 1.56 s to
3.46 s and stayed green. every collaborator is a mock answering on the calling
thread, so there is nothing to wait for. removed, same answer as lab 17.

6, not in the guide. replaced the real validator with
mock(CustomerValidator.class). four failures and one error, every rejection in
the class disappeared and ACTIVE to PROSPECT was written through, while the
happy-path tests stayed green. that is the honor violation the guide warns
about, one collaborator away from mocking the class under test. restored.

7, also not in the guide. built the validator on a second mock repository
while the service kept the first. only the two uniqueness tests failed, with
nothing thrown, and the exists stubs on the first mock were then reported
unnecessary. narrower than lab 17's version of the same experiment, which used
two in-memory repositories and got the failures without that second signal.
restored.

8, also not in the guide, and the only one that edits a production class.
reordered CustomerValidator.validateNew to look up the email before the id.
one failure and one error, both in the new suite, NeverWantedButInvoked on
existsByEmail and the now-dead existsById stub in the duplicate-email test.
restored, and the section below is what it shows.


WHAT THE INTERACTION TESTS ADD

the coverage number did not move, 46 of 46 lines and 20 of 20 branches before
and after. what moved is the ordering evidence. reordering validateNew to look
up the email before the id keeps every lab 17 test green, because both orders
reject a duplicate id with the same 409 and store the same nothing, and turns
aDuplicateIdIsRejectedBeforeTheEmailIsLookedUp red with NeverWantedButInvoked
naming CustomerValidator line 59 and the address it passed. a rule running in
the wrong order is invisible to state assertions and to a line counter.
