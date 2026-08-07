# Lab 25 — AI Review Policy

lab25-001

## Must reject
service returning ResponseEntity.
controller calling the map store directly.
hard-coded prod password.
@Autowired fields in place of constructor injection.

## Must check
service goes through the repository interface, accept after review.
fixtures still CUS-1001 ACTIVE and CUS-1002 PROSPECT.
duplicate create still fails.

## Where to record review
docs/lab25-001.md, accept or reject with the reason. N/A when no AI was used and
the layering was done by hand. AI is optional here, never required.

## Scope
Pre-lab only.


## Debug / design challenge

Copilot suggests @Autowired fields on CustomerService — accept or reject?

reject. constructor injection with a final field, so a missing repository fails
at startup rather than at the first request.

## Predict the Output / Behavior

If you did not use AI, what do you write in lab25-001?

N/A, layering completed manually.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/ai-review-policy.md`
- [ x ] Reject list
- [ x ] Check list
- [ x ] Record location
