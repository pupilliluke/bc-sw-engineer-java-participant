# Lab 40 — Map CRM Attack Surfaces

## Reference

| Surface | OWASP theme | Example | Holds |
| --- | --- | --- | --- |
| `GET/PUT /api/customers/{id}` | Broken access control | agent opens `CUS-1001` | PII |
| `GET /api/customers/page` params | Injection | status filter, page, size | IDs |
| `POST /api/auth/login`, the JWT | Broken authentication | token issue and verify | credentials |
| `pom.xml` and its transitives | Vulnerable components | a CVE two levels down | none |
| `application.yml` | Security misconfiguration | `jwt-secret` default in Git | secret |
| log lines and exception messages | Logging failures | email inside a 409 message | PII |
| Kafka envelope, labs 30 and 31 | Injection, logging failures | event payload carrying a customer | PII |

## Step 1 — Inventory touchpoints

seven surfaces, more than the five asked for, because the CRM already has
them all built rather than planned.

PII against IDs is the split that decides how much each one matters.
`full_name`, `email` and `phone` are PII. `public_id` and `customer_id`
are identifiers, and `CUS-1001` on its own tells an attacker nothing
except that a record exists. that is why the customer read API is the
worst of the seven, it turns an id into a name and an email address.

the JWT surface covers both directions, issuing a token in
`AuthController` and verifying it in `JwtAuthenticationFilter`. the
signing key comes from `${northstar.security.jwt-secret}`, and
application.yml supplies a default for it.

Kafka is listed but not live in the lab 39 tree. labs 30 and 31 built the
producer and the DLT, the capstone wires them into this CRM, and an event
carrying a customer record is a PII sink the moment it exists.

## Step 2 — Check the reference

the five deck themes map onto the seven surfaces without a gap:

| Theme | Where it lands |
| --- | --- |
| injection | search and page parameters, Kafka payloads |
| broken access control | the customer read and write APIs |
| security misconfiguration | application.yml, CSRF disabled, actuator exposure |
| vulnerable components | pom.xml transitives |
| logging and monitoring failures | correlation ids, PII in messages |

two of these are already in better shape than the theme suggests.
injection through the API is narrow because every query is a Spring Data
derived method or a bound `@Query`, so no string concatenation reaches
SQL. misconfiguration is partly handled too, actuator exposes only
`health` and `info`, not `/env`, and `.env` is gitignored with
`.env.example` carrying blank password values.

CSRF is disabled in `SecurityConfig`. that is defensible for a stateless
token API where the browser is not carrying a session cookie, and it is
the same call lab 36 made, but it is a decision to defend at the gate
rather than an oversight to leave unmentioned.

## Step 3 — Rank top three

**1. object-level authorization on the customer API.** `SecurityConfig`
requires role AGENT or ADMIN for `/api/customers/**`, and that is the
only check. there is no `@PreAuthorize`, no principal comparison, nothing
in `CustomerController` or `CustomerService` that asks whether this agent
should see this customer. any agent who can reach the API can read every
record in it. business impact: one compromised agent account exposes the
whole customer base rather than that agent's own book, and the 50,002
rows lab 38 loaded are the scale of that exposure.

**2. the JWT signing secret's default value.** application.yml reads
`${JWT_SECRET:lab-only-change-me}`, so the fallback is committed and
known. anyone holding it can forge a token for any role, including ADMIN,
which makes the role checks in point 1 decorative. business impact:
authentication becomes an honour system, and no access-control fix above
it holds.

**3. PII in error paths.** `DuplicateCustomerException` builds its
message as the email address plus the correlation id. the handler returns
a generic body to the client, which is right, but the exception message
itself is what reaches the log. business impact: a customer's email ends
up in log storage that has a different retention and access policy than
the database it came from, and the 409 path is the one an attacker can
trigger deliberately to enumerate which addresses exist.

the ranking is deliberately ordered by what a release gate before
containers should block on. all three are decisions in code I already
wrote, none of them needs a scanner to find, and a Dependency-Check run
would report none of them.

## Step 4 — Anchor to fixtures

Amina `CUS-1001` ACTIVE and Ravi `CUS-1002` PROSPECT, the same synthetic
pair since lab 37. no real customer data anywhere in these notes.

the concrete question for point 1 is whether agent-a, whose book is
Ravi, can open Amina. today the answer is yes, and nothing in
`SecurityRulesTest` asks.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab40-owasp-surface-map.md`
- [ x ] At least five surfaces listed
- [ x ] Top three ranked with business impact
- [ x ] Synthetic fixtures only
