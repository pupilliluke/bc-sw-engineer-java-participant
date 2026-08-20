# Lab 40 — Fill SAST Path TODOs

## Step 1 — Copy template

```
Endpoint: _____
Authz check: _____
Sink (SQL/file/log): _____
Customer fixture used: _____
Risk if missing check: _____
```

## Step 2 — Fill for customer read

```
Endpoint: GET /api/customers/{id}
Authz check: role only. SecurityConfig requires hasAnyRole("AGENT","ADMIN")
  on /api/customers/**. no @PreAuthorize, no principal comparison, no
  object-level owner check anywhere in CustomerController or
  CustomerService. TODO: decide whether an agent is scoped to a book of
  customers, and if so enforce it here.
Sink (SQL/file/log): JPQL through CustomerRepository.findByPublicId, a
  Spring Data derived method, so the id is a bound parameter and never
  concatenated. no logging on the read path today.
Customer fixture used: CUS-1001 (Amina Khan), ACTIVE
Risk if missing check: any authenticated agent reads any customer. the
  role check answers "is this a CRM user", never "is this their record",
  so one compromised agent token exposes every row rather than that
  agent's own. OWASP A01, and the scanner in exercise 2 cannot see it.
```

the traversal is worth writing out because it is short and it is where
the gap sits. the controller takes `{id}` from the path and hands it
straight to the service, which loads by `public_id` and maps to a DTO.
nothing between the filter and the database consults who is asking.

`SecurityRulesTest` already proves the role boundary holds, an AGENT
token is rejected on `/api/admin/**`. it does not test whether agent-a
can read a customer belonging to agent-b, because the application has no
concept of that ownership yet. the missing test and the missing check are
the same gap seen from two sides.

## Step 3 — Second path

```
Endpoint: PATCH /api/customers/{id}/status
Authz check: role only, same hasAnyRole("AGENT","ADMIN"). the business
  rule that does exist is the ALLOWED transition map in CustomerService,
  which is correctness rather than authorization: it blocks
  PROSPECT -> SUSPENDED for everyone equally. TODO: decide whether a
  status change is an AGENT action at all, or ADMIN only.
Sink (SQL/file/log): UPDATE through the repository, with @Version so a
  stale write is rejected. the log sink is the IllegalStateException
  message on an illegal transition, which carries the correlation id
  lab-request-001 and the two status values, no PII.
Customer fixture used: CUS-1002 (Ravi Singh), PROSPECT
Risk if missing check: an agent can move any customer to CLOSED, which
  is terminal in the transition map. no endpoint reopens a CLOSED
  customer, so a single unauthorized call is unrecoverable through the
  API and needs a database edit to undo.
```

CLOSED being terminal is the part that turns a low-severity authz gap
into a real one. the transition map allows every state into CLOSED and
allows nothing out of it, so the blast radius of an unauthorized write
here is larger than the read path's.

## Step 4 — Self-check

no passwords, no tokens, no real PII in these notes. the two fixtures are
the synthetic pair from lab 37, and the only email addresses that appear
anywhere in this module's notes are `@example.com` and `@example.test`.

what stays `_____` for lab 40 to prove with code, not prose:

| Open | What proves it |
| --- | --- |
| whether object-level authz is in scope for this CRM | a decision recorded, then a check |
| the negative test for it | a test where agent-a is refused CUS-1002, currently unwritten |
| whether status change is ADMIN only | a rule in SecurityConfig or @PreAuthorize |
| the jwt-secret default | removing the fallback so a missing JWT_SECRET fails startup |

that last row is not from either path above, it came out of the surface
map, and it belongs on this list because it makes the other three moot
while it stands.

## Scope
Pre-lab only — do not finish the full graded lab in this exercise.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/lab40-sast-todo-notes.md`
- [ x ] Template filled for read and write paths
- [ x ] Fixtures CUS-1001/CUS-1002 used
- [ x ] No secrets in notes
