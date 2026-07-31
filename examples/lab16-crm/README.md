Northstar CRM build (Lab 16)

  mvn -B clean test
  mvn -q compile exec:java "-Dexec.mainClass=com.northstar.crm.Main"

Same jar caveat as Lab 15, java -jar target\customer-service.jar still fails with
NoClassDefFoundError because the jar bundles no dependencies. exec:java supplies
the Maven runtime classpath.

Adds one error model on top of Lab 15's service layer. Every failure below the
facade is a BusinessException carrying a code, a status hint and the correlation
id; GlobalExceptionHandler turns those, Bean Validation violations and anything
unexpected into one ErrorResponse; the facade returns ApiResult rather than
throwing. Artifact is still com.northstar:customer-service:0.1.0-SNAPSHOT,
copied forward from lab15-crm.

WIRING

  CustomerRepository repo = new InMemoryCustomerRepository();
  CustomerValidator validator = new CustomerValidator(repo);
  CustomerService service = new DefaultCustomerService(repo, validator);
  CustomerApiFacade api = new CustomerApiFacade(service);

Unchanged from Lab 15. The facade builds its own GlobalExceptionHandler, and a
second constructor takes one for a test that needs to see the mapping calls.

ERROR SHAPE

  {"timestamp":"2026-07-31T15:07:36.951Z","status":404,
   "error":"CUSTOMER_NOT_FOUND","message":"Customer not found: CUS-9999",
   "correlationId":"lab-request-001","errors":{}}

Six fields on every failure. errors is always present and is {} when no single
field is to blame.

STATUS MAP

| Case | status | code |
| --- | --- | --- |
| Bean Validation on the request DTO | 400 | VALIDATION_FAILED |
| unknown status string, missing request | 400 | VALIDATION_FAILED |
| unknown customer id | 404 | CUSTOMER_NOT_FOUND |
| illegal transition, duplicate id, duplicate email | 409 | BUSINESS_CONFLICT |
| anything not expected | 500 | INTERNAL_ERROR |

409 rather than 422 for the illegal transition. What makes ACTIVE -> PROSPECT
fail is the stored status, not the request read on its own, and the same request
against Ravi succeeds. The rule for this project is that if the answer changes
when the stored data changes, it is 409. Reasoning and the Spring forward map
are in docs/error-model-notes.md.

CATCH ORDER

  catch (BusinessException ex)   // specific, first
  catch (Exception ex)           // fallback, last

Same order in create, getById and changeStatus. Experiment 4 below is what
happens without it.

APIRESULT

Lab 15's facade returned a DTO and threw on failure, so every caller wrote a
try/catch and Main printed exception class names. The facade now returns Ok or
Fail, and the sealed interface makes a switch over the two exhaustive without a
default branch. Nothing in Main catches anything.

  case ApiResult.Ok ok     -> ok.body()
  case ApiResult.Fail fail -> fail.error().toJson()

CORRELATION

lab-request-001, required non-blank at the facade edge and carried on the
exception rather than rebuilt at each layer. A blank id is thrown rather than
returned as a Fail, because without an id there is nothing to put in the
ErrorResponse and a caller that omits it has a bug rather than a bad request.

Lab 15 printed the id twice on a facade-wrapped failure, once from the service
message and once from the facade's own code line. It appears once now, in its
own field, and the messages no longer carry it inline.

Generating an id when a client sends no header is the documented policy for the
Spring lab, not implemented here; there is no request header to read yet.

WHAT LEAVES THE SERVER

| Detail | Log | Client payload |
| --- | --- | --- |
| stack trace | yes, ERROR with the throwable | no |
| exception class and message | yes | no |
| failing field name and rule | yes | yes |
| customer id | yes | yes |
| email address | no | no |
| correlationId | yes | yes |

The duplicate-email conflict reads "email is already registered for another
customer" without repeating the address. The id is a stable internal identifier
and stays; the address is the customer's own data. Lab 15 echoed it back.

toJson escapes quotes, backslashes, newlines and control characters, so a
message that carries any of them cannot break the document. There is no JSON
library on this classpath.

FIXTURES

| ID | Name | Status | Email |
| -- | ---- | ------ | ----- |
| CUS-1001 | Amina Khan | ACTIVE | amina.khan@example.com |
| CUS-1002 | Ravi Singh | PROSPECT | ravi.singh@example.com |
| CUS-9999 | not stored | | |

