# Logging contract

What this service writes, and what it must never write. Matches the console in
`notes/screenshots/lab-20/02-api-manual.txt`.

## MDC keys

| Key | Set by | Scope |
| --- | --- | --- |
| `correlationId` | `CorrelationFilter` | the whole request, cleared in `finally` |
| `customerId` | `CustomerService` | one operation, removed in `finally` |
| `op` | `CustomerService` | one operation, removed in `finally` |

The filter owns the full clear. The service only puts and removes its own two
keys, so a service call cannot wipe the correlation id mid-request.

## Pattern

```
%d{ISO8601} %-5level [%thread] %logger{36} corr=%X{correlationId} cust=%X{customerId} op=%X{op} - %msg%n
```

Configured in `src/main/resources/logback-spring.xml`. `com.northstar.crm` and
root are both INFO. There is no `logback.xml` to compete with it.

## Fields

Allowed: `customerId`, `status`, reason codes, `durationMs`, HTTP status,
operation names.

Forbidden: `fullName`, `email`, phone, address, passwords, tokens, PAN. Full
names and emails stay in the domain model and the API responses; they do not
reach a log line or an MDC value.

## Correlation header

`X-Correlation-Id`, example `lab-request-001`. Read on entry, defaulted to
`lab-request-001` when absent or blank, echoed on the response by the filter.
The controller does not set it; setting it in both places emitted the header
twice.

## Levels

| Level | Used for | Example |
| --- | --- | --- |
| INFO | success path lifecycle | `Creating customer`, `Customer created status=ACTIVE durationMs=0` |
| WARN | rejected on purpose | `Create rejected reason=duplicate`, `Rejecting create reason=missing_full_name customerId=CUS-1002` |
| ERROR | unexpected failure, exception passed last | `Create failed` plus the stack |
| DEBUG | off by default in production profiles | not used in this service |

A rejected request never reaches ERROR. Validation failures leave the service
`try` untouched and duplicates rethrow from their own catch, so no expected
client error prints a stack trace.

## Operations

| `op` | Emitted by | Levels seen |
| --- | --- | --- |
| `customer.create` | `CustomerService.create` | INFO, WARN duplicate, ERROR |
| `customer.get` | `CustomerService.findById` | INFO |

Reason codes at the API edge: `invalid_id`, `missing_full_name`,
`missing_status`. These carry `cust=` empty because the controller rejects
before the service opens its MDC scope; the id is in the message instead.

## Enforcement

`CustomerLoggingIT` asserts the ids, the correlation id and the op appear on
the console and that `Amina`, `Dana Whitfield` and `@example.com` do not. The
rule fails the build rather than living in this file alone.

## Production

Ship the console to the central store; never embed secrets in the pattern.
Raise a single class or package to DEBUG for an incident, never root, and
revert it. Actuator, metrics and distributed tracing are Lab 21.
