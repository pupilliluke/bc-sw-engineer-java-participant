# Lab 27 — Rollback Evidence Plan

## Before measurement
read ACC-1001-MAIN and ACC-1001-LOYALTY balances and count the TransactionLog
rows. write both down before anything moves.

## Force-fail action
POST a transfer from ACC-1001-MAIN to ACC-FORCE-FAIL with correlation
lab-request-001. expect an error response, not a 200.

## After assertions
MAIN unchanged at the before number, LOYALTY unchanged, and no success
TransactionLog row for lab-request-001. happy path MAIN to LOYALTY is the
contrast: both balances move and exactly one log row is written.

reject any AI draft that catches Exception and swallows it inside a
@Transactional method. nothing propagates out, Spring commits the partial debit
and atomicity is gone.

automated test idea: seed the two accounts, call transfer to ACC-FORCE-FAIL,
assert the exception, then re-read both balances and the log count and assert
all three are unchanged.

## Evidence location
notes/screenshots/lab-27/

## Scope
Pre-lab only.


## Debug / design challenge

If the log uses REQUIRES_NEW, what misleading evidence might you see?

a log row that survives the rollback. it commits in its own transaction, so the
balances roll back and the row stays, which reads as a transfer that happened.

## Predict the Output / Behavior

Why reset seeds between automated tests?

a transfer in one test leaves the balances moved, so the next test's before
number is wrong and its assertion passes or fails for the wrong reason.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/rollback-plan.md`
- [ x ] Before/after
- [ x ] ACC-FORCE-FAIL
- [ x ] Log assertion
