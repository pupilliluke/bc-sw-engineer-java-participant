Lab 16 API exception handling (reflection questions, checkpoints, manual
verification, failure experiments)

built under examples\lab16-crm, copied forward from lab15-crm. ErrorResponse
with six fields, BusinessException carrying a code and a status hint,
GlobalExceptionHandler mapping three families to one shape, the facade returning
ApiResult, fifty-two tests green.


REFLECTION QUESTIONS

1. Which design decision most affected correctness?

deciding the status at the throw site instead of in the handler. the exception
carries the code and the hint, so the handler copies rather than guesses, and
lab 15's bug where a repository outage came back as CUSTOMER_CONFLICT cannot
happen. the second was returning ApiResult, which took the try/catch out of
every caller.

2. What evidence proves the implementation works?

fifty-two green tests, and specifically unexpectedIsGeneric500 and
unexpectedFailureBecomesAGeneric500, which assert what is absent from the
payload as well as what is present. the Main transcript shows the 400, the 404
and the 409 with the same six fields and lab-request-001 on each, and 1001 still
ACTIVE after the rejected transition. output is under
examples\lab16-crm\notes\screenshots\lab-16.

3. Which failure was hardest to diagnose?

none of them was hard, which is the change from lab 15. the interesting one was
experiment 4. the wrong catch order does not misbehave at runtime here, javac
refuses to compile it, so reproducing the guide's symptom needed the specific
catch deleted rather than moved. the compiler catching it is the answer to why
typed exceptions were worth the refactor.


CHECKPOINTS

| Checkpoint | Confirm | Result |
| --- | --- | --- |
| A1 | lab16-crm under examples/ | Pass |
| A2 | ErrorResponse always includes correlationId and errors | Pass, errors is {} when empty |
| A3 | BusinessException factories for notFound and conflict | Pass, plus validation for the 400 backstop |
| B1 | handler maps business, validation and unexpected | Pass, four methods, fromFields shares the 400 |
| B2 | facade returns ApiResult Ok or Fail | Pass, sealed, no throw on the demo path |
| B3 | catch order business before generic | Pass, in all three methods |
| C1 | 400 validation JSON with field errors and lab-request-001 | Pass, errors.email |
| C2 | 404 for CUS-9999 | Pass |
| C3 | 409 illegal transition, CUS-1001 still ACTIVE | Pass, asserted in the store and printed |
| D1 | GlobalExceptionHandlerTest green | Pass, 6 tests, plus ErrorResponseTest 4 |
| D2 | no stack traces or secrets in client payloads or git | Pass, target/ ignored |
| D3 | error model notes and status choices documented | Pass, README plus docs/error-model-notes.md |


MANUAL VERIFICATION

| # | Check | Result |
| --- | --- | --- |
| 1 | create and get CUS-1001 still succeed | Pass |
| 2 | invalid email gives 400 with errors.email and correlation | Pass |
| 3 | CUS-9999 gives the 404 payload | Pass |
| 4 | illegal transition gives 409, status unchanged | Pass |
| 5 | correlation on every failure | Pass, six of six in the transcript |
| 6 | no stack traces in client JSON | Pass, proved by experiment 5 and two tests |
| 7 | handler unit tests pass | Pass |
| 8 | no secrets in git, target/ ignored | Pass |
| 9 | README documents the status choices | Pass, 409 over 422 with the rule |
| 10 | can explain the ControllerAdvice mapping | Pass, forward map in docs/error-model-notes.md |


FAILURE EXPERIMENTS

1. made findById throw a bare RuntimeException carrying a SELECT and a jdbc url
for CUS-9999. the client got 500 INTERNAL_ERROR with the fixed message and
nothing else, and the SQL and the database host stayed in the server log with
the stack. this is lab 15's experiment 1 answered, an infrastructure failure is
no longer reported as a conflict. restored.

2. blank fullName and not-an-email in one request. both fields came back in
errors, email then fullName, and the order is deterministic because
fromValidation sorts by field path before collecting; validate returns a Set.
no change needed.

3. asked for CUS-9999 twice. identical status, code, message and correlation id
both times, only the timestamp differs. the correlation id is per request and
the caller supplied the same one, so two calls that share an id are two calls
support can read as one unit of work. no change needed.

4. tried to catch Exception before BusinessException. javac refused, exception
BusinessException has already been caught, because BusinessException extends
RuntimeException and the specific catch becomes unreachable. deleted the
specific catch instead, which compiles, and the ACTIVE to PROSPECT conflict came
back as 500 INTERNAL_ERROR with the conflict message gone from the payload.
restored.

5. put ex.toString() into the 500 message. the payload came back with
java.lang.RuntimeException, the SELECT and jdbc:postgresql://crm-db:5432 in the
message field. exactly two tests fail on it, unexpectedIsGeneric500 and
unexpectedFailureBecomesAGeneric500, on the fixed-message assertion before the
absence checks for jdbc and the exception class name get a chance to run.
restored.
