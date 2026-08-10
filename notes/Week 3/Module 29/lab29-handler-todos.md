# Lab 29 — GlobalExceptionHandler TODOs

## Advice annotation
@RestControllerAdvice on class GlobalExceptionHandler.

## Handlers (list)
- handleMethodArgumentNotValid(MethodArgumentNotValidException) -> 400, field
  violations in the body
- handleCustomerNotFound(CustomerNotFoundException) -> 404
- handleDuplicate(DuplicateCustomerException) -> 409
- handleIllegalTransition(IllegalStatusTransitionException) -> 409
- handleGeneric(Exception) -> 500

## 500 rule
No stack trace, no SQL, no internal class or table name in the body. Log the
exception with the correlation id, return the envelope with a generic message.

## Scope
Pre-lab only. The advice does not run validation. The controller still needs
@Valid on the @RequestBody or MethodArgumentNotValidException is never thrown.
GET CUS-1001 and CUS-1002 still return 200 after the handlers exist.


## Debug / design challenge

Should CustomerNotFoundException be handled before Exception?

it has to exist as its own handler. spring picks the most specific match, so
file order does not matter, but with only handleGeneric a missing customer is a
500.

## Predict the Output / Behavior

Where must the advice class live for component scanning?

under the @SpringBootApplication package, com.northstar.crm or below it.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab29-handler-todos.md`
- [ x ] Advice type
- [ x ] 400/404/409
- [ x ] Safe 500