DEMO OUTPUT

  400 bad email  -> fail  {"timestamp":"...","status":400,"error":"VALIDATION_FAILED","message":"Validation failed","correlationId":"lab-request-001","errors":{"email":"email must be a valid address"}}
  404 unknown    -> fail  {"timestamp":"...","status":404,"error":"CUSTOMER_NOT_FOUND","message":"Customer not found: CUS-9999","correlationId":"lab-request-001","errors":{}}
  409 transition -> fail  {"timestamp":"...","status":409,"error":"BUSINESS_CONFLICT","message":"illegal status transition ACTIVE -> PROSPECT","correlationId":"lab-request-001","errors":{}}
  1001 still     -> ok    CustomerResponseDTO{customerId='CUS-1001', ..., status=ACTIVE, ...}

Full transcript in notes/screenshots/lab-16/02-main-demo.txt.

TESTS

  mvn -B clean test
  mvn -B test -Dtest=GlobalExceptionHandlerTest

Fifty-two tests, all green. New in this lab, GlobalExceptionHandlerTest 6 and
ErrorResponseTest 4. CustomerApiFacadeTest grew from 8 to 11 for the ApiResult
rewrite, the generic 500 and the blank correlation id. CustomerValidatorTest and
DefaultCustomerServiceTest assert the status hint and the code now rather than
the JDK exception type.

WHAT LAB 16 CHANGED

CustomerValidator and DefaultCustomerService throw BusinessException instead of
IllegalArgumentException and IllegalStateException. The status is decided at the
throw site, which is what Lab 15's failure experiment 1 asked for.

addCustomer and validateNew take a correlation id. changeStatus already did.
Every rejection below the facade can now carry the caller's id, which is what
the exception needs to be mapped without the facade re-supplying it.

CustomerApiFacade returns ApiResult and holds a GlobalExceptionHandler. Its five
Lab 15 code constants are gone; the codes live on BusinessException and in the
handler. get is renamed getById to match the lab guide.

CustomerNotFoundException is deleted. It was Lab 8 scaffolding that nothing
referenced, and BusinessException.notFound builds the same message with a status
and a correlation id attached.

FAILURE EXPERIMENTS

All five were run and restored.

| # | Experiment | Observed | Restored |
| --- | --- | --- | --- |
| 1 | Repository findById throws a bare RuntimeException carrying SQL | generic 500, no SQL and no host in the JSON, stack in the server log | findById restored |
| 2 | Blank fullName and bad email in one request | both fields in errors, email then fullName | no change needed |
| 3 | Not-found CUS-9999 twice | identical shape and correlation id both times | no change needed |
| 4 | Catch Exception before BusinessException | javac rejects it; deleting the specific catch instead compiles and the 409 returns as a 500 | catch order restored |
| 5 | ex.toString() in the 500 message | the database host and the SQL reach the client payload | fixed message restored |

Experiment 4 is the one worth writing down. The wrong order is not a runtime bug
in this code, it is a compile error, "exception BusinessException has already
been caught", because BusinessException extends RuntimeException. The failure
the guide describes needs the specific catch deleted rather than moved, and then
the 409 conflict comes back as INTERNAL_ERROR with the conflict message gone.

Experiment 5 is caught by exactly two tests, unexpectedIsGeneric500 and
unexpectedFailureBecomesAGeneric500. Both assert the fixed message and then that
the payload does not contain jdbc or the exception class name; the message
equality is the assertion that trips first.

SECURITY AND PRODUCTION REVIEW

1. Which inputs are untrusted?

Everything reaching the facade, including the correlation id. It is echoed into
the response and the logs and is never treated as identity or authorisation. A
real service reading it from a header would also bound its length and character
set before logging it, since it ends up in a log line.

2. Where is each check enforced?

Shape at the facade with Bean Validation, meaning in CustomerValidator, and
exposure in GlobalExceptionHandler. The handler is the only place that decides
what a client sees, which is why the 500 message is a constant and not an
argument.

3. Which values are sensitive?

Email addresses, and they are now out of client-facing messages. Stack traces
and exception messages are server-side only. Nothing here holds a password or a
token yet; when it does, the same rule applies at the same boundary.

CLEANUP

  mvn -B clean
  git status

No containers, no ports. Keep lab16-crm for Labs 17 and 18.

NOTES

The error model, the trust boundary and the Spring forward map are in
docs/error-model-notes.md. Lab 15's layer reasoning is still in
docs/service-layer-notes.md and Lab 14's in docs/dto-boundary-notes.md. Pre-lab
exercises and reflection answers are in notes/Week 2/Module 16/. Full GUIDE at
labs/Week 2 - Backend, AI Tools and Testing/module-16/lab16/.
