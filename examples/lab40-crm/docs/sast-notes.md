# Lab 40 — Manual SAST notes

Data-flow pass over `crm-api`, tracing untrusted request values to sinks.
Dependency-Check found none of this: every finding below is a decision in code
written in Labs 36 and 39, not a library version.

Finding ids continue the ledger in `docs/security-findings.csv`.

## Untrusted inputs

Everything a caller controls, and where it enters:

| Input | Location | Reaches |
| --- | --- | --- |
| `{id}` path variable | `CustomerController:48`, `:59`, `:67` | `CustomerService.load` → JPQL |
| request body | `CustomerController:42`, `:60`, `:68` | entity fields, then INSERT/UPDATE |
| `status`, `page`, `size` | `CustomerController:28-30` | `findByStatus`, `PageRequest` |
| `X-Correlation-Id` header | `CustomerController:43`, `:61`, `:69` | exception messages, logs |
| `Authorization` bearer token | `JwtAuthenticationFilter` | the entire identity of the request |

## lab40-009 — Broken access control, object level

**FQN:** `com.northstar.crm.api.CustomerController#get(String)` →
`com.northstar.crm.service.CustomerService#get(String)` →
`#load(String)` → `CustomerRepository#findByPublicId`

**Locations:** `SecurityConfig:33`, `CustomerController:47-49`,
`CustomerService:73-75`, `CustomerService:93-96`

The only authorization on the customer API is the line

```java
.requestMatchers("/api/customers/**").hasAnyRole("AGENT", "ADMIN")   // SecurityConfig:33
```

That answers "is the caller a CRM user". Nothing answers "is this caller
entitled to *this* customer". The traversal is four hops and consults the
principal at none of them: the controller takes `{id}` straight from the path,
the service loads by `public_id`, the mapper returns the DTO. There is no
`@PreAuthorize` anywhere in the module, no `Authentication` or `Principal`
parameter on any handler, and no owner column on `customer` to compare against.

Same gap on the write paths, `CustomerController:57` `PUT` and
`CustomerController:65` `PATCH .../status`. The write side is worse: the
transition map at `CustomerService:21-25` allows every state into `CLOSED` and
nothing out of it, so one unauthorized `PATCH` is unrecoverable through the API.

**Risk:** any authenticated agent reads or edits any customer. One compromised
agent token exposes the whole book rather than that agent's own.

**Not yet proven.** `SecurityRulesTest` covers the *role* boundary — an AGENT
token is refused on `/api/admin/**` — and nothing asserts the object boundary,
because the application has no concept of ownership to assert. Step 6 writes
that reproducer.

**Policy question this raises, which the code cannot answer:** is a customer
owned by an agent at all? If every agent legitimately serves every customer,
this is not a finding and the assessment should say so explicitly rather than
leaving it ambiguous. Recorded as `needs_review` for that reason.

## lab40-010 — Weak default: committed signing key

**FQN:** `com.northstar.crm.security.JwtService#JwtService(String)`

**Locations:** `application.yml:34`, `JwtService:16`, `JwtService:56`

```yaml
jwt-secret: ${JWT_SECRET:lab-only-change-me}    # application.yml:34
```

```java
public JwtService(@Value("${northstar.security.jwt-secret}") String secret)  // JwtService:16
mac.init(new SecretKeySpec(secret.getBytes(UTF_8), "HmacSHA256"));           // JwtService:56
```

The fallback after the colon is the signing key when `JWT_SECRET` is unset, and
it is committed. Anyone with read access to the repository can mint a token for
any subject and any role, including ADMIN.

**Risk:** this defeats `lab40-009` and every other authorization control at
once. A forged ADMIN token satisfies `SecurityConfig:33` and `:34` perfectly.

**Contrast within the same file:** `application.yml:14` is
`password: ${CRM_APP_PASSWORD}` — no default, so a missing value fails startup
loudly. The datasource password got this right and the signing key did not.

## Injection — traced, no finding

Every read of an untrusted value reaches the database through a bound
parameter:

| Path | Mechanism |
| --- | --- |
| `findByPublicId(String)` | Spring Data derived query, `public_id = ?` |
| `existsByEmail(String)` | derived query |
| `findByStatus(String, Pageable)` | derived query plus `LIMIT`/`OFFSET` |

No `@Query` in this module builds JPQL by concatenation, no
`EntityManager.createNativeQuery` takes a request value — the one native query
in the codebase is in `CustomerRepositoryIT` and its `:id` is bound. `String +`
never reaches a query.

The guide's parameterized example is already the shape in use; the derived
methods bind for the same reason. The risk to record is a *future* `@Query`
written by append, not anything present today.

Paging is bounded at `CustomerController:31`,
`Math.min(Math.max(size, 1), 100)`, so `size=100000` cannot be used to pull the
table, and the sort is fixed in code rather than read from the query string, so
no caller can name an arbitrary column.

## PII in logs and errors

**Location:** `DuplicateCustomerException:13`

```java
super("duplicate customer email " + email + " [" + correlationId + "]");
```

The HTTP body is safe — `ApiExceptionHandler:41-44` returns a generic
ProblemDetail with no address in it — but the exception *message* is what a log
appender writes. A customer's email therefore reaches log storage, which has
different retention and access rules than the database it came from, and the
409 path is one an attacker can trigger deliberately to test which addresses
exist.

`CustomerService:65-67` is the same construct done correctly: the illegal
transition message carries two status values and the correlation id, no PII.

Two log sinks in the read path today: none. `CustomerService#get` logs nothing.

## Configuration review

| Setting | Location | State |
| --- | --- | --- |
| `ddl-auto` | `application.yml:18` | `validate` — correct, Hibernate never mutates the schema |
| `open-in-view` | `application.yml:16` | `false` — correct, lazy access stays inside the transaction |
| actuator exposure | `application.yml:31` | `health,info` only; `/env` and `/heapdump` not exposed |
| datasource password | `application.yml:14` | environment, no default — correct |
| CSRF | `SecurityConfig:28` | disabled |
| session policy | `SecurityConfig:29` | `STATELESS` |

CSRF disabled is defensible rather than an oversight, and the justification is
`SecurityConfig:29` two lines below it: the API is stateless and carries no
session cookie, so there is no ambient credential for a cross-site form to
abuse — the bearer token has to be attached deliberately. It is recorded here
so the gate defends the decision rather than discovering it.

## Summary

| Id | Finding | Class |
| --- | --- | --- |
| lab40-009 | object-level authz absent on read and write paths | needs_review, pending the ownership policy |
| lab40-010 | committed default JWT signing key | needs_review |
| — | injection | traced, no finding |
| — | configuration | four settings correct, CSRF documented |
| — | PII in exception message | folded into lab40-010's remediation batch |

Step 6 turns `lab40-009` into a failing test before anything is changed.
