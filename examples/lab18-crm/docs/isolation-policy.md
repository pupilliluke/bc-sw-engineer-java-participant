Isolation policy (Lab 18)

Which suites run against a mock, which run against the real in-memory
repository, and how to choose stub or verify. Ten test classes, ninety-eight
tests, two of the classes new in this lab.

WHICH SUITE USES WHAT

| Class | Repository | Why |
| --- | --- | --- |
| CustomerServiceMockitoTest | @Mock | unit isolation, 12 tests, new here |
| CustomerServiceBddMockTest | @Mock | the same idea in BDDMockito wording, 3 tests |
| CustomerServiceTests | real InMemoryCustomerRepository | lab 17, 16 tests, kept |
| CustomerValidatorTest | real | the uniqueness rules need a store to read |
| CustomerValidatorParameterizedTest | none | validateTransition never touches the port |
| CustomerApiFacadeTest | real, and mock(CustomerService.class) twice | edge behaviour, mocks only where the real service cannot produce the case |
| InMemoryCustomerRepositoryTest | it is the subject | the adapter's own contract |
| CustomerMapperTest, CustomerTest, ErrorResponseTest, GlobalExceptionHandlerTest | none | no collaborator to isolate |

CustomerValidator is real in both service suites and is built on the same
repository instance the service holds. That is the wiring production uses, and
it is the reason these tests do not use @InjectMocks alone: the validator
takes the repository through its own constructor and a second mock there is
invisible until a uniqueness rule quietly stops firing.

WHY BOTH STAY

The mocked suites and the lab 17 suite answer different questions. The mock
proves which calls DefaultCustomerService made through the port: findById then
save, save never on a rejection, existsByEmail never after a duplicate id. The
real repository proves the write survived, that a rejected transition left the
stored status alone and that lookups are by value.

Neither subsumes the other. Deleting the lab 17 suite would leave nothing
asserting that InMemoryCustomerRepository and the service agree; deleting the
mocked suite would leave the negatives resting on counting stored customers.
Lab 19 adds the layer above, so the real-repository suite is also the one that
keeps working when the collaborators change shape.

STUB OR VERIFY

Stub what the unit reads, verify what the unit does.

  when(repository.findById("CUS-1002")).thenReturn(Optional.of(ravi));   arrange
  verify(repository).save(any(Customer.class));                          assert

A when() written after the call under test never applies to a call that has
already happened, so the order in the test body is fixed.

Three rules this suite follows:

Stub save with thenAnswer(call -> call.getArgument(0)) wherever the service
returns the saved customer. An unstubbed mock hands back null and the test
then fails on a NullPointerException rather than on the rule.

Do not stub what the path never calls. MockitoExtension runs strict stubs, so
an unused when() fails the run with UnnecessaryStubbingException. The
never-saves tests stub findById only.

Do not verify with equals where the field under test is not part of equals.
Customer.equals compares customerId alone, so verify(repository).save(ravi)
passes whatever status the argument carried. An ArgumentCaptor, or an argThat
on the two fields, is what reads the status.

WHAT IS NEVER MOCKED

DefaultCustomerService, the class under test. A test that mocks it asserts the
stub.

CustomerValidator, which holds the ALLOWED transition table. Experiment 6 in
notes/screenshots/lab-18/03-failure-experiments.txt replaces it with a mock:
four rejections disappear, ACTIVE to PROSPECT is written through, and the
happy-path tests stay green.

CustomerStatus and Customer. Enum constants and value objects have nothing to
control, and a mocked Customer breaks the equals that verify and the captor
depend on.

CORRELATION ON FAILURE PATHS

Every mocked failure path asserts the code and the status hint, and the paths
that carry a correlation id assert lab-request-001 on the exception. The type
alone does not identify the rule, because BusinessException is thrown by all
of them.

  404 CUSTOMER_NOT_FOUND    CUS-9999, findById stubbed empty
  409 BUSINESS_CONFLICT     illegal transition, duplicate id, duplicate email
  400 VALIDATION_FAILED     a null target status

An infrastructure fault is not translated here. Experiment 1 stubs findById to
throw and the RuntimeException leaves changeStatus untouched; the generic 500
is the facade's job and CustomerApiFacadeTest asserts it.

WHAT MOCKS DO NOT PROVE

The stub is an assumption about CustomerRepository, not a check of it. A mock
saying findById returns Optional.empty() proves what the service does with
that answer, not that InMemoryCustomerRepository ever gives it. That contract
is InMemoryCustomerRepositoryTest's, and a repository against a real store is
an integration concern that arrives later.

Coverage is not what changed either. The service package was at 46 of 46 lines
and 20 of 20 branches in lab 17 and still is; the mocked suites walk paths the
real-repository suites already reached. What they add is the interaction
evidence, which no line counter can see.
