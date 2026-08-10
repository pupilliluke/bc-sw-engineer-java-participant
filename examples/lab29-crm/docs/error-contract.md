Lab 29 error contract

| Case | Exception | Status | Message |
| ---- | --------- | ------ | ------- |
| Blank id, blank name, bad email | MethodArgumentNotValidException | 400 | Validation failed, with violations |
| GET CUS-9999 | IllegalArgumentException | 404 | Customer not found: CUS-9999 |
| POST an id that already exists | IllegalStateException | 409 | Duplicate customer: CUS-1001 |
| Anything else | Exception | 500 | Unexpected error, no stack trace |

The customer APIs are behind the lab 28 filter chain, so every call above needs
a Bearer token from POST /api/auth/login as agent1. Without one the request is
401 from the filter chain and never reaches the advice.

Lab 14 put constraints on the request DTO and lab 16 handled exceptions, and
they were two separate ideas with two separate shapes. Here they are one Boot
contract. @Valid on the controller turns a constraint into
MethodArgumentNotValidException, one @RestControllerAdvice turns that and the
two service exceptions into the same ErrorResponse, and a client parses one
shape for all four statuses.
