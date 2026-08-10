# Lab 27 — Propagation Warnings

| Pattern | Risk |
| --- | --- |
| REQUIRES_NEW on log | the log commits in its own transaction, so the row survives a rollback and reads as a transfer that happened |
| Self-invocation | this.transfer() from inside the same class never crosses the proxy, so @Transactional is ignored and nothing is atomic |
| Swallow exception | catching Exception inside the method means nothing propagates out, Spring commits the partial debit |
| TX on controller | the boundary follows the protocol rather than the operation, and SOAP and REST get two different definitions of atomic |

NOT_SUPPORTED mid-transfer is the fourth one to avoid. it suspends the
transaction, so whatever runs under it is outside the rollback.

## Lab default
REQUIRED on the outer transfer(). nothing inside needs custom propagation, and
Spring Boot's default transaction manager is enough. no custom manager for this
lab.

## Scope
Pre-lab only.


## Debug / design challenge

Copilot suggests try/catch around debit that returns null — accept or reject?

reject. the exception never leaves the method so Spring commits, and the caller
gets null instead of a failure.

## Predict the Output / Behavior

Why is REQUIRED usually enough for this lab?

debit, credit and the log are one unit of work with one entry point, so there is
no second transaction to join or suspend.


## Pass criteria

Self-check before marking Pass:

- [ x ] File exists at `notes/propagation-warnings.md`
- [ x ] Four risks
- [ x ] Lab default REQUIRED
