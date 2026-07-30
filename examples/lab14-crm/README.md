Northstar CRM build (Lab 14)

  mvn -B clean verify
  mvn -B -q exec:java

Lab 12's java -jar target\customer-service.jar no longer runs on its own. The jar
bundles no dependencies and Main now reaches jakarta.validation through the
facade, so it needs the Maven runtime classpath and exec:java is what supplies
it. Checked, not assumed, it fails with NoClassDefFoundError: jakarta/validation/
Validation at CustomerApiFacade line 39.

Adds an API contract boundary on top of Lab 12. Every call in the demo goes
through CustomerApiFacade, so everything printed as an API response is a
CustomerResponseDTO and the Customer entity never leaves the mapper. Artifact is
com.northstar:customer-service:0.1.0-SNAPSHOT, same coordinates as Labs 9 to 12,
the project carries forward rather than forking.

TESTS

  mvn -B clean test
  mvn -B test -Dtest=CustomerRequestDTOValidationTest

Twenty-two tests, all green. CustomerTest 2 and CustomerServiceTest 6 carried
from Lab 12, plus CustomerRequestDTOValidationTest 5, CustomerApiFacadeTest 5 and
CustomerMapperTest 4. Mockito is finally earning its place in the pom, the
never-reaches-the-service test needs a mock of CustomerService.

VALIDATION RULES, CustomerRequestDTO

| Field | Constraints | Required |
| --- | --- | --- |
| customerId | @NotBlank, @Size(max=32), @Pattern(CUS-\d{4}) | yes |
| fullName | @NotBlank, @Size(2..100) | yes |
| email | @NotBlank, @Email, @Size(max=254) | yes |
| phone | @Size(max=32) | no |
| status | @NotBlank, @Size(1..32) | yes |

status is checked for presence but not for value. ACTIVE, PROSPECT, SUSPENDED
and CLOSED are the legal values and the mapper is what enforces that, see the
status note below.

phone has a size limit and no presence constraint, which is what optional means
here. @Size ignores null, so leaving phone out is legal and sending a 40
character one is not.

RESPONSE CONTRACT, CustomerResponseDTO

| Field | Source | Notes |
| --- | --- | --- |
| customerId | entity | stable identity |
| fullName | entity | client-supplied on create |
| email | entity | client-supplied on create |
| status | entity | enum name as text |
| createdAt | entity, server-stamped | Instant, UTC |
| updatedAt | entity, server-stamped | Instant, UTC |

phone is accepted, stored, and not returned. It is the smallest honest example
of the no-leak rule in this project, the response is a chosen subset rather than
whatever the entity happens to hold.

FIXTURES

| ID | Name | Status | Email |
| -- | ---- | ------ | ----- |
| CUS-1001 | Amina Khan | ACTIVE | amina.khan@example.com |
| CUS-1002 | Ravi Singh | PROSPECT | ravi.singh@example.com |

Correlation id lab-request-001, passed into every facade call and echoed on every
failure. It rides a header-shaped parameter, not a DTO field.

SAMPLE INVALID

  email=not-an-email
  -> IllegalArgumentException
     [lab-request-001] CUSTOMER_VALIDATION_FAILED: email: email must be a valid address

  fullName=" "
  -> [lab-request-001] CUSTOMER_VALIDATION_FAILED: fullName: fullName is required;
     fullName: fullName must be between 2 and 100 characters

A blank name trips two constraints, not one. @NotBlank rejects the whitespace and
@Size sees length 1 against a minimum of 2. Both messages ship, because a client
fixing one and resubmitting should not discover the second on the next round trip.

  status=ACTVE
  -> [lab-request-001] CUSTOMER_STATUS_INVALID: status: ACTVE is not a known status

  get CUS-9999
  -> [lab-request-001] CUSTOMER_NOT_FOUND: customerId: CUS-9999

WHAT LAB 14 ADDED

CustomerRequestDTO and CustomerResponseDTO in dto, CustomerMapper in mapper, and
CustomerApiFacade in api. The facade is the API edge and its create method does
four things in a fixed order, validate, map, call the service, map back. Nothing
public in api returns a Customer.

CustomerService gained two methods. createCustomer(Customer) is the entry point
the facade uses, it keeps the duplicate and blank guards and stamps createdAt and
updatedAt, and the old five-argument createCustomer now delegates to it so Lab
12's tests still pass unchanged. findByCustomerId returns an Optional so the
facade can build its own not-found message rather than inheriting the service's.

