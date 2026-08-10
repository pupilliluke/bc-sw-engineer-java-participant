# Lab 28 — Authentication Versus Authorization

| Concept | Question | CRM example | HTTP |
| --- | --- | --- | --- |
| Authentication | Who are you? | Missing/bad JWT | 401 |
| Authorization | What may you do? | agent1 hits /api/admin | 403 |

## Correlation vs auth
Correlation lab-request-001 ≠ authentication.

## Debug / design challenge

Expired JWT on a permitted role — 401 or 403?

401. the token fails authentication, so the role is never reached.

## Predict the Output / Behavior

Valid AGENT token on /api/admin — 401 or 403?

403. authentication passes, AGENT is just not allowed on that route.

## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/authn-authz.md`
- [ x ] Both concepts
- [ x ] 401/403 mapped
- [ x ] Correlation note
