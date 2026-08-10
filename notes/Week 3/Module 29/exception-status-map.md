# Lab 29 — Exception to Status Map

| Case | Status | Code |
| --- | --- | --- |
| Invalid body | 400 | VALIDATION_FAILED |
| CUS-9999 | 404 | CUSTOMER_NOT_FOUND |
| Duplicate CUS-1001 | 409 | DUPLICATE_CUSTOMER |
| Illegal transition | 409 | ILLEGAL_TRANSITION |
| Unhandled exception | 500 | INTERNAL_ERROR |

409 for the illegal transition. The body is well formed and the fields are all
valid, what blocks it is the current status of the customer, so it is a conflict
with the state of the resource rather than an unprocessable payload.

## Scope
Pre-lab only. All five are handled by one @RestControllerAdvice, not by
try/catch in each controller.


## Debug / design challenge

Is 500 acceptable for expected not-found?

no. a missing customer is an expected outcome and 404 says so. 500 tells the
client to retry and pages whoever owns the alert.

## Predict the Output / Behavior

Should duplicate be 400 or 409 in this lab?

409. the request is valid, CUS-1001 already existing is the conflict.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/exception-status-map.md`
- [ x ] 400/404/409 rows
- [ x ] Codes named
