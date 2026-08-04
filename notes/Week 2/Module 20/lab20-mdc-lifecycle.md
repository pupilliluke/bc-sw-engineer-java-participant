# Lab 20 — MDC Lifecycle

## Put
On request entry, a filter puts the correlation ID into the MDC.
MDC.put("correlationId", "lab-request-001");

## Use
Every log line inside that request picks the value up from the MDC through the logback pattern, so no method has to pass it as an argument.
<pattern>%d{ISO8601} %-5level [%thread] %logger{36} corr=%X{correlationId:-none} - %msg%n</pattern>

## Clear
finally { MDC.clear(); }
Always, even when the request fails. The thread goes back to the pool, so a key left behind would be inherited by the next request on that thread.

## Lab 21 boundary
A correlation ID in the logs is not distributed tracing. Trace IDs, spans and the Actuator and metrics work are Lab 21.

## Scope
Pre-lab only.

Where should put happen — controller method body only, or a filter wrapping all requests?

A filter wrapping all requests. The ID goes in on request entry and comes out in the filter's finally, so every request is covered and every log line in between carries it.
A controller method body only covers that one method. Other endpoints get no correlation ID, and anything logged before or after the controller runs is missed.

Does Logback pattern %X{corr} work if you never MDC.put?

The pattern still runs, it just has nothing to print. %X{corr} resolves to an empty value when the key was never put.
Writing it as corr=%X{correlationId:-none} prints none instead of a blank, which is easier to read in the log.

- [ X ] File exists at `notes/lab20-mdc-lifecycle.md`
- [ X ] Put step
- [ X ] Use step
- [ X ] Clear step
