Northstar CRM build (Lab 15)

  mvn -B clean test
  mvn -q compile exec:java "-Dexec.mainClass=com.northstar.crm.Main"

Same jar caveat as Lab 14, java -jar target\customer-service.jar still fails with
NoClassDefFoundError because the jar bundles no dependencies. exec:java supplies
the Maven runtime classpath.

Adds a service layer on top of Lab 14's API boundary. The store moved out of the
service and behind CustomerRepository, the business rules moved into
CustomerValidator, and CustomerService became an interface with
DefaultCustomerService behind it. Artifact is still
com.northstar:customer-service:0.1.0-SNAPSHOT, copied forward from lab14-crm.

WIRING

  CustomerRepository repo = new InMemoryCustomerRepository();
  CustomerValidator validator = new CustomerValidator(repo);
  CustomerService service = new DefaultCustomerService(repo, validator);
  CustomerApiFacade api = new CustomerApiFacade(service);

One repository instance, shared by the validator and the service. With two
instances email uniqueness stops working and every test still passes, which is
failure experiment 4 below.

Spring builds the same graph in Lab 22, from @Service and a constructor rather
than from Main. The constructor parameter lists do not change.

LAYERS

| Class | Role | Knows about |
| --- | --- | --- |
| CustomerApiFacade | API edge, shape validation, stable codes | DTOs, mapper, CustomerService |
| CustomerService | use-case interface | entity only |
| DefaultCustomerService | orchestration, timestamps | validator, repository |
| CustomerValidator | business meaning | repository, CustomerStatus |
| CustomerRepository | persistence port | entity only |
| InMemoryCustomerRepository | the adapter, owns the Map | java.util |

The Map is a private field of InMemoryCustomerRepository with no getter, and
findAll returns a copy of its values. Nothing in service or api imports
HashMap, java.sql, Connection or EntityManager.

TRANSITIONS

| From | Allowed to | Rejected |
| --- | --- | --- |
| PROSPECT | ACTIVE, CLOSED | PROSPECT, SUSPENDED |
| ACTIVE | SUSPENDED, CLOSED | ACTIVE, PROSPECT |
| SUSPENDED | ACTIVE, CLOSED | PROSPECT, SUSPENDED |
| CLOSED | nothing | everything |

Anything not in the allowed column throws IllegalStateException carrying the
from, the to and the correlation id. Two consequences of that: CLOSED is
terminal, and same-status is a rejection.

SAME STATUS IS A REJECTION, NOT A NO-OP

activate on a customer who is already ACTIVE fails. A second activate means the
caller expected a PROSPECT, so the mismatch is reported rather than swallowed.
The cost is that changeStatus is not idempotent, so a client retrying after a
timeout cannot tell its own retry from a real conflict. Lab 14 left the same gap
on create and this lab does not close it either.

EMAIL CASE POLICY

Case-insensitive. existsByEmail trims and lowercases with Locale.ROOT on both
sides, so AMINA.KHAN@example.com is a duplicate of amina.khan@example.com. The
stored value keeps whatever case the client sent; only the comparison is
normalised.

BEAN VALIDATION VERSUS CustomerValidator

Lab 14's annotations answer is this payload well formed, using nothing but the
payload. CustomerValidator answers is this legal right now, and every one of its
rules needs something the payload does not carry, the current store for
uniqueness and the current status for a transition. That is why none of them can
be an annotation, and why the validator holds a repository.

FIXTURES

| ID | Name | Status | Email |
| -- | ---- | ------ | ----- |
| CUS-1001 | Amina Khan | ACTIVE | amina.khan@example.com |
| CUS-1002 | Ravi Singh | PROSPECT | ravi.singh@example.com |

Correlation id lab-request-001. It is a parameter on changeStatus rather than
constructor state, because one service instance serves many requests.

DEMO OUTPUT

  activated CUS-1002 status=ACTIVE
  expected failure: illegal status transition ACTIVE -> PROSPECT [lab-request-001]
  CUS-1001 still: ACTIVE

Full transcript in notes/screenshots/lab-15/02-main-demo.txt.

TESTS

  mvn -B clean test
  mvn -B test -Dtest=CustomerValidatorTest

