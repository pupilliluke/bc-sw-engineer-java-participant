Lab 29 validation and error handling (reflection questions, checkpoints)

built under examples\lab29-crm, copied from the lab 29 starter. the starter
ships lab 28 security and the ErrorResponse class, so the work was the DTO
constraints, @Valid on create and the four handler bodies. 4 tests green on two
consecutive clean runs. app captured on port 8080, all five experiments run.


REFLECTION QUESTIONS

1. Which design decision most affected correctness (where validation runs)?

running it at the controller boundary. @Valid on the create method rejects the
body before CustomerService sees it, so the service never has to check what it
was handed. experiment 1 removed @Valid and a blank name and not-an-email were
stored and read back at 201.

2. What evidence proves the error contract is stable?

the same envelope on four statuses. 400, 404, 409 and 500 all return timestamp,
status, error, message, correlationId and violations, with violations an empty
array rather than null on the three that have no field errors.
ErrorEnvelopeTest asserts the fields and not only the status.

3. Which failure was hardest to diagnose (missing @Valid, advice not scanned,
missing Bearer)?

none of those three. a malformed body coming back 500 instead of 400. the
mapping is correct as the GUIDE writes it, and the cause is that
@ExceptionHandler(Exception.class) catches HttpMessageNotReadableException
before Boot can answer it. nothing in the 500 body says which exception it was,
the log is the only place it shows.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab29-crm under examples/ | Pass, copied from starter/ |
| A2 | lab 28 security packages present in starter | Pass, SecurityConfig, security/, AuthController, AdminController |
| A3 | existing ErrorResponse and FieldViolation unchanged | Pass, no path field and no rejectedValue added |
| B1 | annotated CustomerRequest with @NotBlank / @Email | Pass, jakarta.validation on id, name, email, status |
| B2 | @Valid on create, no PATCH | Pass |
| B3 | invalid POST rejected at the boundary with Bearer | Pass, 400 with three violations |
| C1 | validation 400 envelope with lab-request-001 | Pass, and lab-request-001 by default when the header is absent |
| C2 | IllegalArgumentException 404, IllegalStateException 409 | Pass, CUS-9999 and duplicate CUS-1001 |
| C3 | safe 500 fallback, docs/error-contract.md present | Pass, fixed message in the body, stack in the log |
| D1 | ErrorEnvelopeTest Tests run: 4 including the 401 | Pass, two consecutive clean runs |
| D2 | happy GET with Bearer still 200 | Pass, CUS-1001 and CUS-1002 |
| D3 | no secrets, stack traces or target/ committed | Pass, target/ and .env ignored |

FULL PATH

| Item | Result |
| --- | --- |
| All five failure experiments | Pass |
| Lab 14 / 16 unify note in docs/error-contract.md | Pass |
| PATCH status DTO | not added, the GUIDE marks it optional and not in the starter |
| SOAP / Spring-WS alignment paragraph | not added, optional full path |

SECURITY AND PRODUCTION REVIEW

1. which inputs are untrusted?

the JSON body, the customer id in the path, and the X-Correlation-Id and
Authorization headers. the body is constrained by Bean Validation, the id is a
map key and a miss is a 404, and the correlation header is echoed into the
envelope with no length limit on it.

2. where are authn, authz and validation enforced?

authn and authz in the lab 28 filter chain, validation at the controller through
@Valid. the filter chain runs first, so an unauthenticated bad request is a 401
and never learns which fields were invalid.

3. which values are sensitive, and never in a client 500 body?

JWT_SECRET, the bearer tokens and the lab passwords. the 500 body is the fixed
string Unexpected error, the class name and stack go to the log, and no
violation carries a rejected value.

WINDOWS HOW-TO PASS CRITERIA

| # | Confirm | Result |
| - | --- | --- |
| 1 | workspace open in IntelliJ with SDK 21 | Pass, temurin-21.0.4 |
| 2 | lab project under examples/lab29-crm | Pass |
| 3 | GUIDE deliverables and checkpoints complete | Pass |
| 4 | commands succeed | Pass, mvn -B test and spring-boot:run |
| 5 | evidence under notes/screenshots/lab-29 | Pass, kept in the project as since lab 14 |
