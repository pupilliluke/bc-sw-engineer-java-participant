# Lab 40 — Threat checklist

Scope written before any scanner runs, so findings can be judged against
stated targets rather than sorted by volume.

## Scope

**In scope:** `crm-api` in this repository — its source, its `pom.xml` and the
transitive dependencies that reach the runtime classpath, its
`application.yml`, and the Flyway migrations under `db/migration`.

**Out of scope:** the container image (Lab 41 builds it, it does not exist
yet), the k3s manifests (Lab 42), `crm-ui`, the PostgreSQL server itself, and
every third-party system. Nothing outside this module is a scan target.

**Authorized targets:** this repository and the local `crm-postgres-lab40`
container only. No network scanning, no traffic at any host that is not
localhost.

**Fixtures:** synthetic `CUS-1001` (Amina Khan, ACTIVE) and `CUS-1002` (Ravi
Singh, PROSPECT). No real customer data enters this lab, its reports, or its
notes.

**Baseline:** `mvn -B clean verify` green at 22 tests — 15 unit, 7 integration
— against `crm40`, Flyway at v2. Any lower count after remediation is a
regression, not a security improvement.

## Components

| Component | What it is | Trust |
| --- | --- | --- |
| `crm-api` | Spring Boot 3.3.5, JDK 21 | runs our code, trusted |
| PostgreSQL 17 | `crm40` in Docker, port 5432 | trusted, reached over localhost |
| `application.yml` | config, reads `../.env` via `spring.config.import` | holds a secret reference |
| `.env` | real values, gitignored | secret store |
| JWT | issued by `AuthController`, verified by `JwtAuthenticationFilter` | the entire basis of identity |

## Data classes

| Class | Fields | Handling |
| --- | --- | --- |
| PII | `full_name`, `email`, `phone` | never in logs, reports, or committed evidence |
| Identifiers | `public_id`, `customer_id` | safe in evidence; `CUS-1001` alone reveals nothing |
| Business state | `status`, `balance`, `version` | safe in evidence |
| Secrets | DB password, JWT signing key | never in Git, never in a report, environment only |

## Users and trust boundaries

Two roles: **AGENT** and **ADMIN**. `SecurityConfig` permits
`/api/auth/login`, `/actuator/health` and `/error` anonymously, requires
AGENT or ADMIN on `/api/customers/**`, and ADMIN on `/api/admin/**`.

| Boundary | Crossing | Enforced by |
| --- | --- | --- |
| anonymous → authenticated | bearer token on every request | `JwtAuthenticationFilter` |
| AGENT → ADMIN | `/api/admin/**` | `SecurityConfig`, tested by `SecurityRulesTest` |
| agent → another agent's customer | **not a boundary today** | nothing |
| app → database | JDBC as `crm_app` | least-privilege role from Lab 37 |

The third row is the finding. There is no notion of a customer belonging to an
agent, so there is nothing to enforce and nothing to test.

## OWASP mapping to concrete endpoints

| OWASP theme | Endpoint / location | Concrete risk |
| --- | --- | --- |
| Broken access control | `GET /api/customers/{id}`, `PUT /api/customers/{id}`, `PATCH /api/customers/{id}/status` | role checked, object never. Any agent reads or edits any customer; `CustomerController` and `CustomerService` contain no `@PreAuthorize` and no principal comparison |
| Injection | `GET /api/customers/page?status=&page=&size=`, `CustomerRepository` | low as written — every query is a Spring Data derived method or a bound `@Query`, so no concatenation reaches SQL. The risk is a future `@Query` built by string append |
| Auth failures | `POST /api/auth/login`, `JwtService` | `${northstar.security.jwt-secret}` has the committed fallback `lab-only-change-me` in `application.yml`. Anyone with the repo can mint an ADMIN token, which makes every row above it decorative |
| Security misconfiguration | `application.yml`, `SecurityConfig` | actuator exposes only `health` and `info`, not `/env` — good. CSRF is disabled, defensible for a stateless bearer-token API but a decision to defend, not an omission |
| Logging failures | `DuplicateCustomerException`, `ApiExceptionHandler` | the exception message is built from the email address. The HTTP body is generic, but the message is what reaches the log, so a customer email lands in log storage with different retention than the database it came from |

## Ranked for the gate

1. **Object-level authorization** — one compromised agent token exposes every
   customer rather than that agent's own.
2. **JWT signing secret default** — forgeable ADMIN tokens make item 1's role
   checks meaningless.
3. **PII in the 409 path** — an attacker can trigger it deliberately to
   enumerate which email addresses exist.

None of the three is something Dependency-Check can find. All three are
decisions in code written in Labs 36 and 39.

## Triage ledger columns

Defined before scanning, so findings are classified rather than counted:

```
finding_id,source,package_or_location,cve_or_rule,cvss,classification,owner,due_date,notes
```

Header committed empty at `docs/security-findings.csv`. Classifications:
`needs_review` (start state, not a resting place), `true_positive`,
`false_positive`, `accepted_risk` (owner and expiry required), `fixed`
(re-scan evidence required).