Forty tests, all green. New in this lab, InMemoryCustomerRepositoryTest 5,
CustomerValidatorTest 7 and DefaultCustomerServiceTest 9. CustomerApiFacadeTest
grew from 5 to 8, with the conflict code, the facade activate path and proof
that a rejected transition leaves Amina ACTIVE. Lab 14's CustomerServiceTest is
gone, its cases live in DefaultCustomerServiceTest against the new API.

WHAT LAB 15 CHANGED

CustomerRepository was a Lab 8 stub class that threw
UnsupportedOperationException. It is now the interface, with
InMemoryCustomerRepository behind it.

CustomerService was Lab 14's concrete class holding a HashMap. The name now
belongs to the interface and the implementation is DefaultCustomerService, which
holds no store at all. It kept the timestamp stamping and the PROSPECT default;
its blank and duplicate guards moved to CustomerValidator.

CustomerApiFacade gained changeStatus and two codes, CUSTOMER_CONFLICT and
CUSTOMER_TRANSITION_INVALID. Business rules below the facade throw plain
IllegalArgument and IllegalState exceptions; the facade is where they become a
code a client can switch on.

CustomerController and the CustomerRequest and CustomerResponse stubs are
deleted. Lab 14's README called this the Lab 15 tidy-up. They were Lab 8
scaffolding that threw UnsupportedOperationException, nothing referenced them,
and keeping them would have forced legacy methods onto the new interface.

FAILURE EXPERIMENTS

All five were run and restored.

| # | Experiment | Observed | Restored |
| --- | --- | --- | --- |
| 1 | Repository save throws for CUS-1003 | service surfaces it, Amina and Ravi intact, but the facade reports it as CUSTOMER_CONFLICT | save restored |
| 2 | CLOSED to ACTIVE and ACTIVE to PROSPECT | both rejected by the validator, both messages carry the correlation id | no change needed |
| 3 | changeStatus to ACTIVE twice | second call rejected as ACTIVE -> ACTIVE, matches the table above | no change needed |
| 4 | Validator given its own repository instance | duplicate email accepted, CUS-1004 stored with Amina's address, no test failed | shared instance restored |
| 5 | setStatus before validateTransition | Amina left PROSPECT after a rejected call, and CLOSED to ACTIVE did the same to CUS-1003 | order restored, 40 green |

In experiment 1 a storage outage came back to the client as CUSTOMER_CONFLICT,
because the facade cannot tell a rule violation from an infrastructure failure
when both arrive as IllegalStateException. Lab 16's typed exceptions are the
fix.

Experiment 4 produced no failure of any kind. The store ended up with two
customers sharing an email because the validator was asking a second, empty
repository whether the address was taken. Only the Main transcript showed it.

Experiment 5 is why validateTransition is called before setStatus and why
DefaultCustomerServiceTest asserts the stored status after the exception, not
just that an exception was thrown.

SECURITY AND PRODUCTION REVIEW

1. Which inputs are untrusted?

Everything reaching the facade, including the target status on changeStatus and
the correlation id. The id is echoed into logs and messages and is never treated
as identity or authorisation.

2. Where is each check enforced?

Shape at the facade, meaning in CustomerValidator, storage integrity nowhere yet.
The in-memory repository has no unique constraint, so email uniqueness is only
as good as the validator that runs before save. A real database would enforce it
underneath as the last guarantee. Authentication and authorisation are still
absent, and activate is exactly the operation that will need them.

3. Which values are sensitive?

None today, example.com addresses and 555-01xx numbers. Status history will
matter later, who activated a customer and when is an audit question, and this
lab stores only the current value plus updatedAt.

CLEANUP

  mvn -B clean
  git status

No containers, no ports. Keep lab15-crm for Lab 16.

NOTES

Layer responsibilities and the Spring preview are in docs/service-layer-notes.md.
Lab 14's entity versus DTO reasoning is still in docs/dto-boundary-notes.md.
Pre-lab exercises and reflection answers are in notes/Week 2/Module 15/. Full
GUIDE at labs/Week 2 - Backend, AI Tools and Testing/module-15/lab15/.
