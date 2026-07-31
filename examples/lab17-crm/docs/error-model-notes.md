Error model notes (Lab 16)

THE SHAPE

Six fields, on every failure, success or not.

| Field | Type | Note |
| --- | --- | --- |
| timestamp | ISO-8601 UTC | Instant.now at construction, or passed in for a test |
| status | int | the HTTP status a transport would use |
| error | string | the stable code a client switches on |
| message | string | for a human reading a support ticket |
| correlationId | string | the caller's id, never generated here |
| errors | object | field to message, {} when there is no field to blame |

errors is always in the document. An empty object is a value a client can read;
a missing key is a branch a client has to write.

STATUS TABLE

| Case | status | code |
| --- | --- | --- |
| Bean Validation on the request DTO | 400 | VALIDATION_FAILED |
| unknown status string, missing request | 400 | VALIDATION_FAILED |
| unknown customer id | 404 | CUSTOMER_NOT_FOUND |
| illegal transition, duplicate id, duplicate email | 409 | BUSINESS_CONFLICT |
| anything not expected | 500 | INTERNAL_ERROR |

409 AND NOT 422

Both were considered. 422 says the request was understood and is semantically
wrong on its own terms; ACTIVE -> PROSPECT is neither. The same request against
the same customer an hour earlier would have succeeded, and the same request
against Ravi succeeds now. What makes it fail is the stored state, which is what
409 means. The rule for this project is one line: if the answer changes when the
stored data changes, it is 409.

A duplicate email is the same argument, so both conflicts share one code.

WHERE THE STATUS IS DECIDED

At the throw site, not in the handler. BusinessException carries a code and a
status hint, and the handler copies them. A new failure type therefore does not
need a new branch in the handler.

The alternative is a switch on the exception class in the handler. That reads
the same until two exception classes need the same status, and then the switch
grows a case per class and drifts.

THREE FAMILIES IN, ONE SHAPE OUT

| Family | Method | What decides the status |
| --- | --- | --- |
| typed domain failure | fromBusiness | the exception's own hint |
| Bean Validation violations | fromValidation | always 400 |
| a field failure with no violation | fromFields | always 400 |
| everything else | fromUnexpected | always 500 |

fromValidation sorts violations by field path before collecting them, because
validate() returns a Set and the field order in the JSON would otherwise move
between runs.

TRUST BOUNDARY

| Detail | Log | Client payload |
| --- | --- | --- |
| stack trace | yes, ERROR with the throwable | no |
| exception class and message | yes | no |
| the failing field name and rule | yes | yes |
| customer id | yes | yes |
| email address | no | no |
| correlationId | yes | yes |

fromUnexpected logs the throwable and returns a fixed sentence. ex.getMessage()
on an unexpected failure is written by whatever threw it, and a driver or a file
API will put a host, a path or a query in it. Failure experiment 5 put
ex.toString() in the message and the client payload came back with the database
host and the SQL.

The duplicate-email conflict says the address is already registered without
repeating it. The customer id is a stable internal identifier and stays in the
message; the address is the customer's own data.

CATCH ORDER

    catch (BusinessException ex)   // specific, first
    catch (Exception ex)           // fallback, last

In that order in all three facade methods. Reversing them is not a runtime bug
here, javac rejects it with "exception BusinessException has already been
caught". Deleting the specific catch instead compiles, and the 409 comes back as
a 500.

WHAT LAB 15 THREW

| Failure | Lab 15 | Lab 16 |
| --- | --- | --- |
| illegal transition | IllegalStateException | BusinessException 409 |
| duplicate id or email | IllegalStateException | BusinessException 409 |
| unknown customer | IllegalArgumentException | BusinessException 404 |
| blank id or name below the facade | IllegalArgumentException | BusinessException 400 |
| repository outage | IllegalStateException | untyped, 500 |

The last row is the point. Lab 15's facade reported a storage outage as
CUSTOMER_CONFLICT, because a rule violation and an infrastructure failure both
arrived as IllegalStateException. Now only the deliberate failures are typed and
anything else falls to the 500.

SPRING FORWARD MAP

    @RestControllerAdvice
    class GlobalExceptionHandler {
        @ExceptionHandler(BusinessException.class)                 -> fromBusiness
        @ExceptionHandler(MethodArgumentNotValidException.class)    -> fromValidation
        @ExceptionHandler(Exception.class)                          -> fromUnexpected
    }

The three methods do not change; the annotations replace the try/catch in the
facade, and ResponseEntity.status(error.getStatus()).body(error) replaces
ApiResult.Fail. Spring hands the validation handler a
MethodArgumentNotValidException rather than a Set of violations, so
fromValidation gains an adapter that pulls getBindingResult().getFieldErrors()
into the same field map.

The correlation id moves to a filter that reads the request header, puts it in
the MDC, and generates one when the header is absent.