Customer gained updatedAt. createdAt was already there from Lab 10.

DESIGN DECISIONS

customerId carries @Pattern(CUS-\d{4}) on top of the guide's @NotBlank and
@Size. Module 14 exercise 3 already decided the id has a shape, and a pattern is
the cheapest way to say so without a custom annotation. It also means the
oversized-id case fails for a reason a client can read.

status is a String on the DTO, not CustomerStatus. A typed field would fail at
deserialization with a framework message, before any of this code runs. A String
lets the payload arrive, the validator pass it, and the mapper reject it with a
stable code the client can act on. The cost is that the validator alone does not
prove a payload is good, which is worth knowing.

The entity keeps LocalDateTime and the response publishes Instant, converted at
UTC in CustomerMapper.toInstant. One conversion in one place, so the wire format
does not depend on the server's zone.

Failures carry a stable code, CUSTOMER_VALIDATION_FAILED, CUSTOMER_STATUS_INVALID
or CUSTOMER_NOT_FOUND, plus the correlation id. Hibernate's raw messages are used
as field detail but the class names and rejected values stay out of the message.

Violations are sorted before joining. validate() returns a Set, so without a sort
the same bad payload can report its fields in a different order between runs.

The Lab 8 CustomerController and the CustomerRequest and CustomerResponse stubs
are still in the tree and still throw UnsupportedOperationException. The facade
supersedes that path. Deleting them is a Lab 15 tidy-up, not a Lab 14 deliverable.

TWO LAYERS

Structural validation is on the DTO and runs at the boundary, is the payload
well formed. Business validation is in CustomerService and needs the current
store, does CUS-9999 exist and is CUS-1001 already taken. Experiment 4 below is
what happens when the first layer is skipped.

FAILURE EXPERIMENTS

All five were run and restored. Output is under notes/screenshots/lab-14/.

| # | Experiment | Observed | Restored |
| --- | --- | --- | --- |
| 1 | Remove hibernate-validator from pom | jakarta.validation.NoProviderFoundException at Validation.buildDefaultValidatorFactory, every validation test errors in setUp | dependency restored, 22 green |
| 2 | Bad email, blank name, blank status | facade throws before CustomerService is touched, each with correlation id | no change needed |
| 3 | Create CUS-1001 twice | IllegalStateException "Customer id already exists: CUS-1001", a service rule, no stable code and no field path | no change needed |
| 4 | Comment out validator.validate | invalid email saved with no failure at all, blank name still caught but by the service in Lab 12's message format, status typo still caught at the mapper | validate call restored |
| 5 | status=ACTVE | zero constraint violations, CustomerStatus.valueOf throws, facade translates to CUSTOMER_STATUS_INVALID | no change needed |

Experiment 4 is the one worth keeping. The email was not merely reported badly,
it was accepted and stored, because nothing below the boundary checks email at
all. The blank name survived only because Lab 12 happened to guard it, and the
error it produced has no code and no field path, which is a worse contract even
when the outcome is the same.

Experiment 3 is the difference the lab is asking about. A duplicate id is not a
malformed payload, it is a legal payload that conflicts with the current store,
so it cannot be an annotation and it cannot be checked at the boundary.

SECURITY AND PRODUCTION REVIEW

1. Which inputs are untrusted?

Every field on CustomerRequestDTO. All five arrive from a caller and all five now
carry constraints. The correlation id is untrusted too, it is echoed into logs and
messages and is never treated as identity or authorisation.

2. Where is validation enforced?

At the facade, before the mapper and before the service. Lab 12's requireNonBlank
and requireUniqueId stay where they are as the second layer, which experiment 4
shows is a backstop rather than a substitute. Authentication and authorisation are
still absent, no caller identity exists in this project.

3. Which values are sensitive and must not reach a response DTO?

None are sensitive today, the fixtures are example.com addresses and 555-01xx
numbers. The rule is written for the fields that arrive later, credential
material, internal risk flags, audit columns and any storage key that differs
from customerId. CustomerMapperTest asserts the response field set so adding one
by accident fails the build.

CLEANUP

  mvn -B clean
  git status

No containers, no ports. Keep lab14-crm for Lab 15.

NOTES

Entity versus DTO reasoning is in docs/dto-boundary-notes.md. Pre-lab exercises
and reflection answers are in notes/Week 2/Module 14/. Full GUIDE at
labs/Week 2 - Backend, AI Tools and Testing/module-14/lab14/.
